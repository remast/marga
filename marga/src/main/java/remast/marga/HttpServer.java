package remast.marga;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
    private final HttpRequestParser requestParser;
    private final AtomicBoolean running;
    private ServerSocket serverSocket;

    HttpServer(Router router, Config config) {
        this.router = router;
        this.config = config;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.responseWriter = new HttpResponseWriter();
        this.requestParser = new HttpRequestParser(config);
        this.running = new AtomicBoolean(false);
    }

    void run() throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Server is already running");
        }

        router.freeze();

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
                } catch (IOException e) {
                    if (running.get()) {
                        logger.warning("I/O exception while accepting connection: " + e.getMessage());
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
            Request request;
            try {
                request = requestParser.parse(in, clientSocket);
            } catch (HttpRequestParser.RequestParseException e) {
                responseWriter.write(out, e.response(), e.requestMethod());
                return;
            }
            if (request == null) {
                return;
            }

            Response response;
            try {
                response = router.handleRequest(request);
            } catch (Throwable t) {
                logger.severe("Unhandled exception while handling request: " + t.getMessage());
                response = Response.serverError("500 - Internal Server Error");
            }

            try {
                responseWriter.write(out, response, request.getMethod());
            } catch (IllegalArgumentException e) {
                logger.severe("Invalid response headers, returning 500: " + e.getMessage());
                responseWriter.write(out, Response.serverError("500 - Internal Server Error"), request.getMethod());
            }
        } catch (SocketException e) {
            logger.fine("Socket closed while handling request: " + e.getMessage());
        } catch (IOException e) {
            logger.warning("Error handling socket request: " + e.getMessage());
        }
    }
}
