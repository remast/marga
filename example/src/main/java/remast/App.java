package remast;

import remast.marga.HttpRouter;
import remast.marga.MediaType;
import remast.marga.Response;
import remast.marga.middleware.ErrorHandlingMiddleware;
import remast.marga.middleware.LoggingMiddleware;
import remast.marga.middleware.TimingMiddleware;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        var router = new HttpRouter();

        // Add middleware (error handler first so it wraps everything)
        router.use(ErrorHandlingMiddleware.create());
        router.use(LoggingMiddleware.create());
        router.use(TimingMiddleware.create());

        // Register routes
        router.GET("/",
                request -> Response.ok("<html><body><h1>Welcome to Marga!</h1></body></html>")
                        .mediaType(MediaType.TEXT_HTML),
                "Root page");
        router.GET("/hello", new HelloHandler(), "Greeting");
        router.GET("/api", new ApiHandler(), "API information");
        router.GET("/greet", new GreetHandler(), "Greet endpoint");
        router.GET("/greet/${name}", new GreetHandler(), "Greet with name parameter");

        router.printRouteDescriptions();

        router.run();
    }

}
