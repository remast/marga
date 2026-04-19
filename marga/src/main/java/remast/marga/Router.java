package remast.marga;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.logging.Logger;
import remast.marga.handlers.DefaultNotFoundHandler;

public class Router {
    private static final Logger logger = Logger.getLogger(Router.class.getName());
    private static final Comparator<Route> ROUTE_SPECIFICITY = Router::compareSpecificity;

    private final Map<String, Route> exactRoutes;
    private final List<Route> parameterizedRoutes;
    private final List<Function<RequestHandler, RequestHandler>> middleware;
    private RequestHandler notFoundHandler;

    public Router() {
        this.exactRoutes = new HashMap<>();
        this.parameterizedRoutes = new ArrayList<>();
        this.middleware = new ArrayList<>();
        this.notFoundHandler = new DefaultNotFoundHandler();
    }

    public Response handleRequest(String method, String path) {
        var request = new Request(normalizeMethod(method), path);
        return handleRequestInternal(request);
    }

    public Response handleRequest(String method, String path, Map<String, String> headers) {
        var request = new Request(normalizeMethod(method), path, headers);
        return handleRequestInternal(request);
    }

    public Response handleRequest(String method, String path, Map<String, String> headers, Map<String, String> queryParams) {
        var request = new Request(normalizeMethod(method), path, headers, queryParams);
        return handleRequestInternal(request);
    }

    public Response handleRequest(Request request) {
        return handleRequestInternal(request);
    }

    private Response handleRequestInternal(Request request) {
        var method = request.getMethod();
        var path = request.getPath();

        var routeKey = method + " " + path;
        var exactRoute = exactRoutes.get(routeKey);
        if (exactRoute != null) {
            return applyMiddleware(exactRoute.getHandler()).handle(request);
        }

        var bestParameterizedRoute = parameterizedRoutes.stream()
            .filter(route -> route.getMethod().equals(method))
            .filter(route -> route.matches(path))
            .max(ROUTE_SPECIFICITY)
            .orElse(null);

        if (bestParameterizedRoute != null) {
            bestParameterizedRoute.extractParameters(path, request);
            return applyMiddleware(bestParameterizedRoute.getHandler()).handle(request);
        }

        var allowedMethods = collectAllowedMethodsForPath(path);
        if (!allowedMethods.isEmpty()) {
            var allowValue = String.join(", ", allowedMethods);
            var methodNotAllowedHandler = (RequestHandler) ignored ->
                Response.methodNotAllowed("405 - Method Not Allowed")
                    .header(HttpHeader.ALLOW, allowValue);
            return applyMiddleware(methodNotAllowedHandler).handle(request);
        }

        return applyMiddleware(notFoundHandler).handle(request);
    }

    public void addRoute(String method, String path, RequestHandler handler) {
        addRoute(method, path, handler, null);
    }

    public void addRoute(String method, String path, RequestHandler handler, String description) {
        var normalizedMethod = normalizeMethod(method);
        if (path.contains("${")) {
            addParameterizedRoute(normalizedMethod, path, handler, description);
            return;
        }

        var routeKey = normalizedMethod + " " + path;
        var existingRoute = exactRoutes.put(routeKey, new Route(normalizedMethod, handler, description));
        if (existingRoute != null) {
            logger.warning("Replacing duplicate exact route: " + routeKey);
        }
    }

    public void addParameterizedRoute(String method, String pattern, RequestHandler handler) {
        addParameterizedRoute(method, pattern, handler, null);
    }

    public void addParameterizedRoute(String method, String pattern, RequestHandler handler, String description) {
        var normalizedMethod = normalizeMethod(method);
        var route = new Route(normalizedMethod, handler, description, pattern);
        for (var i = 0; i < parameterizedRoutes.size(); i++) {
            var existing = parameterizedRoutes.get(i);
            if (existing.getMethod().equals(normalizedMethod)
                && pattern.equals(existing.getPattern())) {
                logger.warning("Replacing duplicate parameterized route: " + normalizedMethod + " " + pattern);
                parameterizedRoutes.set(i, route);
                return;
            }
        }
        parameterizedRoutes.add(route);
    }

