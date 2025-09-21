package remast;

import java.time.LocalDateTime;

import remast.marga.MediaType;
import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

public class ApiHandler implements RequestHandler {
    @Override
    public Response handle(Request request) {
        var userAgent = request.header("User-Agent");
        var contentType = request.header("Content-Type");
        var hasAuth = request.hasHeader("Authorization");
        
        var body = String.format(
            "<html><body><h1>API Information</h1>" +
            "<p><strong>Methode:</strong> %s</p>" +
            "<p><strong>Pfad:</strong> %s</p>" +
            "<p><strong>Zeitstempel:</strong> %s</p>" +
            "<h2>Request Headers</h2>" +
            "<p><strong>User-Agent:</strong> %s</p>" +
            "<p><strong>Content-Type:</strong> %s</p>" +
            "<p><strong>Has Authorization:</strong> %s</p>" +
            "<a href='/'>Zurück zur Startseite</a></body></html>",
            request.getMethod(), 
            request.getPath(), 
            LocalDateTime.now(),
            userAgent != null ? userAgent : "Not provided",
            contentType != null ? contentType : "Not provided",
            hasAuth ? "Yes" : "No"
        );
        return Response.ok(body).mediaType(MediaType.TEXT_HTML);
    }
}
