package remast;

import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

public class NotFoundHandler implements RequestHandler {
    @Override
    public Response handle(Request request) {
        var body = "<html><body><h1>404 - Not Found</h1>" +
                   "<p>Die angeforderte Seite wurde nicht gefunden.</p>" +
                   "<p>Method: " + request.getMethod() + "</p>" +
                   "<p>Path: " + request.getPath() + "</p>" +
                   "<a href='/'>Zurück zur Startseite</a></body></html>";
        return Response.notFound(body);
    }
}
