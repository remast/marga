package remast.marga;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.logging.Logger;
import remast.marga.handlers.DefaultNotFoundHandler;

public class Router {
    private static final Logger logger = Logger.getLogger(Router.class.getName());
    private static final Comparator<Route> ROUTE_SPECIFICITY = Router::compareSpecificity;
    private static final Comparator<Route> ROUTE_SPECIFICITY_DESC = ROUTE_SPECIFICITY.reversed();

    // Interned canonical method names. Lookup by identity / equals is cheap; skips trim/upper on the hot path.
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String PATCH = "PATCH";
    private static final String HEAD = "HEAD";
    private static final String OPTIONS = "OPTIONS";

    // path -> (method -> Route). Removes the per-request "method + ' ' + path" concat and
    // makes 405 allowed-method collection O(1) for the exact-route portion.
    private final Map<String, Map<String, Route>> exactRoutesByPath;
    // Parameterized routes, partitioned by method and pre-sorted by specificity (most specific first).
    // First-match-wins during lookup replaces the prior stream/filter/max-per-request.
    private final Map<String, List<Route>> parameterizedByMethod;
    private final List<Function<RequestHandler, RequestHandler>> middleware;
    private RequestHandler notFoundHandler;

    // Middleware-caching state. Middleware is expected to be registered once at startup
    // (before the first request). Once a request is served, the middleware chain is frozen:
    // every registered handler is pre-wrapped once, the not-found and 405 handlers are cached,
    // and any further mutation of the middleware list throws IllegalStateException.
    private boolean middlewareFrozen;
    private RequestHandler wrappedNotFoundHandler;
    private final Map<String, RequestHandler> wrappedMethodNotAllowedByAllow = new HashMap<>();

    public Router() {
        this.exactRoutesByPath = new LinkedHashMap<>();
        this.parameterizedByMethod = new HashMap<>();
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
        if (!middlewareFrozen) {
            freezeMiddleware();
        }

        var method = request.getMethod();
        var path = request.getPath();

        var methodsForPath = exactRoutesByPath.get(path);
        if (methodsForPath != null) {
            var exactRoute = methodsForPath.get(method);
            if (exactRoute != null) {
                return exactRoute.getWrappedHandler().handle(request);
            }
        }

        var candidates = parameterizedByMethod.get(method);
        if (candidates != null) {
            for (var route : candidates) {
                if (route.matchInto(path, request)) {
                    return route.getWrappedHandler().handle(request);
                }
            }
        }

        var allowedMethods = collectAllowedMethodsForPath(path);
        if (!allowedMethods.isEmpty()) {
            var allowValue = String.join(", ", allowedMethods);
            return wrappedMethodNotAllowedHandler(allowValue).handle(request);
        }

        return wrappedNotFoundHandler.handle(request);
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

        var route = new Route(normalizedMethod, handler, description);
        wrapIfFrozen(route);
        var methodsForPath = exactRoutesByPath.computeIfAbsent(path, ignored -> new LinkedHashMap<>());
        var existing = methodsForPath.put(normalizedMethod, route);
        if (existing != null) {
            logger.warning("Replacing duplicate exact route: " + normalizedMethod + " " + path);
        }
    }

    public void addParameterizedRoute(String method, String pattern, RequestHandler handler) {
        addParameterizedRoute(method, pattern, handler, null);
    }

    public void addParameterizedRoute(String method, String pattern, RequestHandler handler, String description) {
        var normalizedMethod = normalizeMethod(method);
        var route = new Route(normalizedMethod, handler, description, pattern);
        wrapIfFrozen(route);
        var routes = parameterizedByMethod.computeIfAbsent(normalizedMethod, ignored -> new ArrayList<>());
        for (var i = 0; i < routes.size(); i++) {
            var existing = routes.get(i);
            if (pattern.equals(existing.getPattern())) {
                logger.warning("Replacing duplicate parameterized route: " + normalizedMethod + " " + pattern);
                routes.set(i, route);
                routes.sort(ROUTE_SPECIFICITY_DESC);
                return;
            }
        }
        routes.add(route);
        routes.sort(ROUTE_SPECIFICITY_DESC);
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
        if (middlewareFrozen) {
            throw new IllegalStateException("Cannot change notFoundHandler after the first request has been served");
        }
        this.notFoundHandler = notFoundHandler;
    }

    public void use(Function<RequestHandler, RequestHandler> middleware) {
        requireUnfrozen();
        this.middleware.add(middleware);
    }

    @SafeVarargs
    public final void use(Function<RequestHandler, RequestHandler>... middlewares) {
        requireUnfrozen();
        for (var middlewareItem : middlewares) {
            this.middleware.add(middlewareItem);
        }
    }

    public void clearMiddleware() {
        requireUnfrozen();
        this.middleware.clear();
    }

