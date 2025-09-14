package remast;

import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

public class RootHandler implements RequestHandler {
    @Override
    public Response handle(Request request) {
        var body = "<html><body><h1>Freddy BBQ Server</h1>" +
                   "<p>Willkommen bei Freddy BBQ!</p>" +
                   "<ul>" +
                   "<li><a href='/hello'>Hello</a></li>" +
                   "<li><a href='/api'>API</a></li>" +
                   "<li><a href='/greet/John'>Greet John</a></li>" +
                   "<li><a href='/greet/Jane'>Greet Jane</a></li>" +
                   "</ul></body></html>";
        return Response.ok(body);
    }
}
