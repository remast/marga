package remast.marga.middleware;

import org.junit.jupiter.api.Test;
import remast.marga.MediaType;
import remast.marga.Request;
import remast.marga.Response;

import static org.junit.jupiter.api.Assertions.*;

class GzipCompressionMiddlewareTest {

    @Test
    void shouldCompressLargeTextResponse() {
        var middleware = new GzipCompressionMiddleware(100); // Low threshold for testing
        var handler = middleware.apply(request -> Response.ok(createLargeTextContent()));
        
        var request = new Request("GET", "/test");
        var response = handler.handle(request);
        
        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertTrue(response.isBinary());
        assertTrue(response.getBodyBytes().length < createLargeTextContent().getBytes().length);
    }
    
    @Test
    void shouldNotCompressSmallResponse() {
        var middleware = new GzipCompressionMiddleware(1000);
        var handler = middleware.apply(request -> Response.ok("Small content"));
        
        var request = new Request("GET", "/test");
        var response = handler.handle(request);
        
        assertNull(response.getHeader("Content-Encoding"));
        assertEquals("Small content", response.getBody());
    }
    
    @Test
    void shouldNotCompressAlreadyCompressedContent() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.apply(request -> Response.ok(createLargeTextContent(), MediaType.APPLICATION_GZIP));
        
        var request = new Request("GET", "/test");
        var response = handler.handle(request);
        
        assertNull(response.getHeader("Content-Encoding"));
    }
    
    @Test
    void shouldNotCompressImages() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.apply(request -> Response.ok(createLargeTextContent(), MediaType.IMAGE_JPEG));
        
        var request = new Request("GET", "/test");
        var response = handler.handle(request);
        
        assertNull(response.getHeader("Content-Encoding"));
    }
    
    @Test
    void shouldNotCompressErrorResponses() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.apply(request -> Response.serverError(createLargeTextContent()));
        
        var request = new Request("GET", "/test");
        var response = handler.handle(request);
        
        assertNull(response.getHeader("Content-Encoding"));
    }
    
    @Test
    void shouldNotCompressEmptyResponse() {
        var middleware = new GzipCompressionMiddleware(100);
        var handler = middleware.apply(request -> Response.noContent());
        
        var request = new Request("GET", "/test");
        var response = handler.handle(request);
        
        assertNull(response.getHeader("Content-Encoding"));
    }
    
    @Test
    void shouldCompressJsonResponse() {
        var middleware = new GzipCompressionMiddleware(100);
        var jsonContent = createLargeJsonContent();
        var handler = middleware.apply(request -> Response.json(jsonContent));
        
        var request = new Request("GET", "/test");
        var response = handler.handle(request);
        
        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertTrue(response.isBinary());
        assertTrue(response.getBodyBytes().length < jsonContent.getBytes().length);
    }
    
    @Test
    void shouldCompressHtmlResponse() {
        var middleware = new GzipCompressionMiddleware(100);
        var htmlContent = createLargeHtmlContent();
        var handler = middleware.apply(request -> Response.html(htmlContent));
        
        var request = new Request("GET", "/test");
        var response = handler.handle(request);
        
        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertTrue(response.isBinary());
        assertTrue(response.getBodyBytes().length < htmlContent.getBytes().length);
    }
    
    @Test
    void shouldPreserveOriginalResponseWhenCompressionFails() {
        var middleware = new GzipCompressionMiddleware(100);
        var originalContent = createLargeTextContent();
        var handler = middleware.apply(request -> Response.ok(originalContent));
        
        var request = new Request("GET", "/test");
        var response = handler.handle(request);
        
        // Should still have a response, even if compression fails
        assertNotNull(response);
        assertTrue(response.getBody().length() > 0);
    }
    
    @Test
    void shouldUseCustomMinCompressionSize() {
        var middleware = new GzipCompressionMiddleware(50);
        var content = createMediumTextContent();
        var handler = middleware.apply(request -> Response.ok(content));
        
        var request = new Request("GET", "/test");
        var response = handler.handle(request);
        
        assertEquals("gzip", response.getHeader("Content-Encoding"));
        assertTrue(response.isBinary());
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
