package remast.marga;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

final class HttpServer {
    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());

    private final Router router;
    private final Config config;
    private final ExecutorService executor;
    private final HttpResponseWriter responseWriter;
    private final AtomicBoolean running;

    private ServerSocket serverSocket;

    HttpServer(Router router, Config config) {
        this.router = router;
        this.config = config;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.responseWriter = new HttpResponseWriter();
        this.running = new AtomicBoolean(false);
    }

    void run() throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Server is already running");
        }

        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(config.getHost(), config.getPort()), config.getAcceptBacklog());
            logger.info("HTTP Server running on " + config.getServerUrl(serverSocket.getLocalPort()));

            while (running.get()) {
                try {
                    var clientSocket = serverSocket.accept();
                    executor.submit(() -> handleSocketRequest(clientSocket));
                } catch (SocketException e) {
                    if (running.get()) {
                        logger.warning("Socket exception while accepting connection: " + e.getMessage());
                    }
                }
            }
        } finally {
            running.set(false);
            closeServerSocket();
            executor.shutdown();
        }
    }

    void shutdown() {
        running.set(false);
        closeServerSocket();
        executor.shutdown();
    }

    int getBoundPort() {
        if (serverSocket != null && serverSocket.isBound() && !serverSocket.isClosed()) {
            return serverSocket.getLocalPort();
        }
        return config.getPort();
    }

    private void closeServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.fine("Failed to close server socket: " + e.getMessage());
            }
        }
    }

    private void handleSocketRequest(Socket clientSocket) {
        try (clientSocket; var in = new BufferedInputStream(clientSocket.getInputStream()); var out = clientSocket.getOutputStream()) {
            clientSocket.setSoTimeout(config.getReadTimeoutMs());

            var requestLine = readLine(in);
            if (requestLine == null || requestLine.isBlank()) {
                return;
            }

            var requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                responseWriter.write(out, Response.badRequest("400 - Bad Request"));
                return;
            }

            var method = requestParts[0];
            var fullPath = requestParts[1];
            var path = extractPath(fullPath);
            var queryParams = parseQueryParameters(fullPath);
            var headers = parseHeaders(in);

            var contentLength = parseContentLength(headers);
            if (contentLength > config.getMaxRequestBodyBytes()) {
                responseWriter.write(out, new Response("413 - Payload Too Large", HttpStatus.PAYLOAD_TOO_LARGE));
                return;
            }

            var body = contentLength > 0 ? in.readNBytes(contentLength) : new byte[0];
            var charset = parseCharset(headers.get(HttpHeader.CONTENT_TYPE.getValue()));
            var request = new Request(normalizeMethod(method), path, headers, queryParams, body, charset);

            Response response;
            try {
                response = router.handleRequest(request);
            } catch (Throwable t) {
                logger.severe("Unhandled exception while handling request: " + t.getMessage());
                response = Response.serverError("500 - Internal Server Error");
            }

            try {
                responseWriter.write(out, response);
            } catch (IllegalArgumentException e) {
                logger.severe("Invalid response headers, returning 500: " + e.getMessage());
                responseWriter.write(out, Response.serverError("500 - Internal Server Error"));
            }
        } catch (SocketException e) {
            logger.fine("Socket closed while handling request: " + e.getMessage());
        } catch (IOException e) {
            logger.warning("Error handling socket request: " + e.getMessage());
        }
    }

    private Map<String, String> parseHeaders(BufferedInputStream in) throws IOException {
        var headers = new HashMap<String, String>();
        while (true) {
            var headerLine = readLine(in);
            if (headerLine == null || headerLine.isEmpty()) {
                break;
            }

            var colonIndex = headerLine.indexOf(':');
            if (colonIndex > 0) {
                var headerName = headerLine.substring(0, colonIndex).trim();
                var headerValue = headerLine.substring(colonIndex + 1).trim();
                headers.put(headerName, headerValue);
            }
        }
        return headers;
    }

    private String readLine(BufferedInputStream in) throws IOException {
        var line = new StringBuilder();
        int current;
        var sawCarriageReturn = false;

        while ((current = in.read()) != -1) {
            if (current == '\r') {
                sawCarriageReturn = true;
                continue;
            }
            if (current == '\n') {
                break;
            }
            if (sawCarriageReturn) {
                line.append('\r');
                sawCarriageReturn = false;
            }
            line.append((char) current);
        }

        if (current == -1 && line.isEmpty()) {
            return null;
        }

        return line.toString();
    }

    private String extractPath(String fullPath) {
        var questionMarkIndex = fullPath.indexOf('?');
        return questionMarkIndex != -1 ? fullPath.substring(0, questionMarkIndex) : fullPath;
    }

    private Map<String, String> parseQueryParameters(String fullPath) {
        var queryParams = new HashMap<String, String>();
        var questionMarkIndex = fullPath.indexOf('?');
        if (questionMarkIndex == -1) {
            return queryParams;
        }

        var queryString = fullPath.substring(questionMarkIndex + 1);
        if (queryString.isEmpty()) {
            return queryParams;
        }

        var pairs = queryString.split("&");
        for (var pair : pairs) {
            var equalIndex = pair.indexOf('=');
            if (equalIndex != -1) {
                var key = pair.substring(0, equalIndex);
                var value = pair.substring(equalIndex + 1);
                queryParams.put(urlDecode(key), urlDecode(value));
            } else if (!pair.isEmpty()) {
                queryParams.put(urlDecode(pair), "");
            }
        }

        return queryParams;
    }

    private String urlDecode(String encoded) {
        try {
            return java.net.URLDecoder.decode(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encoded;
        }
    }

    private static int parseContentLength(Map<String, String> headers) {
        var value = findHeaderValue(headers, HttpHeader.CONTENT_LENGTH.getValue());
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            var parsed = Integer.parseInt(value.trim());
            return Math.max(parsed, 0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Charset parseCharset(String contentType) {
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        var lower = contentType.toLowerCase();
        var idx = lower.indexOf("charset=");
        if (idx < 0) {
            return StandardCharsets.UTF_8;
        }
        var charsetName = contentType.substring(idx + "charset=".length()).trim();
        var semi = charsetName.indexOf(';');
        if (semi >= 0) {
            charsetName = charsetName.substring(0, semi).trim();
        }
        if (charsetName.startsWith("\"") && charsetName.endsWith("\"") && charsetName.length() >= 2) {
            charsetName = charsetName.substring(1, charsetName.length() - 1);
        }
        try {
            return Charset.forName(charsetName);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            logger.warning("Unsupported charset '" + charsetName + "', falling back to UTF-8");
            return StandardCharsets.UTF_8;
        }
    }

    private static String findHeaderValue(Map<String, String> headers, String name) {
        for (var entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return "GET";
        }
        return method.trim().toUpperCase();
    }
}
