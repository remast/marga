package remast.marga;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpWireIntegrationTest {

    @Test
    void serverShouldWriteHeadersAndBodyCorrectly() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.GET("/ping", request -> Response.ok("pong").header("X-Test", "yes"));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);

        try {
            var response = sendGet(router.getPort(), "/ping");
            assertEquals(200, response.statusCode());
            assertEquals("pong", response.body());
            assertEquals("yes", response.headers().firstValue("X-Test").orElse(null));
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void methodMismatchShouldReturn405WithAllow() {
        var router = new HttpRouter();
        router.POST("/users/${id}", request -> Response.ok("created"));

        var response = router.handleRequest("GET", "/users/42");
        assertEquals(405, response.getStatusCode());
        assertEquals("POST", response.getHeader(HttpHeader.ALLOW));
    }

    @Test
    void gzipMiddlewareShouldRoundTripOverHttp() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.use(new remast.marga.middleware.GzipCompressionMiddleware(10).create());
        router.GET("/gzip", request -> Response.ok("hello ".repeat(2_000)));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);

        try {
            var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

            var request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + router.getPort() + "/gzip"))
                .GET()
                .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, response.statusCode());
            assertEquals("gzip", response.headers().firstValue("Content-Encoding").orElse(null));

            var decoded = gunzip(response.body());
            assertEquals("hello ".repeat(2_000), decoded);
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    private static HttpResponse<String> sendGet(int port, String path) throws Exception {
        var client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

        var request = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:" + port + path))
            .GET()
            .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
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

    private static String gunzip(byte[] compressed) throws IOException {
        try (var gzip = new GZIPInputStream(new java.io.ByteArrayInputStream(compressed))) {
            var output = new java.io.ByteArrayOutputStream();
            var byteBufferArray = new byte[1024];
            int read;
            while ((read = gzip.read(byteBufferArray)) != -1) {
                output.write(byteBufferArray, 0, read);
            }
            return output.toString(StandardCharsets.UTF_8);
        }
    }
}
