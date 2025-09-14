package remast;

import java.io.IOException;

import remast.marga.HttpRouter;

public class App {    
    public static void main(String[] args) throws IOException {
        var router = new HttpRouter();
        
        registerRoutes(router);        
        router.printRouteDescriptions();
        
        router.run();
    }
    
    private static void registerRoutes(HttpRouter router) {
        router.addRoute("GET", "/", new RootHandler(), "Root page");
        router.addRoute("GET", "/hello", new HelloHandler(), "Greeting");
        router.addRoute("GET", "/api", new ApiHandler(), "API information");
        router.addRoute("GET", "/greet", new GreetHandler(), "Greet endpoint");
        router.addRoute("GET", "/greet/${name}", new GreetHandler(), "Greet with name parameter");
    }
    
}
