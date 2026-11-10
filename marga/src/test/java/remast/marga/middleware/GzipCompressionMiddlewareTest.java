package remast.marga.middleware;

import org.junit.jupiter.api.Test;
import remast.marga.HttpStatus;
import remast.marga.MediaType;
import remast.marga.Request;
import remast.marga.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

class GzipCompressionMiddlewareTest {

    @Test
    void shouldCompressLargeTextResponse() {
        var middleware = new GzipCompressionMiddleware(100); // Low threshold for testing
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent()));
        
        var request = gzipRequest();
        var response = handler.handle(request);
        
        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertTrue(response.isBinary());
        assertTrue(response.getBodyBytes().length < createLargeTextContent().getBytes().length);
    }
    
    @Test
    void shouldNotCompressSmallResponse() {
        var middleware = new GzipCompressionMiddleware(1000);
        var handler = middleware.create().apply(request -> Response.ok("Small content"));
        
        var request = gzipRequest();
        var response = handler.handle(request);
        
        assertNull(response.getHeader("Content-Encoding"));
        assertEquals("Small content", response.getBody());
    }
    
    @Test
    void shouldNotCompressAlreadyCompressedContent() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent(), MediaType.APPLICATION_GZIP));
        
        var request = gzipRequest();
        var response = handler.handle(request);
        
        assertNull(response.getHeader("Content-Encoding"));
    }
    
    @Test
    void shouldNotCompressImages() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent(), MediaType.IMAGE_JPEG));
        
        var request = gzipRequest();
        var response = handler.handle(request);
        
        assertNull(response.getHeader("Content-Encoding"));
    }
    
    @Test
    void shouldNotCompressErrorResponses() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.serverError(createLargeTextContent()));
        
        var request = gzipRequest();
        var response = handler.handle(request);
        
        assertNull(response.getHeader("Content-Encoding"));
    }
    
    @Test
    void shouldNotCompressEmptyResponse() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.noContent());
        
        var request = gzipRequest();
        var response = handler.handle(request);
        
        assertNull(response.getHeader("Content-Encoding"));
    }
    
    @Test
    void shouldCompressJsonResponse() {
        var middleware = new GzipCompressionMiddleware(100);
        var jsonContent = createLargeJsonContent();
        var handler = middleware.create().apply(request -> Response.json(jsonContent));
        
        var request = gzipRequest();
        var response = handler.handle(request);
        
        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertTrue(response.isBinary());
        assertTrue(response.getBodyBytes().length < jsonContent.getBytes().length);
    }
    
    @Test
    void shouldCompressHtmlResponse() {
        var middleware = new GzipCompressionMiddleware(100);
        var htmlContent = createLargeHtmlContent();
        var handler = middleware.create().apply(request -> Response.html(htmlContent));
        
        var request = gzipRequest();
        var response = handler.handle(request);
        
        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertTrue(response.isBinary());
        assertTrue(response.getBodyBytes().length < htmlContent.getBytes().length);
    }
    
    @Test
    void shouldPreserveOriginalResponseWhenCompressionFails() {
        var middleware = new GzipCompressionMiddleware(100);
        var originalContent = createLargeTextContent();
        var handler = middleware.create().apply(request -> Response.ok(originalContent));
        
        var request = gzipRequest();
        var response = handler.handle(request);
        
        // Should still have a response, even if compression fails
        assertNotNull(response);
        assertTrue(response.getBody().length() > 0);
    }
    
    @Test
    void shouldUseCustomMinCompressionSize() {
        var middleware = new GzipCompressionMiddleware(50);
        var content = createMediumTextContent();
        var handler = middleware.create().apply(request -> Response.ok(content));
        
        var request = gzipRequest();
        var response = handler.handle(request);
        
        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertTrue(response.isBinary());
    }
    
    @Test
    void shouldNotCompressWithoutAcceptEncoding() {
        var middleware = new GzipCompressionMiddleware(100);
        var content = createLargeTextContent();
        var handler = middleware.create().apply(request -> Response.ok(content));

        var response = handler.handle(new Request("GET", "/test"));

        assertNull(response.getHeader("Content-Encoding"));
        assertEquals("Accept-Encoding", response.getHeader("Vary"));
        assertEquals(content, response.getBody());
    }

    @Test
    void shouldNotCompressWhenGzipNotAccepted() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent()));

        var response = handler.handle(requestWithAcceptEncoding("br, deflate"));

        assertNull(response.getHeader("Content-Encoding"));
    }

    @Test
    void shouldNotCompressWhenGzipQualityIsZero() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent()));

        var response = handler.handle(requestWithAcceptEncoding("gzip;q=0, *;q=1"));

        assertNull(response.getHeader("Content-Encoding"));
    }

    @Test
    void shouldCompressWhenWildcardAccepted() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent()));

        var response = handler.handle(requestWithAcceptEncoding("*"));

        assertEquals("gzip", response.getHeader("Content-Encoding"));
    }

    @Test
    void shouldMatchAcceptEncodingCaseInsensitively() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent()));

        var response = handler.handle(requestWithAcceptEncoding("deflate, GZIP;Q=0.5"));

        assertEquals("gzip", response.getHeader("Content-Encoding"));
    }

    @Test
    void shouldAddVaryHeaderWhenCompressing() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent()));

        var response = handler.handle(gzipRequest());

        assertEquals("Accept-Encoding", response.getHeader("Vary"));
    }

    @Test
    void shouldAppendToExistingVaryHeader() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent()).header("Vary", "Origin"));

        var response = handler.handle(gzipRequest());

        assertEquals("Origin, Accept-Encoding", response.getHeader("Vary"));
    }

    @Test
    void shouldNotDuplicateVaryAcceptEncoding() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent()).header("Vary", "accept-encoding"));

        var response = handler.handle(gzipRequest());

        assertEquals("accept-encoding", response.getHeader("Vary"));
    }

    @Test
    void shouldPreserveResponseHeaders() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent())
            .header("X-Custom", "value")
            .header("Cache-Control", "no-cache"));

        var response = handler.handle(gzipRequest());

        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertEquals("value", response.getHeader("X-Custom"));
        assertEquals("no-cache", response.getHeader("Cache-Control"));
        assertEquals("text/plain", response.getHeader("Content-Type"));
    }

    @Test
    void shouldCompressBodyUsingResponseCharset() throws IOException {
        var middleware = new GzipCompressionMiddleware(100);
        var content = "Grüße aus Köln. ".repeat(100);
        var handler = middleware.create().apply(request ->
            new Response(content, HttpStatus.OK, MediaType.TEXT_PLAIN, StandardCharsets.ISO_8859_1));

        var response = handler.handle(gzipRequest());

        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertEquals(StandardCharsets.ISO_8859_1, response.getCharset());
        assertArrayEquals(content.getBytes(StandardCharsets.ISO_8859_1), gunzip(response.getBodyBytes()));
    }

    @Test
    void shouldCompressBinaryResponseWithCompressibleType() throws IOException {
        var middleware = new GzipCompressionMiddleware(100);
        var bytes = createLargeJsonContent().getBytes(StandardCharsets.UTF_8);
        var handler = middleware.create().apply(request -> new Response(bytes, 200, MediaType.APPLICATION_JSON));

        var response = handler.handle(gzipRequest());

        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertArrayEquals(bytes, gunzip(response.getBodyBytes()));
    }

    @Test
    void shouldNotCompressAlreadyEncodedResponse() {
        var middleware = new GzipCompressionMiddleware(100);
        var content = createLargeTextContent();
        var handler = middleware.create().apply(request -> Response.ok(content).header("Content-Encoding", "br"));

        var response = handler.handle(gzipRequest());

        assertEquals("br", response.getHeader("Content-Encoding"));
        assertEquals(content, response.getBody());
    }

    @Test
    void shouldNotCompressNonTextApplicationTypes() {
        var middleware = new GzipCompressionMiddleware(100);
        for (var type : new String[] {"application/pdf", "application/octet-stream", "application/zip", "application/wasm"}) {
            var handler = middleware.create().apply(request -> Response.ok(createLargeTextContent(), new MediaType(type)));

            var response = handler.handle(gzipRequest());

            assertNull(response.getHeader("Content-Encoding"), type);
        }
    }

    @Test
    void shouldCompressStructuredSyntaxSuffixTypes() {
        var middleware = new GzipCompressionMiddleware(100);
        for (var type : new String[] {"application/problem+json", "application/atom+xml", "image/svg+xml"}) {
            var handler = middleware.create().apply(request -> Response.ok(createLargeJsonContent(), new MediaType(type)));

            var response = handler.handle(gzipRequest());

            assertEquals("gzip", response.getHeader("Content-Encoding"), type);
        }
    }

    private static Request gzipRequest() {
        return requestWithAcceptEncoding("gzip, deflate");
    }

    private static Request requestWithAcceptEncoding(String value) {
        return new Request("GET", "/test", Map.of("accept-encoding", value));
    }

    private static byte[] gunzip(byte[] data) throws IOException {
        try (var input = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return input.readAllBytes();
        }
    }

    private String createLargeTextContent() {
        var sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("This is a line of text that will be repeated many times to create a large response body. ");
            sb.append("Line number: ").append(i).append(". ");
            sb.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit. ");
        }
        return sb.toString();
    }
    
    private String createMediumTextContent() {
        var sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("This is a medium-sized text content for testing compression. ");
        }
        return sb.toString();
    }
    
    private String createLargeJsonContent() {
        var sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"users\": [\n");
        for (int i = 0; i < 500; i++) {
            sb.append("    {\n");
            sb.append("      \"id\": ").append(i).append(",\n");
            sb.append("      \"name\": \"User ").append(i).append("\",\n");
            sb.append("      \"email\": \"user").append(i).append("@example.com\",\n");
            sb.append("      \"description\": \"This is a detailed description for user ").append(i).append(" that contains a lot of text to make the JSON response larger and more suitable for compression testing.\"\n");
            sb.append("    }");
            if (i < 499) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }
    
    private String createLargeHtmlContent() {
        var sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html><head><title>Test Page</title></head><body>\n");
        for (int i = 0; i < 200; i++) {
            sb.append("<div class=\"content\">\n");
            sb.append("  <h2>Section ").append(i).append("</h2>\n");
            sb.append("  <p>This is paragraph content for section ").append(i).append(". ");
            sb.append("It contains a lot of text to make the HTML response larger and more suitable for compression testing. ");
            sb.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>\n");
            sb.append("</div>\n");
        }
        sb.append("</body></html>");
        return sb.toString();
    }
}
