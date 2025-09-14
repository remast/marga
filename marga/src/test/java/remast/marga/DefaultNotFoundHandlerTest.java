package remast.marga;

import remast.marga.handlers.DefaultNotFoundHandler;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DefaultNotFoundHandlerTest {

    @Test
    void handleRequest() {
        var handler = new DefaultNotFoundHandler();
        var request = new Request("GET", "/nonexistent");
        
        var response = handler.handle(request);
        
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("404 - Not Found"));
        assertTrue(response.getBody().contains("The requested resource was not found"));
        assertTrue(response.getBody().contains("<html>"));
        assertTrue(response.getBody().contains("</html>"));
    }

    @Test
    void handleRequestWithDifferentMethods() {
        var handler = new DefaultNotFoundHandler();
        
        var getRequest = new Request("GET", "/test");
        var postRequest = new Request("POST", "/test");
        var putRequest = new Request("PUT", "/test");
        
        var getResponse = handler.handle(getRequest);
        var postResponse = handler.handle(postRequest);
        var putResponse = handler.handle(putRequest);
        
        assertEquals(404, getResponse.getStatusCode());
        assertEquals(404, postResponse.getStatusCode());
        assertEquals(404, putResponse.getStatusCode());
        
        assertEquals(getResponse.getBody(), postResponse.getBody());
        assertEquals(postResponse.getBody(), putResponse.getBody());
    }

    @Test
    void handleRequestWithPathParameters() {
        var handler = new DefaultNotFoundHandler();
        var request = new Request("GET", "/users/123");
        request.addPathParam("id", "123");
        
        var response = handler.handle(request);
        
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("404 - Not Found"));
    }
}
