package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;

class RequestTest {

    @Test
    void constructor() {
        var request = new Request("GET", "/test");
        
        assertEquals("GET", request.getMethod());
        assertEquals("/test", request.getPath());
        assertTrue(request.getPathParams().isEmpty());
    }

    @Test
    void pathParamOperations() {
        var request = new Request("POST", "/users/123");
        
        request.addPathParam("id", "123");
        request.addPathParam("name", "john");
        
        assertEquals("123", request.pathParam("id"));
        assertEquals("john", request.pathParam("name"));
        assertNull(request.pathParam("nonexistent"));
        
        var pathParams = request.getPathParams();
        assertEquals(2, pathParams.size());
        assertEquals("123", pathParams.get("id"));
        assertEquals("john", pathParams.get("name"));
    }

    @Test
    void pathParamsAreImmutable() {
        var request = new Request("GET", "/test");
        request.addPathParam("key", "value");
        
        var pathParams = request.getPathParams();
        pathParams.put("newKey", "newValue");
        
        assertNull(request.pathParam("newKey"));
        assertEquals("value", request.pathParam("key"));
    }

    @Test
    void overwritePathParam() {
        var request = new Request("GET", "/test");
        
        request.addPathParam("id", "123");
        assertEquals("123", request.pathParam("id"));
        
        request.addPathParam("id", "456");
        assertEquals("456", request.pathParam("id"));
    }

    @Test
    void nullPathParam() {
        var request = new Request("GET", "/test");
        
        request.addPathParam("key", null);
        assertNull(request.pathParam("key"));
    }

    @Test
    void constructorWithHeaders() {
        var headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("User-Agent", "TestClient");
        
        var request = new Request("POST", "/api", headers);
        
        assertEquals("POST", request.getMethod());
        assertEquals("/api", request.getPath());
        assertEquals("application/json", request.header("Content-Type"));
        assertEquals("TestClient", request.header("User-Agent"));
        assertTrue(request.getHeaders().isEmpty() == false);
    }

    @Test
    void headerOperations() {
        var headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer token123");
        
        var request = new Request("GET", "/test", headers);
        
        assertEquals("application/json", request.header("Content-Type"));
        assertEquals("Bearer token123", request.header("Authorization"));
        assertNull(request.header("NonExistent"));
        
        assertTrue(request.hasHeader("Content-Type"));
        assertTrue(request.hasHeader("Authorization"));
        assertFalse(request.hasHeader("NonExistent"));
    }

    @Test
    void headerWithEnum() {
        var headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("User-Agent", "TestClient");
        
        var request = new Request("GET", "/test", headers);
        
        assertEquals("application/json", request.header(HttpHeader.CONTENT_TYPE));
        assertEquals("TestClient", request.header(HttpHeader.USER_AGENT));
        assertNull(request.header(HttpHeader.AUTHORIZATION));
        
        assertTrue(request.hasHeader(HttpHeader.CONTENT_TYPE));
        assertTrue(request.hasHeader(HttpHeader.USER_AGENT));
        assertFalse(request.hasHeader(HttpHeader.AUTHORIZATION));
    }

    @Test
    void headersAreImmutable() {
        var headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        
        var request = new Request("GET", "/test", headers);
        
        var requestHeaders = request.getHeaders();
        requestHeaders.put("New-Header", "new-value");
        
        assertNull(request.header("New-Header"));
        assertEquals("application/json", request.header("Content-Type"));
    }

    @Test
    void emptyHeaders() {
        var request = new Request("GET", "/test");
        
        assertTrue(request.getHeaders().isEmpty());
        assertNull(request.header("Any-Header"));
        assertFalse(request.hasHeader("Any-Header"));
    }

    @Test
    void constructorWithQueryParams() {
        var headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        
        var queryParams = new HashMap<String, String>();
        queryParams.put("name", "john");
        queryParams.put("age", "25");
        
        var request = new Request("GET", "/users", headers, queryParams);
        
        assertEquals("GET", request.getMethod());
        assertEquals("/users", request.getPath());
        assertEquals("application/json", request.header("Content-Type"));
        assertEquals("john", request.queryParam("name"));
        assertEquals("25", request.queryParam("age"));
        assertTrue(request.hasQueryParam("name"));
        assertTrue(request.hasQueryParam("age"));
        assertFalse(request.hasQueryParam("nonexistent"));
    }

    @Test
    void queryParamOperations() {
        var queryParams = new HashMap<String, String>();
        queryParams.put("search", "java");
        queryParams.put("limit", "10");
        queryParams.put("active", ""); // Flag parameter
        
        var request = new Request("GET", "/search", new HashMap<>(), queryParams);
        
        assertEquals("java", request.queryParam("search"));
        assertEquals("10", request.queryParam("limit"));
        assertEquals("", request.queryParam("active"));
        assertNull(request.queryParam("nonexistent"));
        
        var allQueryParams = request.getQueryParams();
        assertEquals(3, allQueryParams.size());
        assertEquals("java", allQueryParams.get("search"));
        assertEquals("10", allQueryParams.get("limit"));
        assertEquals("", allQueryParams.get("active"));
    }

    @Test
    void queryParamsAreImmutable() {
        var queryParams = new HashMap<String, String>();
        queryParams.put("key", "value");
        
        var request = new Request("GET", "/test", new HashMap<>(), queryParams);
        
        var requestQueryParams = request.getQueryParams();
        requestQueryParams.put("newKey", "newValue");
        
        assertNull(request.queryParam("newKey"));
        assertEquals("value", request.queryParam("key"));
    }

    @Test
    void emptyQueryParams() {
        var request = new Request("GET", "/test");
        
        assertTrue(request.getQueryParams().isEmpty());
        assertNull(request.queryParam("any-param"));
        assertFalse(request.hasQueryParam("any-param"));
    }

    @Test
    void queryParamsWithSpecialCharacters() {
        var queryParams = new HashMap<String, String>();
        queryParams.put("search", "hello world");
        queryParams.put("filter", "category=books&price<50");
        queryParams.put("encoded", "test%20value");
        
        var request = new Request("GET", "/search", new HashMap<>(), queryParams);
        
        assertEquals("hello world", request.queryParam("search"));
        assertEquals("category=books&price<50", request.queryParam("filter"));
        assertEquals("test%20value", request.queryParam("encoded"));
    }

    @Test
    void constructorWithoutQueryParams() {
        var request = new Request("GET", "/test");
        
        assertTrue(request.getQueryParams().isEmpty());
        assertNull(request.queryParam("any"));
        assertFalse(request.hasQueryParam("any"));
    }

    @Test
    void constructorWithHeadersButNoQueryParams() {
        var headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        
        var request = new Request("GET", "/test", headers);
        
        assertEquals("application/json", request.header("Content-Type"));
        assertTrue(request.getQueryParams().isEmpty());
    }
}
