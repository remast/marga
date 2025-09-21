# 🚀 Marga - Ultra-Lightweight HTTP Router

**Zero dependencies. Pure Java. Lightning fast.**

Marga is a featherweight HTTP router built from the ground up for Java applications that need simplicity without compromise. No bloated frameworks, no external dependencies, just clean, efficient routing that gets out of your way.

## ✨ Why Marga?

- **🎯 Zero Dependencies** - Pure Java 21, no external libraries
- **⚡ Virtual Threads** - Built-in support for Java 21's virtual threads
- **🔧 Simple API** - Intuitive routing with minimal boilerplate
- **📦 Tiny Footprint** - Minimal overhead, maximum performance
- **🎨 Modern Java** - Leverages latest Java features and best practices

## 🚀 Quick Start

Add Marga to your project and start building APIs in minutes:

```java
import remast.marga.*;
import static remast.marga.MediaType.*;

public class App {
    public static void main(String[] args) throws IOException {
        var router = new HttpRouter();
        
        // Simple routes
        router.GET("/", request -> 
            Response.ok("<h1>Welcome to Marga!</h1>"));

        router.GET("/api/users", request -> 
            Response.ok("{\"users\": []}")
                    .mediaType(APPLICATION_JSON));
            
        // Parameterized routes
        router.GET("/users/${id}", request -> {
            var userId = request.pathParam("id");
            return Response.ok("User ID: " + userId);
        });
        
        // Run the server
        router.run();
    }
}
```

That's it! Your HTTP server is running on `http://localhost:8080` with virtual thread support out of the box.

## 🔧 Middleware Support

Marga includes powerful middleware capabilities with a functional approach:

```java
var router = new HttpRouter();

// Add built-in middleware
router.use(LoggingMiddleware.create());

// Authentication middleware
router.use(handler -> request -> {
    var authHeader = request.getHeader("Authorization");
    if (authHeader == null) {
        return Response.unauthorized("Missing authorization");
    }
    return handler.handle(request);
});

router.GET("/api/protected", request -> Response.ok("Secret data"));
```

**Built-in middleware includes:**
- 📝 **LoggingMiddleware** - Request/response logging
- ⏱️ **TimingMiddleware** - Performance timing
- 🗜️ **GzipCompressionMiddleware** - Response compression

See [MIDDLEWARE.md](MIDDLEWARE.md) for complete middleware documentation.

## 🏗️ Architecture

Marga is designed with simplicity and performance in mind:

- **Parameterized Routes** - Efficient pattern matching with path variables
- **Virtual Threads** - Fast and scalable with Java 21's virtual threads
- **Minimal Memory Footprint** - No unnecessary abstractions or overhead

## 📦 Installation

### Maven
```xml
<dependency>
    <groupId>io.github.remast</groupId>
    <artifactId>marga-router</artifactId>
    <version>0.0.2</version>
</dependency>
```

### Requirements
- Java 21 or higher
- No external dependencies

## 🎮 Example Application

Check out the `example` directory for complete examples including:
- Basic routing
- Parameterized routes
- Custom handlers
- Error handling

---

**Ready to build something amazing?** Start with Marga and experience the joy of simple, efficient HTTP routing in Java! 🎉
