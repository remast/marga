# Marga Spring Boot Example

This application uses the Marga Spring Boot starter. The starter registers Spring-managed beans that implement `RequestHandler` and have one or more `@MargaRoute` annotations.

```sh
mvn -pl example-spring -am package
java -jar example-spring/target/example-spring-0.0.5-SNAPSHOT.jar
```

The Marga server listens on `http://localhost:8081` by default in this example. See `sample-requests.http` for sample requests.

Routes use Marga's `${name}` path parameter syntax, and handler implementations receive the full Marga `Request` and return a Marga `Response`. Services can be injected into handlers through normal Spring constructor injection.

The server settings can be configured with `marga.host`, `marga.port`, `marga.read-timeout-ms`, `marga.accept-backlog`, and `marga.max-request-body-bytes`.
