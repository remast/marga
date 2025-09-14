package remast;

import java.time.LocalDateTime;

import remast.marga.MediaType;
import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

public class ApiHandler implements RequestHandler {
    @Override
    public Response handle(Request request) {
        var body = String.format(
            "<html><body><h1>API Information</h1>" +
            "<p><strong>Methode:</strong> %s</p>" +
            "<p><strong>Pfad:</strong> %s</p>" +
            "<p><strong>Zeitstempel:</strong> %s</p>" +
            "<a href='/'>Zurück zur Startseite</a></body></html>",
            request.getMethod(), request.getPath(), LocalDateTime.now()
        );
        return Response.ok(body).mediaType(MediaType.TEXT_HTML);
    }
}
