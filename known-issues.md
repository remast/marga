# Known Issues

## Malformed request body framing

Invalid or negative `Content-Length` values are treated as zero, and a short read is accepted as a complete body. Reject malformed framing with `400 Bad Request`, including invalid, negative, conflicting, duplicate, or incomplete content lengths.

## Server restart lifecycle

`shutdown()` terminates the server's executor permanently. Calling `run()` again on the same `HttpRouter` can bind successfully but rejects accepted connection tasks. Either make shutdown terminal in the public contract or recreate the executor for each run.

## Concurrent route registration

Routes may be added after the first request, while routing concurrently reads mutable `HashMap`, `LinkedHashMap`, and `ArrayList` instances. Freeze route registration after serving begins or publish immutable route snapshots.