    public void addRoute(String path, RequestHandler handler) {
        addRoute("GET", path, handler, null);
    }

    public void addRoute(String path, RequestHandler handler, String description) {
        addRoute("GET", path, handler, description);
    }

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

    public void setNotFoundHandler(RequestHandler notFoundHandler) {
        if (notFoundHandler == null) {
            throw new IllegalArgumentException("notFoundHandler cannot be null");
        }
        this.notFoundHandler = notFoundHandler;
    }

    public void use(Function<RequestHandler, RequestHandler> middleware) {
        this.middleware.add(middleware);
    }

    @SafeVarargs
    public final void use(Function<RequestHandler, RequestHandler>... middlewares) {
        for (var middlewareItem : middlewares) {
            this.middleware.add(middlewareItem);
        }
    }

    public void clearMiddleware() {
        this.middleware.clear();
    }

    public int middlewareCount() {
        return this.middleware.size();
    }

    Map<String, Route> getRoutes() {
        var allRoutes = new HashMap<String, Route>(exactRoutes);
        for (var route : parameterizedRoutes) {
            var key = route.getMethod() + " " + route.getPattern();
            allRoutes.put(key, route);
        }
        return allRoutes;
    }

    public void printRouteDescriptions() {
        logger.info("Application Routes:");
        for (var entry : exactRoutes.entrySet()) {
            var routeKey = entry.getKey();
            var parts = routeKey.split(" ", 2);
            if (parts.length == 2) {
                var route = entry.getValue();
                var description = route.getDescriptionOrDefault();
                logger.info(String.format("  %s %s - %s", parts[0], parts[1], description));
            }
        }

        var sortedParameterizedRoutes = new ArrayList<>(parameterizedRoutes);
        sortedParameterizedRoutes.sort((left, right) -> compareSpecificity(right, left));
        for (var route : sortedParameterizedRoutes) {
            logger.info(String.format("  %s %s - %s", route.getMethod(), route.getPattern(), route.getDescriptionOrDefault()));
        }
    }

    private RequestHandler applyMiddleware(RequestHandler handler) {
        var wrappedHandler = handler;
        for (var i = middleware.size() - 1; i >= 0; i--) {
            wrappedHandler = middleware.get(i).apply(wrappedHandler);
        }
        return wrappedHandler;
    }

    private TreeSet<String> collectAllowedMethodsForPath(String path) {
        var allowedMethods = new TreeSet<String>();
        for (var entry : exactRoutes.entrySet()) {
            var parts = entry.getKey().split(" ", 2);
            if (parts.length == 2 && parts[1].equals(path)) {
                allowedMethods.add(parts[0]);
            }
        }
        for (var route : parameterizedRoutes) {
            if (route.matches(path)) {
                allowedMethods.add(route.getMethod());
            }
        }
        return allowedMethods;
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return "GET";
        }
        return method.trim().toUpperCase();
    }

    private static int compareSpecificity(Route left, Route right) {
        var staticSegments = Integer.compare(left.getStaticSegmentCount(), right.getStaticSegmentCount());
        if (staticSegments != 0) {
            return staticSegments;
        }

        var parameterSegments = Integer.compare(right.getParameterSegmentCount(), left.getParameterSegmentCount());
        if (parameterSegments != 0) {
            return parameterSegments;
        }

        var firstParameterPosition = Integer.compare(left.getFirstParameterIndex(), right.getFirstParameterIndex());
        if (firstParameterPosition != 0) {
            return firstParameterPosition;
        }

        var leftPattern = left.getPattern() == null ? "" : left.getPattern();
        var rightPattern = right.getPattern() == null ? "" : right.getPattern();
        return leftPattern.compareTo(rightPattern);
    }
}