    public int middlewareCount() {
        return this.middleware.size();
    }

    Map<String, Route> getRoutes() {
        var allRoutes = new LinkedHashMap<String, Route>();
        for (var entry : exactRoutesByPath.entrySet()) {
            var path = entry.getKey();
            for (var methodEntry : entry.getValue().entrySet()) {
                allRoutes.put(methodEntry.getKey() + " " + path, methodEntry.getValue());
            }
        }
        for (var entry : parameterizedByMethod.entrySet()) {
            for (var route : entry.getValue()) {
                allRoutes.put(entry.getKey() + " " + route.getPattern(), route);
            }
        }
        return allRoutes;
    }

    public void printRouteDescriptions() {
        logger.info("Application Routes:");
        for (var entry : exactRoutesByPath.entrySet()) {
            var path = entry.getKey();
            for (var methodEntry : entry.getValue().entrySet()) {
                var route = methodEntry.getValue();
                logger.info(String.format("  %s %s - %s", methodEntry.getKey(), path, route.getDescriptionOrDefault()));
            }
        }

        var sortedParameterizedRoutes = new ArrayList<Route>();
        for (var routes : parameterizedByMethod.values()) {
            sortedParameterizedRoutes.addAll(routes);
        }
        sortedParameterizedRoutes.sort(ROUTE_SPECIFICITY_DESC);
        for (var route : sortedParameterizedRoutes) {
            logger.info(String.format("  %s %s - %s", route.getMethod(), route.getPattern(), route.getDescriptionOrDefault()));
        }
    }

    private void freezeMiddleware() {
        middlewareFrozen = true;
        wrappedNotFoundHandler = applyMiddleware(notFoundHandler);
        for (var methodsForPath : exactRoutesByPath.values()) {
            for (var route : methodsForPath.values()) {
                if (route.getWrappedHandler() == null) {
                    route.setWrappedHandler(applyMiddleware(route.getHandler()));
                }
            }
        }
        for (var routes : parameterizedByMethod.values()) {
            for (var route : routes) {
                if (route.getWrappedHandler() == null) {
                    route.setWrappedHandler(applyMiddleware(route.getHandler()));
                }
            }
        }
    }

    private void wrapIfFrozen(Route route) {
        if (middlewareFrozen) {
            route.setWrappedHandler(applyMiddleware(route.getHandler()));
        }
    }

    private RequestHandler wrappedMethodNotAllowedHandler(String allowValue) {
        var cached = wrappedMethodNotAllowedByAllow.get(allowValue);
        if (cached != null) {
            return cached;
        }
        RequestHandler baseHandler = ignored ->
            Response.methodNotAllowed("405 - Method Not Allowed")
                .header(HttpHeader.ALLOW, allowValue);
        var wrapped = applyMiddleware(baseHandler);
        wrappedMethodNotAllowedByAllow.put(allowValue, wrapped);
        return wrapped;
    }

    private RequestHandler applyMiddleware(RequestHandler handler) {
        var wrappedHandler = handler;
        for (var i = middleware.size() - 1; i >= 0; i--) {
            wrappedHandler = middleware.get(i).apply(wrappedHandler);
        }
        return wrappedHandler;
    }

    private void requireUnfrozen() {
        if (middlewareFrozen) {
            throw new IllegalStateException("Middleware cannot be modified after the first request has been served");
        }
    }

    private TreeSet<String> collectAllowedMethodsForPath(String path) {
        var allowedMethods = new TreeSet<String>();
        var methodsForPath = exactRoutesByPath.get(path);
        if (methodsForPath != null) {
            allowedMethods.addAll(methodsForPath.keySet());
        }
        for (var entry : parameterizedByMethod.entrySet()) {
            for (var route : entry.getValue()) {
                if (route.matches(path)) {
                    allowedMethods.add(entry.getKey());
                    break;
                }
            }
        }
        return allowedMethods;
    }

    private String normalizeMethod(String method) {
        if (method == null) {
            return GET;
        }
        // Fast path: already-canonical strings (avoids trim+toUpperCase allocation).
        switch (method) {
            case GET: return GET;
            case POST: return POST;
            case PUT: return PUT;
            case DELETE: return DELETE;
            case PATCH: return PATCH;
            case HEAD: return HEAD;
            case OPTIONS: return OPTIONS;
            default:
                if (method.isBlank()) {
                    return GET;
                }
                var upper = method.trim().toUpperCase();
                // Return interned constant when possible so later equals() calls can short-circuit.
                switch (upper) {
                    case GET: return GET;
                    case POST: return POST;
                    case PUT: return PUT;
                    case DELETE: return DELETE;
                    case PATCH: return PATCH;
                    case HEAD: return HEAD;
                    case OPTIONS: return OPTIONS;
                    default: return upper;
                }
        }
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
