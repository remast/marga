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

    private static String sendRaw(int port, String request) throws IOException {
        return new String(sendRawBytes(port, request), StandardCharsets.ISO_8859_1);
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
