package remast.marga;

/**
 * Middleware interface for processing requests before they reach the actual handler.
 * Middleware can perform pre-processing, post-processing, or both.
 * 
 * @param handler The next handler in the chain
 * @return A new RequestHandler that wraps the original handler with middleware logic
 */
@FunctionalInterface
public interface Middleware {
    RequestHandler apply(RequestHandler handler);
}
