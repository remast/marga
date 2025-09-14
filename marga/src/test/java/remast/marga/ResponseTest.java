package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void constructorWithBodyAndStatus() {
        var response = new Response("Hello World", HttpStatus.OK);
        
        assertEquals("Hello World", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void constructorWithBodyAndStatusCode() {
        var response = new Response("Error", 500);
        
        assertEquals("Error", response.getBody());
        assertEquals(500, response.getStatusCode());
    }

    @Test
    void constructorWithBodyOnly() {
        var response = new Response("Default OK");
        
        assertEquals("Default OK", response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void staticFactoryMethods() {
        var okResponse = Response.ok("Success");
        assertEquals("Success", okResponse.getBody());
        assertEquals(200, okResponse.getStatusCode());

        var notFoundResponse = Response.notFound("Not Found");
        assertEquals("Not Found", notFoundResponse.getBody());
        assertEquals(404, notFoundResponse.getStatusCode());

        var serverErrorResponse = Response.serverError("Server Error");
        assertEquals("Server Error", serverErrorResponse.getBody());
        assertEquals(500, serverErrorResponse.getStatusCode());

        var badRequestResponse = Response.badRequest("Bad Request");
        assertEquals("Bad Request", badRequestResponse.getBody());
        assertEquals(400, badRequestResponse.getStatusCode());

        var unauthorizedResponse = Response.unauthorized("Unauthorized");
        assertEquals("Unauthorized", unauthorizedResponse.getBody());
        assertEquals(401, unauthorizedResponse.getStatusCode());

        var forbiddenResponse = Response.forbidden("Forbidden");
        assertEquals("Forbidden", forbiddenResponse.getBody());
        assertEquals(403, forbiddenResponse.getStatusCode());

        var methodNotAllowedResponse = Response.methodNotAllowed("Method Not Allowed");
        assertEquals("Method Not Allowed", methodNotAllowedResponse.getBody());
        assertEquals(405, methodNotAllowedResponse.getStatusCode());

        var createdResponse = Response.created("Created");
        assertEquals("Created", createdResponse.getBody());
        assertEquals(201, createdResponse.getStatusCode());

        var noContentResponse = Response.noContent();
        assertEquals("", noContentResponse.getBody());
        assertEquals(204, noContentResponse.getStatusCode());
    }

    @Test
    void nullBody() {
        var response = new Response(null, HttpStatus.OK);
        
        assertNull(response.getBody());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void emptyBody() {
        var response = new Response("", HttpStatus.NO_CONTENT);
        
        assertEquals("", response.getBody());
        assertEquals(204, response.getStatusCode());
    }
}
