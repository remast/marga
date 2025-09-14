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

public class App {
    public static void main(String[] args) throws IOException {
        var router = new HttpRouter();
        
        // Simple routes
        router.addRoute("GET", "/", request -> 
            Response.ok("<h1>Welcome to Marga!</h1>"));
            
        router.addRoute("GET", "/api/users", request -> 
            Response.ok("{\"users\": []}"));
            
        // Parameterized routes
        router.addRoute("GET", "/users/${id}", request -> {
            var userId = request.pathParam("id");
            return Response.ok("User ID: " + userId);
        });
        
        // Start the server
        router.run();
    }
}
```

That's it! Your HTTP server is running on `http://localhost:8080` with virtual thread support out of the box.

## 🎯 Features

### Simple Route Registration
```java
// Exact routes
router.addRoute("GET", "/api/status", handler);
router.addRoute("POST", "/api/users", handler);

// Parameterized routes with path variables
router.addRoute("GET", "/users/${id}", handler);
router.addRoute("GET", "/posts/${category}/${slug}", handler);
```

### Clean Request Handling
```java
public class UserHandler implements RequestHandler {
    @Override
    public Response handle(Request request) {
        var userId = request.pathParam("id");
        var method = request.getMethod();
        
        return switch (method) {
            case "GET" -> Response.ok("User: " + userId);
            case "PUT" -> Response.ok("Updated user: " + userId);
            default -> Response.methodNotAllowed("Method not supported");
        };
    }
}
```

## 🏗️ Architecture

Marga is designed with simplicity and performance in mind:

- **Parameterized Routes** - Efficient pattern matching with path variables
- **Virtual Threads** - Non-blocking I/O with Java 21's virtual threads
- **Minimal Memory Footprint** - No unnecessary abstractions or overhead

## 📦 Installation

### Maven
```xml
<dependency>
    <groupId>remast</groupId>
    <artifactId>marga-router</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Requirements
- Java 21 or higher
- No external dependencies

## 🎮 Example Applications

Check out the `sample-app` directory for complete examples including:
- Basic routing
- Parameterized routes
- Custom handlers
- Error handling

## 🤝 Contributing

Marga is built for the community. Contributions are welcome! Whether it's bug fixes, new features, or documentation improvements, we'd love to see your pull requests.

---

**Ready to build something amazing?** Start with Marga and experience the joy of simple, efficient HTTP routing in Java! 🎉
