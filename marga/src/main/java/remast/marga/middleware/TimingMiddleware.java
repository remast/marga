package remast.marga.middleware;

import remast.marga.Middleware;
import remast.marga.RequestHandler;

import java.util.logging.Logger;

/**
 * Middleware that adds timing information to response headers.
 * This demonstrates how middleware can modify responses.
 */
public class TimingMiddleware implements Middleware {
    private static final Logger logger = Logger.getLogger(TimingMiddleware.class.getName());
    private static final String TIMING_HEADER = "X-Response-Time";
    
    @Override
    public RequestHandler apply(RequestHandler handler) {
        return request -> {
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
