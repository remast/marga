# Known Issues

## Malformed request body framing

Invalid or negative `Content-Length` values are treated as zero, and a short read is accepted as a complete body. Reject malformed framing with `400 Bad Request`, including invalid, negative, conflicting, duplicate, or incomplete content lengths. `Transfer-Encoding: chunked` is ignored and the body is treated as empty; reject it with `501` or `411` until supported.

## Error responses may be lost

On `400` and `413` the socket is closed without draining the unread request body. The OS may send a TCP RST, discarding the response before the client reads it. Drain a bounded amount of input or half-close the output and linger briefly before closing.

## Missing `Expect: 100-continue` support

Clients sending `Expect: 100-continue` (e.g. curl for larger bodies) wait for a timeout before sending the body.

## Lenient request-line parsing

The request line is split on spaces without checking for exactly three parts or a valid `HTTP/1.x` version (no `505`). Absolute-form targets (`http://host/path`) are not handled. The path is neither percent-decoded nor normalized, so path parameters remain encoded while query parameters are decoded.

## Incomplete response validation

Header values are only checked for CR/LF; NUL and other control characters pass. A handler returning a `1xx` status sends it as the final response.

## Server restart lifecycle

`shutdown()` terminates the server's executor permanently. Calling `run()` again on the same `HttpRouter` can bind successfully but rejects accepted connection tasks. Either make shutdown terminal in the public contract or recreate the executor for each run. The router also stays frozen after the first `run()`, so routes and middleware cannot be changed before a restart.

Calling `shutdown()` before `run()` has bound is ignored. `serverSocket` is not `volatile` but is read from other threads in `getBoundPort()`. Shutdown does not wait for in-flight requests (`awaitTermination` is never called).

## Errors logged without stack traces

Unhandled exceptions are logged with `logger.severe(t.getMessage())`, dropping the stack trace. Use `logger.log(Level.SEVERE, "...", t)`.

## Minor

- Response status line and headers are written line by line to the socket stream without buffering.
- No keep-alive; every response sends `Connection: close`.
- Request bodies are copied multiple times (`Request` constructor and every `getBodyBytes()` call).
- `Route` splits the pattern three times although `PatternMatcher` already parses it.
- HTTP methods are upper-cased although they are case-sensitive.
- `Request.addPathParam` is public, so handlers can change path parameters.
- `HttpResponseWriter.appendCharsetIfNeeded` matches `charset=` case-sensitively.
