# Known Issues

## Response header-name validation

`HttpResponseWriter` rejects CR/LF in header values but does not validate header names. A response header name containing CR/LF can inject additional response headers. Validate header names as HTTP field-name tokens before serializing them.

## HTTP response body rules

The server writes a response body for every response. HTTP forbids bodies on `HEAD`, `1xx`, `204`, and `304` responses. Pass request-method context to the response writer and suppress bodies where required.

## Malformed request body framing

Invalid or negative `Content-Length` values are treated as zero, and a short read is accepted as a complete body. Reject malformed framing with `400 Bad Request`, including invalid, negative, conflicting, duplicate, or incomplete content lengths.

## Server restart lifecycle

`shutdown()` terminates the server's executor permanently. Calling `run()` again on the same `HttpRouter` can bind successfully but rejects accepted connection tasks. Either make shutdown terminal in the public contract or recreate the executor for each run.

## Concurrent route registration

Routes may be added after the first request, while routing concurrently reads mutable `HashMap`, `LinkedHashMap`, and `ArrayList` instances. Freeze route registration after serving begins or publish immutable route snapshots.
