package remast.marga.middleware;

import remast.marga.HttpHeader;
import remast.marga.Middleware;
import remast.marga.RequestHandler;
import remast.marga.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * Middleware that compresses response bodies using gzip compression.
 * Only compresses responses that are larger than a minimum threshold
 * and have compressible content types.
 */
public class GzipCompressionMiddleware implements Middleware {
    private static final Logger logger = Logger.getLogger(GzipCompressionMiddleware.class.getName());
    private static final String GZIP_ENCODING = "gzip";
    private static final int MIN_COMPRESSION_SIZE = 1024; // 1KB minimum
    
    private final int minCompressionSize;
    
    public GzipCompressionMiddleware() {
        this(MIN_COMPRESSION_SIZE);
    }
    
    public GzipCompressionMiddleware(int minCompressionSize) {
        this.minCompressionSize = minCompressionSize;
    }
    
    @Override
    public RequestHandler apply(RequestHandler handler) {
        return request -> {
            var response = handler.handle(request);
            
            // Only compress if response is successful and has a body
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300 
                && response.getBody() != null && !response.getBody().isEmpty()) {
                
                var originalBody = response.getBody();
                var originalSize = originalBody.getBytes(StandardCharsets.UTF_8).length;
                
                // Only compress if body is large enough and content type is compressible
                if (originalSize >= minCompressionSize && isCompressible(response.getMediaType())) {
                    try {
                        var compressedBody = compress(originalBody);
                        var compressionRatio = (double) compressedBody.length / originalSize;
                        
                        // Only use compression if it actually reduces size significantly
                        if (compressionRatio < 0.9) {
                            logger.fine(String.format("Compressed response from %d to %d bytes (%.1f%% reduction)", 
                                originalSize, compressedBody.length, (1 - compressionRatio) * 100));
                            
                            return new Response(compressedBody, response.getStatusCode(), response.getMediaType())
                                .header(HttpHeader.CONTENT_ENCODING, GZIP_ENCODING);
                        }
                    } catch (IOException e) {
                        logger.warning("Failed to compress response: " + e.getMessage());
                    }
                }
            }
            
            return response;
        };
    }
    
    private boolean isCompressible(remast.marga.MediaType mediaType) {
        if (mediaType == null) return true;
        
        var type = mediaType.value().toLowerCase();
        
        // Don't compress already compressed formats
        if (type.contains("gzip") || type.contains("deflate") || type.contains("br") ||
            type.contains("zip") || type.contains("7z") || type.contains("rar") ||
            type.contains("tar") || type.contains("gz")) {
            return false;
        }
        
        // Don't compress images, videos, audio (usually already compressed)
        if (type.startsWith("image/") || type.startsWith("video/") || type.startsWith("audio/")) {
            return false;
        }
        
        // Compress text-based content
        return type.startsWith("text/") || 
               type.contains("json") || 
               type.contains("xml") || 
               type.contains("html") || 
               type.contains("css") || 
               type.contains("javascript") ||
               type.contains("application/");
    }
    
    private byte[] compress(String data) throws IOException {
        var outputStream = new ByteArrayOutputStream();
        try (var gzipStream = new GZIPOutputStream(outputStream)) {
            gzipStream.write(data.getBytes(StandardCharsets.UTF_8));
        }
        return outputStream.toByteArray();
    }
}
