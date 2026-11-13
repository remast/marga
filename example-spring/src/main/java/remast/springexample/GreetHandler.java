package remast.springexample;

import org.springframework.stereotype.Component;

import remast.marga.MediaType;
import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;
import remast.marga.spring.MargaRoute;

@Component
@MargaRoute(path = "/greet/${name}", description = "Greet by name using an injected Spring service")
public class GreetHandler implements RequestHandler {
    private final GreetingService greetings;

    public GreetHandler(GreetingService greetings) {
        this.greetings = greetings;
    }

    @Override
    public Response handle(Request request) {
        var name = request.pathParam("name");
        var body = "<html><body><h1>Greetings!</h1><p>" + greetings.greet(name)
                + "</p><p>Method: " + request.getMethod() + "</p><p>Path: " + request.getPath()
                + "</p><a href='/'>Back</a></body></html>";
        return Response.ok(body).mediaType(MediaType.TEXT_HTML);
    }
}
