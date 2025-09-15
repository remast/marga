# Middleware in Marga

Marga supports middleware functionality similar to Go's middleware pattern. Middleware allows you to perform pre-processing and post-processing on all requests before they reach your actual handlers.

## How Middleware Works

Middleware in Marga follows the same pattern as described in the [Go middleware tutorial](https://drstearns.github.io/tutorials/gomiddleware/):

1. A middleware takes a `RequestHandler` as input
2. Returns a new `RequestHandler` that wraps the original handler
3. Can perform operations before and/or after calling the original handler
4. Multiple middleware can be chained together

## Basic Usage

### Adding Middleware

Use the `use()` method on your `HttpRouter` to add middleware:

```java
var router = new HttpRouter();

// Add middleware
router.use(new LoggingMiddleware());
router.use(new TimingMiddleware());

// Add your routes
router.GET("/hello", new HelloHandler());
```

### Middleware Order

Middleware is applied in the order you add it. The first middleware added will be the outermost wrapper:

```java
router.use(middleware1);  // Outermost
router.use(middleware2);  // Middle
router.use(middleware3);  // Innermost

// Execution order: middleware1 -> middleware2 -> middleware3 -> handler -> middleware3 -> middleware2 -> middleware1
```

## Built-in Middleware

### LoggingMiddleware

Logs each request with method, path, and response status:

```java
router.use(new LoggingMiddleware());
```

Example log output:
```
GET /hello
GET /hello - 200
```

### TimingMiddleware

Adds timing information to responses (currently logs timing info):

```java
router.use(new TimingMiddleware());
```

### GzipCompressionMiddleware

Automatically compresses response bodies using gzip compression to reduce bandwidth usage:

```java
router.use(new GzipCompressionMiddleware()); // Default 1KB threshold
// or
router.use(new GzipCompressionMiddleware(500)); // Custom 500 byte threshold
```

**Features:**
- Only compresses responses larger than the minimum threshold (default: 1KB)
- Automatically detects compressible content types (text, JSON, HTML, CSS, etc.)
- Skips already compressed formats (images, videos, audio, etc.)
- Only applies compression if it reduces size by at least 10%
- Sets `Content-Encoding: gzip` header automatically
- Achieves 95-98% compression ratios for text-based content

**Example compression results:**
```
Compressed response from 163890 to 3325 bytes (98.0% reduction)
Compressed response from 65860 to 1673 bytes (97.5% reduction)
```

## Creating Custom Middleware

### Simple Middleware

```java
public class CustomMiddleware implements Middleware {
    @Override
    public RequestHandler apply(RequestHandler handler) {
        return request -> {
            // Pre-processing
            System.out.println("Before handling: " + request.getPath());
            
            // Call the next handler
            var response = handler.handle(request);
            
            // Post-processing
            System.out.println("After handling: " + response.getStatusCode());
            
            return response;
        };
    }
}
```

### Middleware with Error Handling

```java
public class ErrorHandlingMiddleware implements Middleware {
    @Override
    public RequestHandler apply(RequestHandler handler) {
        return request -> {
            try {
                return handler.handle(request);
            } catch (Exception e) {
                System.err.println("Error handling request: " + e.getMessage());
                return Response.serverError("Internal Server Error");
            }
        };
    }
}
```

### Middleware with Request Modification

```java
public class RequestModificationMiddleware implements Middleware {
    @Override
    public RequestHandler apply(RequestHandler handler) {
        return request -> {
            // You can't modify the request object directly, but you can
            // create a new request or pass additional context
            System.out.println("Processing request to: " + request.getPath());
            
            return handler.handle(request);
        };
    }
}
```

## Example Application

See the `example` module for a complete example showing how to use middleware:

```java
public class App {
    public static void main(String[] args) throws IOException {
        var router = new HttpRouter();
        
        // Add middleware (order matters)
        router.use(new LoggingMiddleware());
        router.use(new TimingMiddleware());
        router.use(new GzipCompressionMiddleware()); // Compress responses
        
        // Add routes
        router.GET("/", new RootHandler());
        router.GET("/hello", new HelloHandler());
        
        router.run();
    }
}
```

## Testing Middleware

The framework includes comprehensive tests for middleware functionality. You can test your custom middleware by:

1. Creating a test that adds your middleware to an `HttpRouter`
2. Making requests and verifying the middleware behavior
3. Testing middleware chaining and error handling

See `MiddlewareTest.java` and `LoggingMiddlewareTest.java` for examples.

## Best Practices

1. **Order matters**: Add middleware in the order you want them to execute
2. **Error handling**: Always wrap your middleware logic in try-catch blocks
3. **Performance**: Keep middleware lightweight to avoid impacting request performance
4. **Logging**: Use appropriate log levels and avoid logging sensitive information
5. **Reusability**: Create reusable middleware that can be applied to different routes
6. **Compression**: Use GzipCompressionMiddleware for text-based APIs to reduce bandwidth
7. **Compression threshold**: Set appropriate minimum compression sizes (1KB default works well)
8. **Content type awareness**: Be mindful of which content types benefit from compression

## Advanced Patterns

### Conditional Middleware

```java
public class ConditionalMiddleware implements Middleware {
    private final String pathPrefix;
    
    public ConditionalMiddleware(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }
    
    @Override
    public RequestHandler apply(RequestHandler handler) {
        return request -> {
            if (request.getPath().startsWith(pathPrefix)) {
                // Apply middleware logic only to specific paths
                System.out.println("Processing API request: " + request.getPath());
            }
            return handler.handle(request);
        };
    }
}
```

### Middleware with Configuration

```java
public class ConfigurableMiddleware implements Middleware {
    private final boolean enableLogging;
    
    public ConfigurableMiddleware(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
    
    @Override
    public RequestHandler apply(RequestHandler handler) {
        return request -> {
            if (enableLogging) {
                System.out.println("Request: " + request.getMethod() + " " + request.getPath());
            }
            return handler.handle(request);
        };
    }
}
```

### Compression Middleware Configuration

The GzipCompressionMiddleware can be configured for different use cases:

```java
// For high-traffic APIs with large responses
router.use(new GzipCompressionMiddleware(512)); // Compress responses > 512 bytes

// For APIs with mostly small responses
router.use(new GzipCompressionMiddleware(2048)); // Only compress larger responses

// For development/testing (compress everything)
router.use(new GzipCompressionMiddleware(0)); // Compress all responses
```

**When to use compression:**
- Text-based APIs (JSON, XML, HTML)
- Large response bodies (>1KB typically)
- High-traffic applications where bandwidth matters
- Mobile applications where data usage is a concern

**When to avoid compression:**
- APIs serving mostly small responses (<1KB)
- Real-time applications where latency is critical
- APIs serving already compressed content (images, videos)
