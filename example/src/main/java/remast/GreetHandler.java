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

        var body = """
                <html><body><h1>Greetings!</h1>
                <p>%s</p>
                <p>Method: %s</p>
                <p>Path: %s</p>
                <p>Name parameter: %s</p>
                <a href='/'>Back</a></body></html>
                """.formatted(
                greeting,
                request.getMethod(),
                request.getPath(),
                name != null ? name : "not provided"
        );
        return Response.ok(body).mediaType(MediaType.TEXT_HTML);
    }

}
