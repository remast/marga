package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
}
