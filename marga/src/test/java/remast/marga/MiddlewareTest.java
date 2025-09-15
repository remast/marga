package remast.marga;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class MiddlewareTest {
    
    private HttpRouter router;
    
    @BeforeEach
    void setUp() {
        router = new HttpRouter();
    }
    
    @Test
    void middlewareShouldWrapHandler() {
        var callCount = new AtomicInteger(0);
        var middlewareCallCount = new AtomicInteger(0);
        
        var handler = (RequestHandler) request -> {
            callCount.incrementAndGet();
            return Response.ok("Hello");
        };
        
        var middleware = (Middleware) nextHandler -> request -> {
            middlewareCallCount.incrementAndGet();
            return nextHandler.handle(request);
        };
        
        router.use(middleware);
        router.GET("/test", handler);
        
        var response = router.handleRequest("GET", "/test");
        
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello", response.getBody());
        assertEquals(1, callCount.get());
        assertEquals(1, middlewareCallCount.get());
    }
    
    @Test
    void multipleMiddlewareShouldChainInOrder() {
        var executionOrder = new AtomicReference<String>("");
        
        var handler = (RequestHandler) request -> {
            executionOrder.set(executionOrder.get() + "handler");
            return Response.ok("Hello");
        };
        
        var middleware1 = (Middleware) nextHandler -> request -> {
            executionOrder.set(executionOrder.get() + "1");
            var response = nextHandler.handle(request);
            executionOrder.set(executionOrder.get() + "1");
            return response;
        };
        
        var middleware2 = (Middleware) nextHandler -> request -> {
            executionOrder.set(executionOrder.get() + "2");
            var response = nextHandler.handle(request);
            executionOrder.set(executionOrder.get() + "2");
            return response;
        };
        
        router.use(middleware1);
        router.use(middleware2);
        router.GET("/test", handler);
        
        var response = router.handleRequest("GET", "/test");
        
        assertEquals(200, response.getStatusCode());
        assertEquals("21handler12", executionOrder.get());
    }
    
    @Test
    void middlewareShouldWorkWithNotFoundHandler() {
        var middlewareCallCount = new AtomicInteger(0);
        
        var middleware = (Middleware) nextHandler -> request -> {
            middlewareCallCount.incrementAndGet();
            return nextHandler.handle(request);
        };
        
        router.use(middleware);
        
        var response = router.handleRequest("GET", "/nonexistent");
        
        assertEquals(404, response.getStatusCode());
        assertEquals(1, middlewareCallCount.get());
    }
    
    @Test
    void middlewareShouldHandleExceptions() {
        var middlewareCallCount = new AtomicInteger(0);
        var exceptionHandled = new AtomicInteger(0);
        
        var handler = (RequestHandler) request -> {
            throw new RuntimeException("Test exception");
        };
        
        var middleware = (Middleware) nextHandler -> request -> {
            middlewareCallCount.incrementAndGet();
            try {
                return nextHandler.handle(request);
            } catch (RuntimeException e) {
                exceptionHandled.incrementAndGet();
                return Response.serverError("Error: " + e.getMessage());
            }
        };
        
        router.use(middleware);
        router.GET("/test", handler);
        
        var response = router.handleRequest("GET", "/test");
        
        assertEquals(500, response.getStatusCode());
        assertEquals("Error: Test exception", response.getBody());
        assertEquals(1, middlewareCallCount.get());
        assertEquals(1, exceptionHandled.get());
    }
}
