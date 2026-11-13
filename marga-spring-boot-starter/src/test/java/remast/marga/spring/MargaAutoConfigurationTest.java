package remast.marga.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import remast.marga.HttpRouter;
import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

class MargaAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MargaAutoConfiguration.class))
            .withUserConfiguration(HandlerConfiguration.class)
            .withPropertyValues("marga.port=0");

    @Test
    void registersRoutesOnSpringManagedRequestHandlers() {
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());
            var router = context.getBean(HttpRouter.class);
            var response = router.handleRequest("GET", "/hello");

            assertEquals(200, response.getStatusCode());
            assertEquals("hello from spring", response.getBody());
        });
    }

    @Test
    void registersParameterizedRoutes() {
        contextRunner.run(context -> {
            var response = context.getBean(HttpRouter.class).handleRequest("GET", "/greet/Marga");

            assertEquals(200, response.getStatusCode());
            assertEquals("Hello, Marga!", response.getBody());
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class HandlerConfiguration {
        @Bean
        RequestHandler helloHandler() {
            return new HelloHandler();
        }

        @Bean
        GreetingService greetingService() {
            return new GreetingService();
        }

        @Bean
        RequestHandler greetHandler(GreetingService greetingService) {
            return new GreetHandler(greetingService);
        }
    }

    @MargaRoute(path = "/hello")
    static class HelloHandler implements RequestHandler {
        @Override
        public Response handle(Request request) {
            return Response.ok("hello from spring");
        }
    }

    static class GreetingService {
        String greet(String name) {
            return "Hello, " + name + "!";
        }
    }

    @MargaRoute(path = "/greet/${name}")
    static class GreetHandler implements RequestHandler {
        private final GreetingService greetingService;

        GreetHandler(GreetingService greetingService) {
            this.greetingService = greetingService;
        }

        @Override
        public Response handle(Request request) {
            return Response.ok(greetingService.greet(request.pathParam("name")));
        }
    }
}
