package remast;

import remast.marga.MediaType;
import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

public class GreetHandler implements RequestHandler {

    @Override
    public Response handle(Request request) {
        var name = request.pathParam("name");
        var greeting = name != null ? "Hello, " + name + "!" : "Hello, stranger!";
        
        var body = "<html><body><h1>Greetings!</h1>" +
                   "<p>" + greeting + "</p>" +
                   "<p>Method: " + request.getMethod() + "</p>" +
                   "<p>Path: " + request.getPath() + "</p>" +
                   "<p>Name parameter: " + (name != null ? name : "not provided") + "</p>" +
                   "<a href='/'>Zurück zur Startseite</a></body></html>";
        return Response.ok(body).mediaType(MediaType.TEXT_HTML);
    }

}
