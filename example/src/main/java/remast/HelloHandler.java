package remast;

import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

public class HelloHandler implements RequestHandler {
    @Override
    public Response handle(Request request) {
        var body = "<html><body><h1>Hello World!</h1>" +
                   "<p>Dies ist ein HTTP-Endpoint mit der Standard Java Bibliothek.</p>" +
                   "<a href='/'>Zurück zur Startseite</a></body></html>";
        return Response.ok(body);
    }
}
