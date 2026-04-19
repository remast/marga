package remast.marga.middleware;

import org.junit.jupiter.api.Test;
import remast.marga.Response;
import remast.marga.Router;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlingMiddlewareTest {

    @Test
    void defaultMiddlewareReturns500WhenHandlerThrows() {
        var router = new Router();
        router.use(ErrorHandlingMiddleware.create());
        router.GET("/boom", request -> {
            throw new RuntimeException("boom");
        });

        var response = router.handleRequest("GET", "/boom");
        assertEquals(500, response.getStatusCode());
        assertEquals("500 - Internal Server Error", response.getBody());
    }

    @Test
    void customMapperProducesMappedResponse() {
        var router = new Router();
        router.use(ErrorHandlingMiddleware.create((request, error) -> {
            if (error instanceof IllegalArgumentException) {
                return new Response("bad: " + error.getMessage(), 400);
            }
            return null;
        }));
        router.GET("/bad", request -> {
            throw new IllegalArgumentException("nope");
        });

        var response = router.handleRequest("GET", "/bad");
        assertEquals(400, response.getStatusCode());
        assertEquals("bad: nope", response.getBody());
    }

    @Test
    void mapperThatReturnsNullFallsBackTo500() {
        var router = new Router();
        router.use(ErrorHandlingMiddleware.create((request, error) -> null));
        router.GET("/boom", request -> {
            throw new RuntimeException("x");
        });

        var response = router.handleRequest("GET", "/boom");
        assertEquals(500, response.getStatusCode());
    }

    @Test
    void mapperThatItselfThrowsFallsBackTo500() {
        var router = new Router();
        router.use(ErrorHandlingMiddleware.create((request, error) -> {
            throw new IllegalStateException("mapper broken");
        }));
        router.GET("/boom", request -> {
            throw new RuntimeException("x");
        });

        var response = router.handleRequest("GET", "/boom");
        assertEquals(500, response.getStatusCode());
    }

    @Test
    void successfulHandlerPassesThrough() {
        var router = new Router();
        router.use(ErrorHandlingMiddleware.create());
        router.GET("/ok", request -> Response.ok("fine"));

        var response = router.handleRequest("GET", "/ok");
        assertEquals(200, response.getStatusCode());
        assertEquals("fine", response.getBody());
    }

    @Test
    void createWithNullMapperThrows() {
        assertThrows(IllegalArgumentException.class, () -> ErrorHandlingMiddleware.create(null));
    }
}
