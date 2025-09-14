package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    @Test
    void routeWithHandlerAndDescription() {
        var handler = new TestRequestHandler("test response");
        var route = new Route(handler, "Test route");
        
        assertEquals(handler, route.getHandler());
        assertEquals("Test route", route.getDescription());
        assertEquals("Test route", route.getDescriptionOrDefault());
        assertNull(route.getPattern());
    }

    @Test
    void routeWithHandlerOnly() {
        var handler = new TestRequestHandler("test response");
        var route = new Route(handler);
        
        assertEquals(handler, route.getHandler());
        assertNull(route.getDescription());
        assertEquals("TestRequestHandler", route.getDescriptionOrDefault());
        assertNull(route.getPattern());
    }

    @Test
    void routeWithPattern() {
        var handler = new TestRequestHandler("test response");
        var route = new Route(handler, "Test route", "/users/${id}");
        
        assertEquals(handler, route.getHandler());
        assertEquals("Test route", route.getDescription());
        assertEquals("/users/${id}", route.getPattern());
        assertTrue(route.matches("/users/123"));
        assertFalse(route.matches("/users"));
    }

    @Test
    void routeWithoutPattern() {
        var handler = new TestRequestHandler("test response");
        var route = new Route(handler, "Test route", null);
        
        assertFalse(route.matches("/users/123"));
        assertFalse(route.matches("/users"));
    }

    @Test
    void extractParameters() {
        var handler = new TestRequestHandler("test response");
        var route = new Route(handler, "Test route", "/users/${id}/posts/${postId}");
        var request = new Request("GET", "/users/123/posts/456");
        
        route.extractParameters("/users/123/posts/456", request);
        
        assertEquals("123", request.pathParam("id"));
        assertEquals("456", request.pathParam("postId"));
    }

    @Test
    void extractParametersWithNoPattern() {
        var handler = new TestRequestHandler("test response");
        var route = new Route(handler, "Test route", null);
        var request = new Request("GET", "/users/123");
        
        route.extractParameters("/users/123", request);
        
        assertTrue(request.getPathParams().isEmpty());
    }

    @Test
    void descriptionOrDefaultWithNullDescription() {
        var handler = new TestRequestHandler("test response");
        var route = new Route(handler, null);
        
        assertEquals("TestRequestHandler", route.getDescriptionOrDefault());
    }

    @Test
    void descriptionOrDefaultWithEmptyDescription() {
        var handler = new TestRequestHandler("test response");
        var route = new Route(handler, "");
        
        assertEquals("", route.getDescriptionOrDefault());
    }

    private static class TestRequestHandler implements RequestHandler {
        private final String responseBody;

        public TestRequestHandler(String responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public Response handle(Request request) {
            return new Response(responseBody);
        }
    }
}
