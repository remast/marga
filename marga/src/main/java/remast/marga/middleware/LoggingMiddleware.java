package remast.marga.middleware;

import remast.marga.Middleware;
import remast.marga.RequestHandler;

import java.util.logging.Logger;

/**
 * Middleware that logs each request.
 * Logs the HTTP method, path, and response status.
 */
public class LoggingMiddleware implements Middleware {
    private static final Logger logger = Logger.getLogger(LoggingMiddleware.class.getName());
    
    @Override
    public RequestHandler apply(RequestHandler handler) {
        return request -> {
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
