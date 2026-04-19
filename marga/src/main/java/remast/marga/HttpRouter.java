package remast.marga;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public class HttpRouter {
    private final Router router;
    private final HttpServer server;

    public HttpRouter() {
        this(Config.defaults());
    }

    public HttpRouter(Config config) {
        this.router = new Router();
        this.server = new HttpServer(router, config);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public Response handleRequest(String method, String path) {
        return router.handleRequest(method, path);
    }

    public Response handleRequest(String method, String path, Map<String, String> headers) {
        return router.handleRequest(method, path, headers);
    }

    public Response handleRequest(String method, String path, Map<String, String> headers, Map<String, String> queryParams) {
        return router.handleRequest(method, path, headers, queryParams);
    }

    public void addRoute(String method, String path, RequestHandler handler) {
        router.addRoute(method, path, handler);
    }

    public void addRoute(String method, String path, RequestHandler handler, String description) {
        router.addRoute(method, path, handler, description);
    }

    public void addParameterizedRoute(String method, String pattern, RequestHandler handler) {
        router.addParameterizedRoute(method, pattern, handler);
    }

    public void addParameterizedRoute(String method, String pattern, RequestHandler handler, String description) {
        router.addParameterizedRoute(method, pattern, handler, description);
    }

    public void addRoute(String path, RequestHandler handler) {
        router.addRoute(path, handler);
    }

    public void addRoute(String path, RequestHandler handler, String description) {
        router.addRoute(path, handler, description);
    }

    public void GET(String path, RequestHandler handler) { // NOSONAR
        router.GET(path, handler);
    }

    public void GET(String path, RequestHandler handler, String description) { // NOSONAR
        router.GET(path, handler, description);
    }

    public void POST(String path, RequestHandler handler) { // NOSONAR
        router.POST(path, handler);
    }

    public void POST(String path, RequestHandler handler, String description) { // NOSONAR
        router.POST(path, handler, description);
    }

    public void PUT(String path, RequestHandler handler) { // NOSONAR
        router.PUT(path, handler);
    }

    public void PUT(String path, RequestHandler handler, String description) { // NOSONAR
        router.PUT(path, handler, description);
    }

    public void DELETE(String path, RequestHandler handler) { // NOSONAR
        router.DELETE(path, handler);
    }

    public void DELETE(String path, RequestHandler handler, String description) { // NOSONAR
        router.DELETE(path, handler, description);
    }

    public void PATCH(String path, RequestHandler handler) { // NOSONAR
        router.PATCH(path, handler);
    }

    public void PATCH(String path, RequestHandler handler, String description) { // NOSONAR
        router.PATCH(path, handler, description);
    }

    public void HEAD(String path, RequestHandler handler) { // NOSONAR
        router.HEAD(path, handler);
    }

    public void HEAD(String path, RequestHandler handler, String description) { // NOSONAR
        router.HEAD(path, handler, description);
    }

    public void OPTIONS(String path, RequestHandler handler) { // NOSONAR
        router.OPTIONS(path, handler);
    }

    public void OPTIONS(String path, RequestHandler handler, String description) { // NOSONAR
        router.OPTIONS(path, handler, description);
    }

    public void use(Function<RequestHandler, RequestHandler> middleware) {
        router.use(middleware);
    }

    @SafeVarargs
    public final void use(Function<RequestHandler, RequestHandler>... middlewares) {
        router.use(middlewares);
    }

    public void clearMiddleware() {
        router.clearMiddleware();
    }

    public int middlewareCount() {
        return router.middlewareCount();
    }

    Map<String, Route> getRoutes() {
        return router.getRoutes();
    }

    public void printRouteDescriptions() {
        router.printRouteDescriptions();
    }

    public void run() throws IOException {
        server.run();
    }

    public int getPort() {
        return server.getBoundPort();
    }

    public void shutdown() {
        server.shutdown();
    }
}
