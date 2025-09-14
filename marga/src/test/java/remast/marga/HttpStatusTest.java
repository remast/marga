package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HttpStatusTest {

    @Test
    void httpStatusValues() {
        assertEquals(200, HttpStatus.OK.getCode());
        assertEquals("OK", HttpStatus.OK.getReasonPhrase());
        
        assertEquals(404, HttpStatus.NOT_FOUND.getCode());
        assertEquals("Not Found", HttpStatus.NOT_FOUND.getReasonPhrase());
        
        assertEquals(500, HttpStatus.INTERNAL_SERVER_ERROR.getCode());
        assertEquals("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        
        assertEquals(400, HttpStatus.BAD_REQUEST.getCode());
        assertEquals("Bad Request", HttpStatus.BAD_REQUEST.getReasonPhrase());
        
        assertEquals(401, HttpStatus.UNAUTHORIZED.getCode());
        assertEquals("Unauthorized", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        
        assertEquals(403, HttpStatus.FORBIDDEN.getCode());
        assertEquals("Forbidden", HttpStatus.FORBIDDEN.getReasonPhrase());
        
        assertEquals(405, HttpStatus.METHOD_NOT_ALLOWED.getCode());
        assertEquals("Method Not Allowed", HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
        
        assertEquals(201, HttpStatus.CREATED.getCode());
        assertEquals("Created", HttpStatus.CREATED.getReasonPhrase());
        
        assertEquals(204, HttpStatus.NO_CONTENT.getCode());
        assertEquals("No Content", HttpStatus.NO_CONTENT.getReasonPhrase());
    }

    @Test
    void toStringMethod() {
        assertEquals("200 OK", HttpStatus.OK.toString());
        assertEquals("404 Not Found", HttpStatus.NOT_FOUND.toString());
        assertEquals("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.toString());
    }

    @Test
    void allStatusCodesAreUnique() {
        var statuses = HttpStatus.values();
        var codes = new int[statuses.length];
        
        for (int i = 0; i < statuses.length; i++) {
            codes[i] = statuses[i].getCode();
        }
        
        for (int i = 0; i < codes.length; i++) {
            for (int j = i + 1; j < codes.length; j++) {
                assertNotEquals(codes[i], codes[j], 
                    "Status codes should be unique: " + statuses[i] + " and " + statuses[j]);
            }
        }
    }
}
