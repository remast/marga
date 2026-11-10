package remast.marga;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouterFreezeTest {

    @Test
    void firstRequestFreezesRouter() {
        var router = new Router();
        router.GET("/a", request -> Response.ok("a"));
        assertFalse(router.isFrozen());

        router.handleRequest("GET", "/a");

        assertTrue(router.isFrozen());
    }

    @Test
    void addingRoutesAfterFreezeThrows() {
        var router = new Router();
        router.freeze();

        assertThrows(IllegalStateException.class, () -> router.GET("/a", request -> Response.ok("a")));
        assertThrows(IllegalStateException.class, () -> router.addParameterizedRoute("GET", "/u/${id}", request -> Response.ok("u")));
    }

    @Test
    void modifyingMiddlewareAfterFreezeThrows() {
        var router = new Router();
        router.freeze();

        Function<RequestHandler, RequestHandler> passThrough = next -> next;
        assertThrows(IllegalStateException.class, () -> router.use(passThrough));
        assertThrows(IllegalStateException.class, router::clearMiddleware);
    }

    @Test
    void freezeIsIdempotent() {
        var wraps = new AtomicInteger();
        var router = new Router();
        router.use((Function<RequestHandler, RequestHandler>) next -> {
            wraps.incrementAndGet();
            return next;
        });
        router.GET("/a", request -> Response.ok("a"));

        router.freeze();
        var afterFirstFreeze = wraps.get();
        router.freeze();

        assertEquals(afterFirstFreeze, wraps.get());
    }

    @Test
    void serverRunFreezesRouter() throws Exception {
        var httpRouter = new HttpRouter(Config.builder().host("127.0.0.1").port(0).build());
        httpRouter.GET("/a", request -> Response.ok("a"));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                httpRouter.run();
            } catch (Exception ignored) {
            }
        });

        try {
            for (var i = 0; i < 100 && httpRouter.getPort() == 0; i++) {
                Thread.sleep(10);
            }
            assertThrows(IllegalStateException.class, () -> httpRouter.GET("/b", request -> Response.ok("b")));
        } finally {
            httpRouter.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void concurrentFirstRequestsWrapEachHandlerOnce() throws Exception {
        var wraps = new AtomicInteger();
        var router = new Router();
        router.use((Function<RequestHandler, RequestHandler>) next -> {
            wraps.incrementAndGet();
            return next;
        });
        router.GET("/a", request -> Response.ok("a"));
        router.GET("/u/${id}", request -> Response.ok(request.pathParam("id")));
        router.POST("/p", request -> Response.ok("p"));

        var threads = 64;
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = new ArrayList<Future<Integer>>();
            for (var i = 0; i < threads; i++) {
                var index = i;
                futures.add(executor.submit(() -> {
                    start.await();
                    return switch (index % 3) {
                        case 0 -> router.handleRequest("GET", "/a").getStatusCode();
                        case 1 -> router.handleRequest("GET", "/u/" + index).getStatusCode();
                        default -> router.handleRequest("GET", "/p").getStatusCode();
                    };
                }));
            }
            start.countDown();
            for (var i = 0; i < threads; i++) {
                var expected = i % 3 == 2 ? 405 : 200;
                assertEquals(expected, futures.get(i).get());
            }
        }

        // 3 routes + not-found handler + one 405 handler for Allow: POST
        assertEquals(5, wraps.get());
    }
}
