package remast;

import java.io.IOException;

import remast.marga.HttpRouter;
import remast.marga.Response;
import remast.marga.middleware.LoggingMiddleware;
import remast.marga.middleware.TimingMiddleware;

public class App {    
    public static void main(String[] args) throws IOException {
        var router = new HttpRouter();
        
        // Add middleware
        router.use(LoggingMiddleware.create());
        router.use(TimingMiddleware.create());
        
        registerRoutes(router);        
        router.printRouteDescriptions();
        
        router.run();
    }
    
    private static void registerRoutes(HttpRouter router) {
        // Using the new HTTP method-specific syntax
        router.GET("/", request -> Response.ok("Welcome to Marga!"), "Root page");
        router.GET("/hello", new HelloHandler(), "Greeting");
        router.GET("/api", new ApiHandler(), "API information");
        router.GET("/greet", new GreetHandler(), "Greet endpoint");
        router.GET("/greet/${name}", new GreetHandler(), "Greet with name parameter");
    }
    
}
