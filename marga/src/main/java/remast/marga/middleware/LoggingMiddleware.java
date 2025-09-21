package remast.marga.middleware;

import remast.marga.RequestHandler;

import java.util.logging.Logger;

/**
 * Middleware that logs each request.
 * Logs the HTTP method, path, and response status.
 */
public class LoggingMiddleware {
    private static final Logger logger = Logger.getLogger(LoggingMiddleware.class.getName());
    
    /**
     * Creates a logging middleware function.
     * @return A function that takes a RequestHandler and returns a RequestHandler with logging
     */
    public static java.util.function.Function<RequestHandler, RequestHandler> create() {
        return handler -> request -> {
            logger.info(String.format("%s %s", 
                request.getMethod(), request.getPath()));
            
            try {
                var response = handler.handle(request);
                logger.info(String.format("%s %s - %d", 
                    request.getMethod(), request.getPath(), response.getStatusCode()));
                return response;
            } catch (Exception e) {
                logger.severe(String.format("%s %s - ERROR: %s", 
                    request.getMethod(), request.getPath(), e.getMessage()));
                throw e;
            }
        };
    }
}
