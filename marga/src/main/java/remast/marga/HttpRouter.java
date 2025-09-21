package remast.marga;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import java.util.Comparator;
import remast.marga.handlers.DefaultNotFoundHandler;

public class HttpRouter {
    private static final Logger logger = Logger.getLogger(HttpRouter.class.getName());
    private final Map<String, Route> exactRoutes;
    private final List<Route> parameterizedRoutes;
    private final RequestHandler notFoundHandler;
    private final Config config;
    private final ExecutorService executor;
    private final List<java.util.function.Function<RequestHandler, RequestHandler>> middleware;
    
    public HttpRouter() {
        this.exactRoutes = new HashMap<>();
        this.parameterizedRoutes = new ArrayList<>();
        this.notFoundHandler = new DefaultNotFoundHandler();
        this.config = new Config();
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.middleware = new ArrayList<>();
        
        // Add shutdown hook to properly close the virtual thread executor
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    public Response handleRequest(String method, String path) {
        var request = new Request(method, path);
        return handleRequestInternal(request);
    }
    
    public Response handleRequest(String method, String path, Map<String, String> headers) {
        var request = new Request(method, path, headers);
        return handleRequestInternal(request);
    }
    
    public Response handleRequest(String method, String path, Map<String, String> headers, Map<String, String> queryParams) {
        var request = new Request(method, path, headers, queryParams);
        return handleRequestInternal(request);
    }
    
    private Response handleRequestInternal(Request request) {
        var method = request.getMethod();
        var path = request.getPath();
        
        // Find the longest matching route among all routes (exact + parameterized)
        Route longestMatch = null;
        int longestLength = 0;
        boolean isExactMatch = false;
        
        // Check exact routes
        var routeKey = method + " " + path;
        var exactRoute = exactRoutes.get(routeKey);
        if (exactRoute != null) {
            longestMatch = exactRoute;
            longestLength = path.length();
            isExactMatch = true;
        }
        
        // Check parameterized routes only if no exact match was found
        if (!isExactMatch) {
            for (var route : parameterizedRoutes) {
                if (route.matches(path)) {
                    var pattern = route.getPattern();
                    if (pattern != null && pattern.length() > longestLength) {
                        longestMatch = route;
                        longestLength = pattern.length();
                    }
                }
            }
        }
        
        if (longestMatch != null) {
            if (!isExactMatch) {
                longestMatch.extractParameters(path, request);
            }
            var handler = longestMatch.getHandler();
            return applyMiddleware(handler).handle(request);
        }
        
        // No route found
        return applyMiddleware(notFoundHandler).handle(request);
    }
    
    public void addRoute(String method, String path, RequestHandler handler) {
        addRoute(method, path, handler, null);
    }
    
    public void addRoute(String method, String path, RequestHandler handler, String description) {
        if (path.contains("${")) {
            // Parameterized route
            var route = new Route(handler, description, path);
            parameterizedRoutes.add(route);
        } else {
            // Exact route
            var routeKey = method + " " + path;
            exactRoutes.put(routeKey, new Route(handler, description));
        }
    }
    
    public void addParameterizedRoute(String method, String pattern, RequestHandler handler) {
        addParameterizedRoute(method, pattern, handler, null);
    }
    
    public void addParameterizedRoute(String method, String pattern, RequestHandler handler, String description) {
        var route = new Route(handler, description, pattern);
        parameterizedRoutes.add(route);
    }
    
    public void addRoute(String path, RequestHandler handler) {
        addRoute("GET", path, handler, null);
    }
    
    public void addRoute(String path, RequestHandler handler, String description) {
        addRoute("GET", path, handler, description);
    }
    
    // HTTP method-specific convenience methods
    public void GET(String path, RequestHandler handler) { // NOSONAR
        addRoute("GET", path, handler, null);
    }
    
    public void GET(String path, RequestHandler handler, String description) { // NOSONAR
        addRoute("GET", path, handler, description);
    }
    
    public void POST(String path, RequestHandler handler) { // NOSONAR
        addRoute("POST", path, handler, null);
    }
    
    public void POST(String path, RequestHandler handler, String description) { // NOSONAR
        addRoute("POST", path, handler, description);
    }
    
    public void PUT(String path, RequestHandler handler) { // NOSONAR
        addRoute("PUT", path, handler, null);
    }
    
    public void PUT(String path, RequestHandler handler, String description) { // NOSONAR
        addRoute("PUT", path, handler, description);
    }
    
    public void DELETE(String path, RequestHandler handler) { // NOSONAR
        addRoute("DELETE", path, handler, null);
    }
    
    public void DELETE(String path, RequestHandler handler, String description) { // NOSONAR
        addRoute("DELETE", path, handler, description);
    }
    
    public void PATCH(String path, RequestHandler handler) { // NOSONAR
        addRoute("PATCH", path, handler, null);
    }
    
    public void PATCH(String path, RequestHandler handler, String description) { // NOSONAR
        addRoute("PATCH", path, handler, description);
    }
    
    public void HEAD(String path, RequestHandler handler) { // NOSONAR
        addRoute("HEAD", path, handler, null);
    }
    
    public void HEAD(String path, RequestHandler handler, String description) { // NOSONAR
        addRoute("HEAD", path, handler, description);
    }
    
    public void OPTIONS(String path, RequestHandler handler) { // NOSONAR
        addRoute("OPTIONS", path, handler, null);
    }
    
    public void OPTIONS(String path, RequestHandler handler, String description) { // NOSONAR
        addRoute("OPTIONS", path, handler, description);
    }
    
    /**
     * Add middleware to the router. Middleware will be applied in the order they are added.
     * @param middleware The middleware to add (a function that takes a RequestHandler and returns a RequestHandler)
     */
    public void use(java.util.function.Function<RequestHandler, RequestHandler> middleware) {
        this.middleware.add(middleware);
    }
    
    /**
     * Add multiple middleware to the router at once (like Chi's variadic approach).
     * @param middlewares The middleware functions to add
     */
    @SafeVarargs
    public final void use(java.util.function.Function<RequestHandler, RequestHandler>... middlewares) {
        for (var mw : middlewares) {
            this.middleware.add(mw);
        }
    }
    
    public void clearMiddleware() {
        this.middleware.clear();
    }
    
    public int middlewareCount() {
        return this.middleware.size();
    }
    
    /**
     * Apply all registered middleware to a handler (like Chi's approach).
     * Middleware is applied in reverse order to maintain the expected execution order.
     * @param handler The original handler
     * @return A new handler wrapped with all middleware
     */
    private RequestHandler applyMiddleware(RequestHandler handler) {
        var wrappedHandler = handler;
        // Apply middleware in reverse order (like Chi does)
        for (int i = middleware.size() - 1; i >= 0; i--) {
            wrappedHandler = middleware.get(i).apply(wrappedHandler);
        }
        return wrappedHandler;
    }
    
    Map<String, Route> getRoutes() {
        var allRoutes = new HashMap<String, Route>(exactRoutes);
        
        // Add parameterized routes with their patterns as keys
        for (var route : parameterizedRoutes) {
            var key = "GET " + route.getPattern(); // Default to GET for display
            allRoutes.put(key, route);
        }
        
        return allRoutes;
    }
    
    public void run() throws IOException {
        try (var serverSocket = new ServerSocket(config.getPort())) {
            System.out.println("HTTP Server running on " + config.getServerUrl());
            
            while (true) {
                var clientSocket = serverSocket.accept();
                executor.submit(() -> handleSocketRequest(clientSocket));
            }
        }
    }
    
    private void handleSocketRequest(Socket clientSocket) {
        try (clientSocket; var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); var out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            try {
                // Read the HTTP request
                var requestLine = in.readLine();
                if (requestLine == null) return;

                var requestParts = requestLine.split(" ");
                if (requestParts.length < 2) return;

                var method = requestParts[0];
                var fullPath = requestParts[1];
                
                var path = extractPath(fullPath);
                var queryParams = parseQueryParameters(fullPath);

                var headers = parseHeaders(in);

                // Handle the request based on path
                var response = handleRequest(method, path, headers, queryParams);

                // Send HTTP response
                out.println("HTTP/1.1 " + response.getStatusCode());
                out.println(String.format("%s: %s; charset=UTF-8", HttpHeader.CONTENT_TYPE.getValue(), response.getMediaType().getValue()));
                out.println(String.format("%s: %d", HttpHeader.CONTENT_LENGTH.getValue(), response.getBody().getBytes(StandardCharsets.UTF_8).length));
                out.println(String.format("%s: close", HttpHeader.CONNECTION.getValue()));
                out.println();
                out.println(response.getBody());

            } catch (IOException e) {
                System.err.println("Error handling request: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    public void printRouteDescriptions() {
        logger.info("Application Routes:");
        
        // Print exact routes
        for (var entry : exactRoutes.entrySet()) {
            var routeKey = entry.getKey();
            var parts = routeKey.split(" ", 2);
            if (parts.length == 2) {
                var method = parts[0];
                var path = parts[1];
                var route = entry.getValue();
                var description = route.getDescriptionOrDefault();
                logger.info(String.format("  %s %s - %s", method, path, description));
            }
        }
        
        // Print parameterized routes sorted by pattern length (longest first)
        var sortedRoutes = new ArrayList<>(parameterizedRoutes);
        sortedRoutes.sort(Comparator.comparing((Route r) -> r.getPattern() != null ? r.getPattern().length() : 0).reversed());
        
        for (var route : sortedRoutes) {
            var pattern = route.getPattern();
            var description = route.getDescriptionOrDefault();
            logger.info(String.format("  GET %s - %s", pattern, description));
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    // ===== HTTP Request Parsing Methods =====
    
    /**
     * Parses HTTP headers from the input stream.
     * @param in the BufferedReader containing the HTTP request
     * @return a Map of header names to header values
     * @throws IOException if there's an error reading from the input stream
     */
    private Map<String, String> parseHeaders(BufferedReader in) throws IOException {
        var headers = new HashMap<String, String>();
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            var colonIndex = headerLine.indexOf(':');
            if (colonIndex > 0) {
                var headerName = headerLine.substring(0, colonIndex).trim();
                var headerValue = headerLine.substring(colonIndex + 1).trim();
                headers.put(headerName, headerValue);
            }
        }
        return headers;
    }
    
    /**
     * Extracts the base path from a full URL path, removing query parameters.
     * @param fullPath the full path including query parameters (e.g., "/api/users?name=john&age=25")
     * @return the base path without query parameters (e.g., "/api/users")
     */
    private String extractPath(String fullPath) {
        var questionMarkIndex = fullPath.indexOf('?');
        return questionMarkIndex != -1 ? fullPath.substring(0, questionMarkIndex) : fullPath;
    }
    
    /**
     * Parses query parameters from a full URL path.
     * @param fullPath the full path including query parameters (e.g., "/api/users?name=john&age=25")
     * @return a Map of query parameter names to their values
     */
    private Map<String, String> parseQueryParameters(String fullPath) {
        var queryParams = new HashMap<String, String>();
        var questionMarkIndex = fullPath.indexOf('?');
        
        if (questionMarkIndex == -1) {
            return queryParams; // No query parameters
        }
        
        var queryString = fullPath.substring(questionMarkIndex + 1);
        if (queryString.isEmpty()) {
            return queryParams; // Empty query string
        }
        
        var pairs = queryString.split("&");
        for (var pair : pairs) {
            var equalIndex = pair.indexOf('=');
            if (equalIndex != -1) {
                var key = pair.substring(0, equalIndex);
                var value = pair.substring(equalIndex + 1);
                // URL decode the key and value
                queryParams.put(urlDecode(key), urlDecode(value));
            } else if (!pair.isEmpty()) {
                // Handle parameters without values (e.g., ?flag)
                queryParams.put(urlDecode(pair), "");
            }
        }
        
        return queryParams;
    }
    
    /**
     * Simple URL decoding for query parameters.
     * @param encoded the URL-encoded string
     * @return the decoded string
     */
    private String urlDecode(String encoded) {
        try {
            return java.net.URLDecoder.decode(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encoded; // Return original if decoding fails
        }
    }
}
