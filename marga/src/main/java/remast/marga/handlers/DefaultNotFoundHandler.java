package remast.marga.handlers;

import remast.marga.RequestHandler;
import remast.marga.Request;
import remast.marga.Response;
import remast.marga.HttpStatus;

public class DefaultNotFoundHandler implements RequestHandler {
    @Override
    public Response handle(Request request) {
        return new Response(
            "<html><body><h1>404 - Not Found</h1><p>The requested resource was not found.</p></body></html>",
            HttpStatus.NOT_FOUND);
    }
}
