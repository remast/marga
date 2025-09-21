package remast.marga.middleware;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import remast.marga.HttpRouter;
import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

class LoggingMiddlewareTest {
    
    private HttpRouter router;
    private ByteArrayOutputStream logOutput;
    private Handler logHandler;
    
    @BeforeEach
    void setUp() {
        router = new HttpRouter();
        logOutput = new ByteArrayOutputStream();
        logHandler = new StreamHandler(new PrintStream(logOutput), new java.util.logging.SimpleFormatter());
        
        var logger = Logger.getLogger(LoggingMiddleware.class.getName());
        logger.addHandler(logHandler);
        logger.setUseParentHandlers(false);
    }
    
    @Test
    void shouldLogRequestAndResponse() {
        var handler = (RequestHandler) request -> Response.ok("Hello World");
        
        router.use(LoggingMiddleware.create());
        router.GET("/test", handler);
        
        var response = router.handleRequest("GET", "/test");
        
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello World", response.getBody());
        
        logHandler.flush();
        var logContent = logOutput.toString();
        
        assertTrue(logContent.contains("GET /test"));
        assertTrue(logContent.contains("GET /test - 200"));
    }
    
    @Test
    void shouldLogRequestWithDifferentMethods() {
        var handler = (RequestHandler) request -> Response.ok("Response");
        
        router.use(LoggingMiddleware.create());
        router.POST("/api/data", handler);
        
        var response = router.handleRequest("POST", "/api/data");
        
        assertEquals(200, response.getStatusCode());
        
        logHandler.flush();
        var logContent = logOutput.toString();
        
        assertTrue(logContent.contains("POST /api/data"));
        assertTrue(logContent.contains("POST /api/data - 200"));
    }
    
    @Test
    void shouldLogErrorWhenHandlerThrowsException() {
        var handler = (RequestHandler) request -> {
            throw new RuntimeException("Test error");
        };
        
        router.use(LoggingMiddleware.create());
        router.GET("/error", handler);
        
        assertThrows(RuntimeException.class, () -> {
            router.handleRequest("GET", "/error");
        });
        
        logHandler.flush();
        var logContent = logOutput.toString();
        
        assertTrue(logContent.contains("GET /error"));
        assertTrue(logContent.contains("GET /error - ERROR: Test error"));
    }
    
    @Test
    void shouldLogNotFoundRequests() {
        router.use(LoggingMiddleware.create());
        
        var response = router.handleRequest("GET", "/nonexistent");
        
        assertEquals(404, response.getStatusCode());
        
        logHandler.flush();
        var logContent = logOutput.toString();
        
        assertTrue(logContent.contains("GET /nonexistent"));
        assertTrue(logContent.contains("GET /nonexistent - 404"));
    }
}
