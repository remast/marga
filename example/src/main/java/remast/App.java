package remast;

import java.io.IOException;

import remast.marga.HttpRouter;
import remast.marga.middleware.LoggingMiddleware;
import remast.marga.middleware.TimingMiddleware;

public class App {    
    public static void main(String[] args) throws IOException {
        var router = new HttpRouter();
        
        // Add middleware (order matters - they will be applied in the order added)
        router.use(new LoggingMiddleware());
        router.use(new TimingMiddleware());
        
        registerRoutes(router);        
        router.printRouteDescriptions();
        
        router.run();
    }
    
    private static void registerRoutes(HttpRouter router) {
        // Using the new HTTP method-specific syntax
        router.GET("/", new RootHandler(), "Root page");
        router.GET("/hello", new HelloHandler(), "Greeting");
        router.GET("/api", new ApiHandler(), "API information");
        router.GET("/greet", new GreetHandler(), "Greet endpoint");
        router.GET("/greet/${name}", new GreetHandler(), "Greet with name parameter");
        
        // You can also mix with the old syntax if needed
        // router.addRoute("GET", "/", new RootHandler(), "Root page");
    }
    
}
