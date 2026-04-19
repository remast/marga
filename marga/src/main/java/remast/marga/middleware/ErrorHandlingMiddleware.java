package remast.marga.middleware;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

public final class ErrorHandlingMiddleware {
    private static final Logger logger = Logger.getLogger(ErrorHandlingMiddleware.class.getName());

    private ErrorHandlingMiddleware() {
    }

    public static Function<RequestHandler, RequestHandler> create() {
        return create(ErrorHandlingMiddleware::defaultMap);
    }

    public static Function<RequestHandler, RequestHandler> create(BiFunction<Request, Throwable, Response> mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper cannot be null");
        }
        return next -> request -> {
            try {
                return next.handle(request);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Unhandled exception in handler for " + request.getMethod() + " " + request.getPath(), t);
                try {
                    var mapped = mapper.apply(request, t);
                    if (mapped != null) {
                        return mapped;
                    }
                } catch (Throwable inner) {
                    logger.log(Level.SEVERE, "Error mapper itself threw; falling back to default 500", inner);
                }
                return defaultMap(request, t);
            }
        };
    }

    private static Response defaultMap(Request request, Throwable error) {
        return Response.serverError("500 - Internal Server Error");
    }
}

