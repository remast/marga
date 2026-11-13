package remast.springexample;

import org.springframework.stereotype.Component;

import remast.marga.MediaType;
import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;
import remast.marga.spring.MargaRoute;

@Component
@MargaRoute(path = "/", description = "Welcome to Marga with Spring Boot")
public class HomeHandler implements RequestHandler {
    @Override
    public Response handle(Request request) {
        return Response.ok("<html><body><h1>Welcome to Marga + Spring Boot!</h1>"
                + "<p>Try <a href='/greet/Spring'>/greet/Spring</a></p></body></html>")
                .mediaType(MediaType.TEXT_HTML);
    }
}
