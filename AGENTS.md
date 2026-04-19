# AGENTS

## Stack and Layout
- Java 21 multi-module Maven repo (`pom.xml` parent with modules `marga` and `example`).
- Core library code lives in `marga/src/main/java/remast/marga`; main entrypoint class is `HttpRouter`.
- Example runnable app lives in `example/src/main/java/remast/App.java` and depends on module `marga`.

## Commands agents should use
- Full local verification (matches CI intent): `mvn clean compile package`.
- Run all tests: `mvn test`.
- Run only library tests: `mvn -pl marga test`.
- Run one test class/method: `mvn -pl marga -Dtest=HttpRouterTest test` or `mvn -pl marga -Dtest=HttpRouterTest#addExactRoute test`.
- Build and run example fat jar: `mvn -pl example -am package` then `java -jar example/target/example-app.jar`.

## CI/release behavior that affects changes
- CI workflow (`.github/workflows/ci.yml`) runs on `main` and `develop` and executes `mvn clean compile package --batch-mode`.
- Maven Central publish workflow (`.github/workflows/maven-publish.yml`) sets version from release tag via `mvn versions:set -DnewVersion=<tag>` then deploys with `mvn -P release deploy -DskipTests`.

## Coding Guidelines
- Prefer `var` for local variables.
- Use Java 21 features when appropriate.
- Keep comments minimal; only add them when naming is not enough.
- Do not prefix JUnit test method names with `test`.
