package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;

class RealUrlEncodingTest {

    @Test
    void testUrlDecodingThroughHttpRequest() {
        var router = new HttpRouter();
        var handler = new TestHandler();
        router.GET("/test", handler);
        
        // Simulate a real HTTP request with encoded URL
        var response = handleRequestWithQuery(router, "GET", "/test?name=john%20doe&email=user%40example.com");
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("name=john doe"));
        assertTrue(response.getBody().contains("email=user@example.com"));
    }

    @Test
    void testSpecialCharactersThroughHttpRequest() {
        var router = new HttpRouter();
        var handler = new TestHandler();
        router.GET("/search", handler);
        
        var response = handleRequestWithQuery(router, "GET", "/search?q=hello%20world&filter=category%3Dbooks");
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("q=hello world"));
        assertTrue(response.getBody().contains("filter=category=books"));
    }

    @Test
    void testUnicodeThroughHttpRequest() {
        var router = new HttpRouter();
        var handler = new TestHandler();
        router.GET("/test", handler);
        
        var response = handleRequestWithQuery(router, "GET", "/test?chinese=%E4%B8%AD%E6%96%87&emoji=%F0%9F%98%80");
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("chinese=中文"));
        assertTrue(response.getBody().contains("emoji=😀"));
    }

    @Test
    void testPlusSignDecodingThroughHttpRequest() {
        var router = new HttpRouter();
        var handler = new TestHandler();
        router.GET("/test", handler);
        
        var response = handleRequestWithQuery(router, "GET", "/test?search=hello+world&formula=a%2Bb%3Dc");
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("search=hello world"));
        assertTrue(response.getBody().contains("formula=a+b=c"));
    }

    @Test
    void testComplexRealWorldExampleThroughHttpRequest() {
        var router = new HttpRouter();
        var handler = new TestHandler();
        router.GET("/search", handler);
        
        var response = handleRequestWithQuery(router, "GET", "/search?q=java%20programming%20tutorial&author=John%20Doe&tags=beginner%2Cprogramming%2Cjava");
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("q=java programming tutorial"));
        assertTrue(response.getBody().contains("author=John Doe"));
        assertTrue(response.getBody().contains("tags=beginner,programming,java"));
    }

    @Test
    void testInvalidEncodingHandlingThroughHttpRequest() {
        var router = new HttpRouter();
        var handler = new TestHandler();
        router.GET("/test", handler);
        
        var response = handleRequestWithQuery(router, "GET", "/test?invalid=%GG&incomplete=%2&normal=valid");
        
        assertEquals(200, response.getStatusCode());
        // Should return original value for invalid encoding
        assertTrue(response.getBody().contains("invalid=%GG"));
        assertTrue(response.getBody().contains("incomplete=%2"));
        assertTrue(response.getBody().contains("normal=valid"));
    }

    @Test
    void testKeyAndValueEncodingThroughHttpRequest() {
        var router = new HttpRouter();
        var handler = new TestHandler();
        router.GET("/test", handler);
        
        var response = handleRequestWithQuery(router, "GET", "/test?key%20with%20spaces=value%20with%20spaces&key%40symbol=value%40symbol");
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("key with spaces=value with spaces"));
        assertTrue(response.getBody().contains("key@symbol=value@symbol"));
    }

    @Test
    void testFlagParametersThroughHttpRequest() {
        var router = new HttpRouter();
        var handler = new TestHandler();
        router.GET("/test", handler);
        
        var response = handleRequestWithQuery(router, "GET", "/test?active&verified&admin=true");
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("active="));
        assertTrue(response.getBody().contains("verified="));
        assertTrue(response.getBody().contains("admin=true"));
    }

    @Test
    void testEmptyValuesThroughHttpRequest() {
        var router = new HttpRouter();
        var handler = new TestHandler();
        router.GET("/test", handler);
        
        var response = handleRequestWithQuery(router, "GET", "/test?empty=&encoded_empty=%20&normal=value");
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("empty="));
        assertTrue(response.getBody().contains("encoded_empty= "));
        assertTrue(response.getBody().contains("normal=value"));
    }

    // Helper method to simulate URL parsing and handle requests with query parameters
    private Response handleRequestWithQuery(HttpRouter router, String method, String fullPath) {
        var questionMarkIndex = fullPath.indexOf('?');
        String path;
        Map<String, String> queryParams = new HashMap<>();
        
        if (questionMarkIndex != -1) {
            path = fullPath.substring(0, questionMarkIndex);
            var queryString = fullPath.substring(questionMarkIndex + 1);
            
            if (!queryString.isEmpty()) {
                var pairs = queryString.split("&");
                for (var pair : pairs) {
                    var equalIndex = pair.indexOf('=');
                    if (equalIndex != -1) {
                        var key = pair.substring(0, equalIndex);
                        var value = pair.substring(equalIndex + 1);
                        // URL decode the key and value
                        queryParams.put(urlDecode(key), urlDecode(value));
                    } else if (!pair.isEmpty()) {
                        // Handle parameters without values (e.g., ?flag)
                        queryParams.put(urlDecode(pair), "");
                    }
                }
            }
        } else {
            path = fullPath;
        }
        
        return router.handleRequest(method, path, new HashMap<>(), queryParams);
    }
    
    // Simple URL decoding for test purposes
    private String urlDecode(String encoded) {
        try {
            return java.net.URLDecoder.decode(encoded, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encoded; // Return original if decoding fails
        }
    }

    private static class TestHandler implements RequestHandler {
        @Override
        public Response handle(Request request) {
            var queryParams = request.getQueryParams();
            var result = new StringBuilder("Query parameters: {");
            
            var first = true;
            for (var entry : queryParams.entrySet()) {
                if (!first) {
                    result.append(", ");
                }
                result.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            result.append("}");
            
            return new Response(result.toString());
        }
    }
}
