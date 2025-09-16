package remast;

import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

public class RootHandler implements RequestHandler {
    @Override
    public Response handle(Request request) {
        return Response.ok("Welcome to Marga!!");
    }
}
