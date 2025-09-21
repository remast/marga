package remast.marga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;


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

    @Test
    void queryParametersWithExactRoute() {
        var queryHandler = new QueryParamHandler();
        router.GET("/search", queryHandler, "Search with query parameters");
        
        var headers = new HashMap<String, String>();
        var queryParams = new HashMap<String, String>();
        queryParams.put("q", "java");
        queryParams.put("limit", "10");
        
        var response = router.handleRequest("GET", "/search", headers, queryParams);
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("q=java"));
        assertTrue(response.getBody().contains("limit=10"));
    }

    @Test
    void queryParametersWithParameterizedRoute() {
        var queryHandler = new QueryParamHandler();
        router.GET("/users/${id}/posts", queryHandler, "Get user posts with query parameters");
        
        var headers = new HashMap<String, String>();
        var queryParams = new HashMap<String, String>();
        queryParams.put("page", "1");
        queryParams.put("size", "20");
        queryParams.put("sort", "date");
        
        var response = router.handleRequest("GET", "/users/123/posts", headers, queryParams);
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("page=1"));
        assertTrue(response.getBody().contains("size=20"));
        assertTrue(response.getBody().contains("sort=date"));
    }

    @Test
    void queryParametersWithSpecialCharacters() {
        var queryHandler = new QueryParamHandler();
        router.GET("/api/search", queryHandler, "Search with special characters");
        
        var headers = new HashMap<String, String>();
        var queryParams = new HashMap<String, String>();
        queryParams.put("search", "hello world");
        queryParams.put("filter", "category=books&price<50");
        queryParams.put("encoded", "test%20value");
        
        var response = router.handleRequest("GET", "/api/search", headers, queryParams);
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("search=hello world"));
        assertTrue(response.getBody().contains("filter=category=books&price<50"));
        assertTrue(response.getBody().contains("encoded=test%20value"));
    }

    @Test
    void queryParametersWithFlagValues() {
        var queryHandler = new QueryParamHandler();
        router.GET("/api/users", queryHandler, "Get users with flags");
        
        var headers = new HashMap<String, String>();
        var queryParams = new HashMap<String, String>();
        queryParams.put("active", ""); // Flag parameter
        queryParams.put("verified", ""); // Another flag
        queryParams.put("admin", "true");
        
        var response = router.handleRequest("GET", "/api/users", headers, queryParams);
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("active="));
        assertTrue(response.getBody().contains("verified="));
        assertTrue(response.getBody().contains("admin=true"));
    }

    @Test
    void queryParametersWithEmptyValues() {
        var queryHandler = new QueryParamHandler();
        router.GET("/api/test", queryHandler, "Test with empty values");
        
        var headers = new HashMap<String, String>();
        var queryParams = new HashMap<String, String>();
        queryParams.put("empty", "");
        queryParams.put("null", null);
        queryParams.put("valid", "value");
        
        var response = router.handleRequest("GET", "/api/test", headers, queryParams);
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("empty="));
        assertTrue(response.getBody().contains("valid=value"));
    }

    @Test
    void noQueryParameters() {
        var queryHandler = new QueryParamHandler();
        router.GET("/api/simple", queryHandler, "Simple endpoint without query params");
        
        var headers = new HashMap<String, String>();
        var queryParams = new HashMap<String, String>();
        
        var response = router.handleRequest("GET", "/api/simple", headers, queryParams);
        
        assertEquals(200, response.getStatusCode());
        assertEquals("Query parameters: {}", response.getBody());
    }

    @Test
    void queryParametersWithPathParameters() {
        var queryHandler = new QueryParamHandler();
        router.GET("/users/${userId}/posts/${postId}", queryHandler, "Get specific post with query params");
        
        var headers = new HashMap<String, String>();
        var queryParams = new HashMap<String, String>();
        queryParams.put("include", "comments");
        queryParams.put("format", "json");
        
        var response = router.handleRequest("GET", "/users/123/posts/456", headers, queryParams);
        
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("include=comments"));
        assertTrue(response.getBody().contains("format=json"));
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

    private static class QueryParamHandler implements RequestHandler {
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
