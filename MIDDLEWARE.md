# Middleware in Marga

Marga uses a functional approach to middleware similar to [Go Chi router](https://github.com/go-chi/chi), where middleware is simply a function that takes a `RequestHandler` and returns a `RequestHandler`. This eliminates the need for special middleware interfaces and makes the system more flexible and composable.

## Basic Usage

```java
var router = new HttpRouter();

// Add middleware one at a time
router.use(LoggingMiddleware.create());
router.use(TimingMiddleware.create());

// Or add multiple middleware at once
router.use(LoggingMiddleware.create(), TimingMiddleware.create());

// Add routes
router.GET("/", request -> Response.ok("Hello World"));
```

## Creating Custom Middleware

You can create custom middleware in two ways:

### 1. Using Lambda Expressions

```java
// Simple logging middleware
router.use(handler -> request -> {
    System.out.println("Request: " + request.getMethod() + " " + request.getPath());
    var response = handler.handle(request);
    System.out.println("Response: " + response.getStatusCode());
    return response;
});

// Authentication middleware
router.use(handler -> request -> {
    var authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return Response.unauthorized("Missing or invalid authorization");
    }
    return handler.handle(request);
});
```

### 2. Using Static Factory Methods

```java
public class CustomMiddleware {
    public static Function<RequestHandler, RequestHandler> create() {
        return handler -> request -> {
            // Pre-processing
            System.out.println("Before handling request");
            
            var response = handler.handle(request);
            
            // Post-processing
            System.out.println("After handling request");
            
            return response;
        };
    }
}

// Usage
router.use(CustomMiddleware.create());
```

## Middleware Execution Order

Middleware is applied in the order it's added to the router, but wrapped in reverse order (like Chi):

```java
router.use(LoggingMiddleware.create());    // Added first
router.use(TimingMiddleware.create());     // Added second
router.use(AuthMiddleware.create());       // Added third

// When a request comes in, the execution order is:
// 1. LoggingMiddleware (pre-processing)
// 2. TimingMiddleware (pre-processing)
// 3. AuthMiddleware (pre-processing)
// 4. Actual route handler
// 5. AuthMiddleware (post-processing)
// 6. TimingMiddleware (post-processing)
// 7. LoggingMiddleware (post-processing)
```

## Built-in Middleware

Marga comes with several built-in middleware implementations:

### LoggingMiddleware

Logs each request and response:

```java
router.use(LoggingMiddleware.create());
```

### TimingMiddleware

Adds timing information to responses:

```java
router.use(TimingMiddleware.create());
```

### GzipCompressionMiddleware

Compresses response bodies using gzip:

```java
var compressionMiddleware = new GzipCompressionMiddleware();
router.use(compressionMiddleware.create());
```

## Error Handling in Middleware

Middleware can handle exceptions from downstream handlers:

```java
router.use(handler -> request -> {
    try {
        return handler.handle(request);
    } catch (Exception e) {
        logger.error("Error handling request", e);
        return Response.serverError("Internal server error");
    }
});
```

## Benefits of the Functional Approach

1. **No Special Interfaces**: Middleware is just a function, no need for special interfaces
2. **Composability**: Easy to combine and compose middleware
3. **Flexibility**: Can use lambda expressions or static methods
4. **Type Safety**: Full type safety with Java's function types
5. **Simplicity**: Less boilerplate code compared to interface-based approaches