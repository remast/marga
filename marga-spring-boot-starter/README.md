# Marga Spring Boot Starter

The starter registers Spring-managed beans that implement Marga's `RequestHandler` interface and are annotated with `@MargaRoute`. Handlers receive Marga `Request` objects and return Marga `Response` objects; Spring services can be injected into handlers normally.

```java
@Component
@MargaRoute(method = "GET", path = "/greet/${name}", description = "Greet a person")
class GreetHandler implements RequestHandler {
    private final GreetingService greetings;

    GreetHandler(GreetingService greetings) {
        this.greetings = greetings;
    }

    @Override
    public Response handle(Request request) {
        return Response.ok(greetings.greet(request.pathParam("name")));
    }
}
```

Routes use Marga's `${name}` path parameter format. `@MargaRoute` is repeatable, so a handler can register multiple paths. The starter configures and starts Marga as part of the Spring application lifecycle.

Configure the server with these Spring properties:

| Property | Default |
| --- | --- |
| `marga.host` | `localhost` |
| `marga.port` | `8080` |
| `marga.read-timeout-ms` | `30000` |
| `marga.accept-backlog` | `0` |
| `marga.max-request-body-bytes` | `1048576` |

Add `io.github.remast:marga-spring-boot-starter` to a Spring Boot application to enable the auto-configuration. The application is responsible for component-scanning its handlers and services.
