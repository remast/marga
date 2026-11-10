package remast.marga;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpWireFormatTest {

    @Test
    void rawSocketShouldReceiveWellFormedResponse() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.GET("/ping", request -> Response.ok("pong"));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);
        try {
            var raw = sendRaw(router.getPort(), "GET /ping HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");

            assertTrue(raw.startsWith("HTTP/1.1 200 OK\r\n"), "status line wrong: " + raw);

            var headerEnd = raw.indexOf("\r\n\r\n");
            assertTrue(headerEnd > 0, "missing header terminator");

            var headerBlock = raw.substring(0, headerEnd);
            assertTrue(headerBlock.contains("\r\nContent-Length: 4\r\n") || headerBlock.contains("\nContent-Length: 4\r\n"),
                "missing Content-Length: " + headerBlock);
            assertTrue(headerBlock.toLowerCase().contains("\ndate: "), "missing Date header");

            var body = raw.substring(headerEnd + 4);
            assertEquals("pong", body);
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void binaryResponseShouldRoundTripByteForByte() throws Exception {
        var payload = new byte[] {0, 1, 2, 3, (byte) 0xFF, (byte) 0xFE, 0x7F, 0x10};
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.GET("/bin", request -> new Response(payload, 200, new MediaType("application/octet-stream")));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);
        try {
            var raw = sendRawBytes(router.getPort(), "GET /bin HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");

            var headerTerminator = new byte[] {'\r', '\n', '\r', '\n'};
            var headerEnd = indexOf(raw, headerTerminator);
            assertTrue(headerEnd > 0, "missing header terminator");

            var bodyLen = raw.length - (headerEnd + 4);
            assertEquals(payload.length, bodyLen);
            for (var i = 0; i < payload.length; i++) {
                assertEquals(payload[i], raw[headerEnd + 4 + i], "byte " + i);
            }
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void headResponseShouldNotWriteBodyButPreserveContentLength() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.GET("/head", request -> Response.ok("pong"));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);
        try {
            var raw = sendRaw(router.getPort(), "HEAD /head HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
            assertTrue(raw.startsWith("HTTP/1.1 200 OK\r\n"), "unexpected status: " + raw);
            assertTrue(raw.contains("Content-Length: 4\r\n"), "missing representation length: " + raw);
            assertEquals("", responseBody(raw));
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void lowercaseContentHeadersShouldNotBeDuplicatedOnWire() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.GET("/headers", request -> Response.ok("{}").header("content-type", "application/json").header("content-length", "999"));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);
        try {
            var raw = sendRaw(router.getPort(), "GET /headers HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
            var headerBlock = raw.substring(0, raw.indexOf("\r\n\r\n"));
            var contentTypeHeaders = headerBlock.lines()
                .filter(line -> line.regionMatches(true, 0, "content-type:", 0, "content-type:".length()))
                .count();
            var contentLengthHeaders = headerBlock.lines()
                .filter(line -> line.regionMatches(true, 0, "content-length:", 0, "content-length:".length()))
                .count();

            assertEquals(1, contentTypeHeaders, headerBlock);
            assertEquals(1, contentLengthHeaders, headerBlock);
            assertTrue(headerBlock.toLowerCase().contains("content-length: 2"), headerBlock);
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void noContentAndNotModifiedResponsesShouldNotWriteBodies() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.GET("/no-content", request -> new Response("ignored", HttpStatus.NO_CONTENT));
        router.GET("/not-modified", request -> new Response("cached", HttpStatus.NOT_MODIFIED));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);
        try {
            var noContent = sendRaw(router.getPort(), "GET /no-content HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
            assertFalse(noContent.toLowerCase().contains("content-length:"), "204 must not include Content-Length: " + noContent);
            assertEquals("", responseBody(noContent));

            var notModified = sendRaw(router.getPort(), "GET /not-modified HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
            assertTrue(notModified.contains("Content-Length: 6\r\n"), "missing representation length: " + notModified);
            assertEquals("", responseBody(notModified));
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void headerValueWithCrlfShouldYield500() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.GET("/bad", request -> Response.ok("hi").header("X-Bad", "foo\r\nX-Injected: yes"));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);
        try {
            var raw = sendRaw(router.getPort(), "GET /bad HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
            assertTrue(raw.startsWith("HTTP/1.1 500 "), "expected 500, got: " + raw);
            assertFalse(raw.contains("X-Injected"), "header injection leaked into response");
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void headerNameWithCrlfShouldYield500WithoutInjectingHeaders() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.GET("/bad", request -> Response.ok("hi").header("X-Bad\r\nX-Injected", "yes"));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);
        try {
            var raw = sendRaw(router.getPort(), "GET /bad HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
            assertTrue(raw.startsWith("HTTP/1.1 500 "), "expected 500, got: " + raw);
            assertFalse(raw.contains("X-Injected"), "header injection leaked into response");
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void headerNameWithInvalidTokenCharacterShouldYield500() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.GET("/bad", request -> Response.ok("hi").header("X Bad", "value"));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);
        try {
            var raw = sendRaw(router.getPort(), "GET /bad HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
            assertTrue(raw.startsWith("HTTP/1.1 500 "), "expected 500, got: " + raw);
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void handlerShouldReadLowercaseWireHeaders() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        router.GET("/headers", request -> Response.ok(request.header(HttpHeader.AUTHORIZATION)));

        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });

        waitForServerPort(router);
        try {
            var raw = sendRaw(router.getPort(), "GET /headers HTTP/1.1\r\nhost: localhost\r\nauthorization: Bearer token123\r\nconnection: close\r\n\r\n");
            assertTrue(raw.endsWith("Bearer token123"), "handler did not receive lowercase header: " + raw);
        } finally {
            router.shutdown();
            serverThread.join(2_000);
        }
    }

    @Test
    void oversizedRequestLineShouldReturn414() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        var serverThread = startServer(router);

        try {
            var request = "GET /" + "x".repeat(8 * 1024) + " HTTP/1.1\r\n\r\n";
            var raw = sendRaw(router.getPort(), request);
            assertTrue(raw.startsWith("HTTP/1.1 414 URI Too Long\r\n"), raw);
        } finally {
            stopServer(router, serverThread);
        }
    }

    @Test
    void oversizedTotalHeadersShouldReturn431() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(3_000).build());
        var serverThread = startServer(router);

        try {
            var request = new StringBuilder("GET / HTTP/1.1\r\n");
            for (var i = 0; i < 17; i++) {
                request.append("X-Header-").append(i).append(": ").repeat("x", 4_000).append("\r\n");
            }
            request.append("\r\n");
            var raw = sendRaw(router.getPort(), request.toString());
            assertTrue(raw.startsWith("HTTP/1.1 431 Request Header Fields Too Large\r\n"), raw);
        } finally {
            stopServer(router, serverThread);
        }
    }

    @Test
    void requestHeadDeadlineShouldReturn408DespiteProgress() throws Exception {
        var router = new HttpRouter(Config.builder().host("127.0.0.1").port(0).readTimeoutMs(700).build());
        var serverThread = startServer(router);

        try (var socket = new Socket("127.0.0.1", router.getPort())) {
            socket.setSoTimeout(3_000);
            var out = socket.getOutputStream();
            out.write("GET / HTTP/1.1\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();
            Thread.sleep(250);
            out.write("X-One: a\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();
            Thread.sleep(250);
            out.write("X-Two: b\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();

            var raw = new String(readAll(socket.getInputStream()), StandardCharsets.ISO_8859_1);
            assertTrue(raw.startsWith("HTTP/1.1 408 Request Timeout\r\n"), raw);
        } finally {
            stopServer(router, serverThread);
        }
    }

    private static String sendRaw(int port, String request) throws IOException {
        return new String(sendRawBytes(port, request), StandardCharsets.ISO_8859_1);
    }

    private static String responseBody(String raw) {
        var headerEnd = raw.indexOf("\r\n\r\n");
        assertTrue(headerEnd >= 0, "missing header terminator: " + raw);
        return raw.substring(headerEnd + 4);
    }

    private static byte[] sendRawBytes(int port, String request) throws IOException {
        try (var socket = new Socket("127.0.0.1", port)) {
            socket.setSoTimeout(5_000);
            socket.getOutputStream().write(request.getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
            try (var in = socket.getInputStream()) {
                return readAll(in);
            }
        }
    }

    private static Thread startServer(HttpRouter router) throws InterruptedException {
        var serverThread = Thread.ofVirtual().start(() -> {
            try {
                router.run();
            } catch (IOException ignored) {
            }
        });
        waitForServerPort(router);
        return serverThread;
    }

    private static void stopServer(HttpRouter router, Thread serverThread) throws InterruptedException {
        router.shutdown();
        serverThread.join(2_000);
    }

    private static byte[] readAll(InputStream in) throws IOException {
        var out = new java.io.ByteArrayOutputStream();
        var buf = new byte[1024];
        int r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
        }
        return out.toByteArray();
    }

    private static int indexOf(byte[] haystack, byte[] needle) {
        outer:
        for (var i = 0; i <= haystack.length - needle.length; i++) {
            for (var j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
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
