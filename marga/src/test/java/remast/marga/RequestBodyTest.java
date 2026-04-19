package remast.marga;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestBodyTest {

    @Test
    void handlerShouldReceiveRequestBodyBytes() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.POST("/echo", request -> Response.ok(request.getBody()));

        withServer(router, () -> {
            var response = sendPost(router.getPort(), "/echo", "hello body".getBytes(StandardCharsets.UTF_8), "text/plain");
            assertEquals(200, response.statusCode());
            assertEquals("hello body", response.body());
        });
    }

    @Test
    void handlerShouldDecodeUtf8MultiByte() throws Exception {
        var text = "héllo — 世界";
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.POST("/echo", request -> Response.ok(request.getBody()));

        withServer(router, () -> {
            var response = sendPost(router.getPort(), "/echo", text.getBytes(StandardCharsets.UTF_8), "text/plain; charset=UTF-8");
            assertEquals(200, response.statusCode());
            assertEquals(text, response.body());
        });
    }

    @Test
    void handlerShouldReceiveBinaryBytes() throws Exception {
        var payload = new byte[] {0, 1, 2, 3, (byte) 0xFF, 0x7F};
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.POST("/echo", request -> new Response(request.getBodyBytes(), 200, new MediaType("application/octet-stream")));

        withServer(router, () -> {
            var response = sendPostBytes(router.getPort(), "/echo", payload, "application/octet-stream");
            assertEquals(200, response.statusCode());
            assertArrayEquals(payload, response.body());
        });
    }

    @Test
    void bodyOverLimitShouldReturn413() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).maxRequestBodyBytes(16).build());
        router.POST("/echo", request -> Response.ok(request.getBody()));

        withServer(router, () -> {
            var tooLarge = "x".repeat(32).getBytes(StandardCharsets.UTF_8);
            var response = sendPost(router.getPort(), "/echo", tooLarge, "text/plain");
            assertEquals(413, response.statusCode());
        });
    }

    @Test
    void missingContentLengthShouldYieldEmptyBody() {
        var router = new Router();
        router.POST("/echo", request -> Response.ok("len=" + request.getContentLength()));

        var response = router.handleRequest("POST", "/echo", java.util.Map.of(), java.util.Map.of());
        assertEquals("len=0", response.getBody());
    }

    @Test
    void handlerShouldReportContentLength() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.POST("/size", request -> Response.ok(String.valueOf(request.getContentLength())));

        withServer(router, () -> {
            var response = sendPost(router.getPort(), "/size", "abcde".getBytes(StandardCharsets.UTF_8), "text/plain");
            assertEquals("5", response.body());
        });
    }

    private interface ServerAction {
        void run() throws Exception;
    }

    private static void withServer(HttpRouter router, ServerAction action) throws Exception {
        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });
        waitForServerPort(router);
        try {
            action.run();
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    private static HttpResponse<String> sendPost(int port, String path, byte[] body, String contentType) throws Exception {
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var request = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:" + port + path))
            .header("Content-Type", contentType)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static HttpResponse<byte[]> sendPostBytes(int port, String path, byte[] body, String contentType) throws Exception {
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var request = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:" + port + path))
            .header("Content-Type", contentType)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private static void waitForServerPort(HttpRouter router) throws InterruptedException {
        for (var i = 0; i < 100; i++) {
            if (router.getPort() > 0) {
                return;
            }
            Thread.sleep(10);
        }
        fail("Server did not start in time");
    }
}
