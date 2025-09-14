package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void constructorWithBodyAndStatus() {
        var response = new Response("Hello World", HttpStatus.OK);
        
        assertEquals("Hello World", response.getBody());
        assertEquals(200, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getMediaType());
    }

    @Test
    void constructorWithBodyAndStatusCode() {
        var response = new Response("Error", 500);
        
        assertEquals("Error", response.getBody());
        assertEquals(500, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getMediaType());
    }

    @Test
    void constructorWithBodyOnly() {
        var response = new Response("Default OK");
        
        assertEquals("Default OK", response.getBody());
        assertEquals(200, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getMediaType());
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
        assertEquals(MediaType.TEXT_PLAIN, response.getMediaType());
    }

    @Test
    void emptyBody() {
        var response = new Response("", HttpStatus.NO_CONTENT);
        
        assertEquals("", response.getBody());
        assertEquals(204, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getMediaType());
    }

    @Test
    void constructorWithMediaType() {
        var response = new Response("JSON data", HttpStatus.OK, MediaType.APPLICATION_JSON);
        
        assertEquals("JSON data", response.getBody());
        assertEquals(200, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType());
    }

    @Test
    void constructorWithStatusCodeAndMediaType() {
        var response = new Response("HTML content", 200, MediaType.TEXT_HTML);
        
        assertEquals("HTML content", response.getBody());
        assertEquals(200, response.getStatusCode());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType());
    }

    @Test
    void staticFactoryMethodsWithMediaType() {
        var jsonResponse = Response.json("{\"message\": \"Hello\"}");
        assertEquals("{\"message\": \"Hello\"}", jsonResponse.getBody());
        assertEquals(200, jsonResponse.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, jsonResponse.getMediaType());

        var htmlResponse = Response.html("<h1>Hello World</h1>");
        assertEquals("<h1>Hello World</h1>", htmlResponse.getBody());
        assertEquals(200, htmlResponse.getStatusCode());
        assertEquals(MediaType.TEXT_HTML, htmlResponse.getMediaType());

        var okWithMediaType = Response.ok("Custom content", MediaType.TEXT_CSS);
        assertEquals("Custom content", okWithMediaType.getBody());
        assertEquals(200, okWithMediaType.getStatusCode());
        assertEquals(MediaType.TEXT_CSS, okWithMediaType.getMediaType());
    }

    @Test
    void customMediaTypeInResponse() {
        var customMediaType = new MediaType("application/custom");
        var response = new Response("Custom content", HttpStatus.OK, customMediaType);
        
        assertEquals("Custom content", response.getBody());
        assertEquals(200, response.getStatusCode());
        assertEquals(customMediaType, response.getMediaType());
        assertEquals("application/custom", response.getMediaType().getValue());
    }
}
