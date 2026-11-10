package remast.marga.middleware;

import remast.marga.HttpHeader;
import remast.marga.HttpStatus;
import remast.marga.MediaType;
import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * Middleware that compresses response bodies using gzip compression.
 * Only compresses responses for clients that accept gzip, that are larger
 * than a minimum threshold and have compressible content types.
 */
public class GzipCompressionMiddleware {
    private static final Logger logger = Logger.getLogger(GzipCompressionMiddleware.class.getName());
    private static final String GZIP_ENCODING = "gzip";
    private static final int MIN_COMPRESSION_SIZE = 1024;
    private static final Set<String> COMPRESSIBLE_APPLICATION_TYPES = Set.of(
        "application/json",
        "application/xml",
        "application/javascript",
        "application/ecmascript",
        "application/x-javascript",
        "application/x-www-form-urlencoded",
        "application/graphql",
        "application/ld+json",
        "application/manifest+json"
    );

    private final int minCompressionSize;

    public GzipCompressionMiddleware() {
        this(MIN_COMPRESSION_SIZE);
    }

    public GzipCompressionMiddleware(int minCompressionSize) {
        this.minCompressionSize = minCompressionSize;
    }

    /**
     * Creates a gzip compression middleware function.
     * @return A function that takes a RequestHandler and returns a RequestHandler with compression
     */
    public Function<RequestHandler, RequestHandler> create() {
        return handler -> request -> {
            var response = handler.handle(request);
            if (!isEligible(response)) {
                return response;
            }

            addVaryAcceptEncoding(response);
            if (!acceptsGzip(request)) {
                return response;
            }

            var originalBody = response.getBodyBytes();
            try {
                var compressedBody = compress(originalBody);
                var compressionRatio = (double) compressedBody.length / originalBody.length;
                if (compressionRatio >= 0.9) {
                    return response;
                }

                logger.fine(() -> String.format("Compressed response from %d to %d bytes (%.1f%% reduction)",
                    originalBody.length, compressedBody.length, (1 - compressionRatio) * 100));
                return copyWithBody(response, compressedBody).header(HttpHeader.CONTENT_ENCODING, GZIP_ENCODING);
            } catch (IOException e) {
                logger.warning("Failed to compress response: " + e.getMessage());
                return response;
            }
        };
    }

    private boolean isEligible(Response response) {
        var status = response.getStatusCode();
        return status >= 200 && status < 300
            && status != HttpStatus.NO_CONTENT.getCode()
            && status != HttpStatus.PARTIAL_CONTENT.getCode()
            && findHeaderName(response.getHeaders(), HttpHeader.CONTENT_ENCODING.getValue()) == null
            && response.getBodyBytes().length >= Math.max(1, minCompressionSize)
            && isCompressible(response.getMediaType());
    }

    static boolean acceptsGzip(Request request) {
        var acceptEncoding = request.header(HttpHeader.ACCEPT_ENCODING);
        if (acceptEncoding == null) {
            return false;
        }

        Double gzipQuality = null;
        Double wildcardQuality = null;
        for (var element : acceptEncoding.split(",")) {
            var parts = element.split(";");
            var coding = parts[0].trim().toLowerCase(Locale.ROOT);
            if (coding.isEmpty()) {
                continue;
            }
            var quality = parseQuality(parts);
            switch (coding) {
                case "gzip", "x-gzip" -> gzipQuality = gzipQuality == null ? quality : Math.max(gzipQuality, quality);
                case "*" -> wildcardQuality = quality;
                default -> { }
            }
        }

        if (gzipQuality != null) {
            return gzipQuality > 0;
        }
        return wildcardQuality != null && wildcardQuality > 0;
    }

    private static double parseQuality(String[] parts) {
        for (var i = 1; i < parts.length; i++) {
            var param = parts[i].trim();
            var separator = param.indexOf('=');
            if (separator < 0 || !param.substring(0, separator).trim().equalsIgnoreCase("q")) {
                continue;
            }
            try {
                var quality = Double.parseDouble(param.substring(separator + 1).trim());
                return quality >= 0 && quality <= 1 ? quality : 0;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 1;
    }

    static boolean isCompressible(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }

        var type = mediaType.value().toLowerCase(Locale.ROOT);
        return type.startsWith("text/")
            || COMPRESSIBLE_APPLICATION_TYPES.contains(type)
            || type.endsWith("+json")
            || type.endsWith("+xml");
    }

    private static void addVaryAcceptEncoding(Response response) {
        var varyName = HttpHeader.VARY.getValue();
        var acceptEncoding = HttpHeader.ACCEPT_ENCODING.getValue();
        var headers = response.getHeaders();
        var existingName = findHeaderName(headers, varyName);
        if (existingName == null) {
            response.header(varyName, acceptEncoding);
            return;
        }

        var existing = headers.get(existingName);
        if (existing == null || existing.isBlank()) {
            response.header(existingName, acceptEncoding);
            return;
        }
        for (var token : existing.split(",")) {
            var trimmed = token.trim();
            if (trimmed.equals("*") || trimmed.equalsIgnoreCase(acceptEncoding)) {
                return;
            }
        }
        response.header(existingName, existing + ", " + acceptEncoding);
    }

    private static Response copyWithBody(Response original, byte[] body) {
        var copy = new Response(body, original.getStatusCode(), null, original.getCharset());
        for (var entry : original.getAllHeaders().entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(HttpHeader.CONTENT_LENGTH.getValue())) {
                for (var value : entry.getValue()) {
                    copy.addHeader(entry.getKey(), value);
                }
            }
        }
        return copy;
    }

    private static String findHeaderName(Map<String, String> headers, String expectedName) {
        for (var name : headers.keySet()) {
            if (name.equalsIgnoreCase(expectedName)) {
                return name;
            }
        }
        return null;
    }

    private static byte[] compress(byte[] data) throws IOException {
        var outputStream = new ByteArrayOutputStream();
        try (var gzipStream = new GZIPOutputStream(outputStream)) {
            gzipStream.write(data);
        }
        return outputStream.toByteArray();
    }
}
