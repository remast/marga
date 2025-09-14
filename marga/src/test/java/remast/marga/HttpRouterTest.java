package remast.marga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class HttpRouterTest {

    private HttpRouter router;
    private TestRequestHandler testHandler;
    private TestRequestHandler userHandler;
    private TestRequestHandler postHandler;

    @BeforeEach
    void setUp() {
        router = new HttpRouter();
        testHandler = new TestRequestHandler("Test response");
        userHandler = new TestRequestHandler("User response");
        postHandler = new TestRequestHandler("Post response");
    }

    @Test
    void addExactRoute() {
        router.addRoute("GET", "/test", testHandler);
        
        var response = router.handleRequest("GET", "/test");
        assertEquals("Test response", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void addExactRouteWithDescription() {
        router.addRoute("GET", "/test", testHandler, "Test endpoint");
        
        var response = router.handleRequest("GET", "/test");
        assertEquals("Test response", response.getBody());
    }

    @Test
    void addRouteWithDefaultMethod() {
        router.addRoute("/test", testHandler);
        
        var response = router.handleRequest("GET", "/test");
        assertEquals("Test response", response.getBody());
    }

    @Test
    void addRouteWithDefaultMethodAndDescription() {
        router.addRoute("/test", testHandler, "Test endpoint");
        
        var response = router.handleRequest("GET", "/test");
        assertEquals("Test response", response.getBody());
    }

    @Test
    void addParameterizedRoute() {
        router.addParameterizedRoute("GET", "/users/${id}", userHandler);
        
        var response = router.handleRequest("GET", "/users/123");
        assertEquals("User response", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void addParameterizedRouteWithDescription() {
        router.addParameterizedRoute("GET", "/users/${id}", userHandler, "Get user by ID");
        
        var response = router.handleRequest("GET", "/users/123");
        assertEquals("User response", response.getBody());
    }

    @Test
    void addRouteWithParameterPattern() {
        router.addRoute("GET", "/users/${id}", userHandler);
        
        var response = router.handleRequest("GET", "/users/123");
        assertEquals("User response", response.getBody());
    }

    @Test
    void exactRouteTakesPrecedenceOverParameterized() {
        router.addRoute("GET", "/users/123", testHandler);
        router.addRoute("GET", "/users/${id}", userHandler);
        
        var response = router.handleRequest("GET", "/users/123");
        assertEquals("Test response", response.getBody());
    }

    @Test
    void longestParameterizedRouteWins() {
        router.addRoute("GET", "/users/${id}", userHandler);
        router.addRoute("GET", "/users/${id}/posts/${postId}", postHandler);
        
        var response = router.handleRequest("GET", "/users/123/posts/456");
        assertEquals("Post response", response.getBody());
    }

    @Test
    void pathParametersAreExtracted() {
        router.addRoute("GET", "/users/${id}/posts/${postId}", postHandler);
        
        var response = router.handleRequest("GET", "/users/123/posts/456");
        assertEquals("Post response", response.getBody());
    }

    @Test
    void notFoundForNonExistentRoute() {
        var response = router.handleRequest("GET", "/nonexistent");
        
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("404 - Not Found"));
    }

    @Test
    void notFoundForWrongMethod() {
        router.addRoute("GET", "/test", testHandler);
        
        var response = router.handleRequest("POST", "/test");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void multipleRoutes() {
        router.addRoute("GET", "/users", userHandler);
        router.addRoute("POST", "/users", postHandler);
        router.addRoute("GET", "/posts", postHandler);
        
        var getUsersResponse = router.handleRequest("GET", "/users");
        var postUsersResponse = router.handleRequest("POST", "/users");
        var getPostsResponse = router.handleRequest("GET", "/posts");
        
        assertEquals("User response", getUsersResponse.getBody());
        assertEquals("Post response", postUsersResponse.getBody());
        assertEquals("Post response", getPostsResponse.getBody());
    }

    @Test
    void getRoutes() {
        router.addRoute("GET", "/users", userHandler);
        router.addRoute("POST", "/posts", postHandler);
        router.addRoute("GET", "/users/${id}", userHandler);
        
        var routes = router.getRoutes();
        
        assertTrue(routes.containsKey("GET /users"));
        assertTrue(routes.containsKey("POST /posts"));
        assertTrue(routes.containsKey("GET /users/${id}"));
        assertEquals(3, routes.size());
    }

    @Test
    void complexRoutingScenario() {
        router.addRoute("GET", "/api/users", userHandler);
        router.addRoute("GET", "/api/users/123", testHandler);
        router.addRoute("GET", "/api/users/${id}", userHandler);
        router.addRoute("GET", "/api/users/${id}/posts", postHandler);
        router.addRoute("GET", "/api/users/${id}/posts/${postId}", testHandler);
        
        assertEquals("User response", router.handleRequest("GET", "/api/users").getBody());
        assertEquals("Test response", router.handleRequest("GET", "/api/users/123").getBody());
        assertEquals("User response", router.handleRequest("GET", "/api/users/456").getBody());
        assertEquals("Post response", router.handleRequest("GET", "/api/users/456/posts").getBody());
        assertEquals("Test response", router.handleRequest("GET", "/api/users/456/posts/789").getBody());
    }

    @Test
    void routeWithSpecialCharacters() {
        router.addRoute("GET", "/files/${filename}", testHandler);
        
        var response = router.handleRequest("GET", "/files/document.pdf");
        assertEquals("Test response", response.getBody());
    }

    @Test
    void emptyPath() {
        router.addRoute("GET", "/", testHandler);
        
        var response = router.handleRequest("GET", "/");
        assertEquals("Test response", response.getBody());
    }

    @Test
    void shutdown() {
        assertDoesNotThrow(() -> router.shutdown());
    }

    // Tests for HTTP method-specific convenience methods
    @Test
    void getMethod() {
        router.GET("/test", testHandler);
        
        var response = router.handleRequest("GET", "/test");
        assertEquals("Test response", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void getMethodWithDescription() {
        router.GET("/test", testHandler, "Test endpoint");
        
        var response = router.handleRequest("GET", "/test");
        assertEquals("Test response", response.getBody());
    }

    @Test
    void postMethod() {
        router.POST("/users", userHandler);
        
        var response = router.handleRequest("POST", "/users");
        assertEquals("User response", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void postMethodWithDescription() {
        router.POST("/users", userHandler, "Create user");
        
        var response = router.handleRequest("POST", "/users");
        assertEquals("User response", response.getBody());
    }

    @Test
    void putMethod() {
        router.PUT("/users/123", userHandler);
        
        var response = router.handleRequest("PUT", "/users/123");
        assertEquals("User response", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void putMethodWithDescription() {
        router.PUT("/users/123", userHandler, "Update user");
        
        var response = router.handleRequest("PUT", "/users/123");
        assertEquals("User response", response.getBody());
    }

    @Test
    void deleteMethod() {
        router.DELETE("/users/123", userHandler);
        
        var response = router.handleRequest("DELETE", "/users/123");
        assertEquals("User response", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void deleteMethodWithDescription() {
        router.DELETE("/users/123", userHandler, "Delete user");
        
        var response = router.handleRequest("DELETE", "/users/123");
        assertEquals("User response", response.getBody());
    }

    @Test
    void patchMethod() {
        router.PATCH("/users/123", userHandler);
        
        var response = router.handleRequest("PATCH", "/users/123");
        assertEquals("User response", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void patchMethodWithDescription() {
        router.PATCH("/users/123", userHandler, "Partially update user");
        
        var response = router.handleRequest("PATCH", "/users/123");
        assertEquals("User response", response.getBody());
    }

    @Test
    void headMethod() {
        router.HEAD("/test", testHandler);
        
        var response = router.handleRequest("HEAD", "/test");
        assertEquals("Test response", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void headMethodWithDescription() {
        router.HEAD("/test", testHandler, "Get headers only");
        
        var response = router.handleRequest("HEAD", "/test");
        assertEquals("Test response", response.getBody());
    }

    @Test
    void optionsMethod() {
        router.OPTIONS("/test", testHandler);
        
        var response = router.handleRequest("OPTIONS", "/test");
        assertEquals("Test response", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void optionsMethodWithDescription() {
        router.OPTIONS("/test", testHandler, "Get allowed methods");
        
        var response = router.handleRequest("OPTIONS", "/test");
        assertEquals("Test response", response.getBody());
    }

    @Test
    void methodSpecificRoutesWithParameters() {
        router.GET("/users/${id}", userHandler, "Get user by ID");
        router.PUT("/users/${id}", userHandler, "Update user by ID");
        router.DELETE("/users/${id}", userHandler, "Delete user by ID");
        
        assertEquals("User response", router.handleRequest("GET", "/users/123").getBody());
        assertEquals("User response", router.handleRequest("PUT", "/users/123").getBody());
        assertEquals("User response", router.handleRequest("DELETE", "/users/123").getBody());
    }

    @Test
    void mixedMethodSyntax() {
        // Mix of old and new syntax
        router.addRoute("GET", "/old", testHandler, "Old syntax");
        router.GET("/new", userHandler, "New syntax");
        
        assertEquals("Test response", router.handleRequest("GET", "/old").getBody());
        assertEquals("User response", router.handleRequest("GET", "/new").getBody());
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
