package remast.marga.middleware;

import remast.marga.RequestHandler;

import java.util.logging.Logger;

/**
 * Middleware that adds timing information to response headers.
 * This demonstrates how middleware can modify responses.
 */
public class TimingMiddleware {
    private static final Logger logger = Logger.getLogger(TimingMiddleware.class.getName());
    private static final String TIMING_HEADER = "X-Response-Time";
    
    /**
     * Creates a timing middleware function.
     * @return A function that takes a RequestHandler and returns a RequestHandler with timing
     */
    public static java.util.function.Function<RequestHandler, RequestHandler> create() {
        return handler -> request -> {
            var startTime = System.currentTimeMillis();
            var response = handler.handle(request);
            var duration = System.currentTimeMillis() - startTime;
            
            // Note: In a real implementation, you'd want to add headers to the response
            // For now, we'll just log the timing information
            logger.info(String.format("Request to %s %s took %dms", 
                request.getMethod(), request.getPath(), duration));
            
            return response;
        };
    }
}
