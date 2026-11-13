package remast.marga;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

final class HttpRequestParser {
    private static final int MAX_REQUEST_LINE_BYTES = 8 * 1024;
    private static final int MAX_HEADER_BYTES = 64 * 1024;
    private static final Logger logger = Logger.getLogger(HttpRequestParser.class.getName());

    private final Config config;

    HttpRequestParser(Config config) {
        this.config = config;
    }

    Request parse(BufferedInputStream in, Socket socket) throws IOException {
        socket.setSoTimeout(config.getReadTimeoutMs());
        var deadline = new RequestHeadDeadline(socket, config.getReadTimeoutMs());

        ParsedLine parsedRequestLine;
        try {
            parsedRequestLine = readLine(in, MAX_REQUEST_LINE_BYTES, deadline);
        } catch (RequestHeadTooLargeException e) {
            throw new RequestParseException(new Response("414 - URI Too Long", HttpStatus.URI_TOO_LONG), null);
        } catch (SocketTimeoutException e) {
            throw new RequestParseException(new Response("408 - Request Timeout", HttpStatus.REQUEST_TIMEOUT), null);
        }
        var requestLine = parsedRequestLine == null ? null : parsedRequestLine.value();
        if (requestLine == null || requestLine.isBlank()) {
            return null;
        }

        var requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            throw new RequestParseException(Response.badRequest("400 - Bad Request"), null);
        }

        var method = requestParts[0];
        var fullPath = requestParts[1];
        var path = extractPath(fullPath);
        var queryParams = parseQueryParameters(fullPath);
        Map<String, String> headers;
        try {
            headers = parseHeaders(in, deadline);
        } catch (RequestHeadTooLargeException e) {
            throw new RequestParseException(
                new Response("431 - Request Header Fields Too Large", HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE), method);
        } catch (InvalidHeaderException e) {
            throw new RequestParseException(Response.badRequest("400 - Bad Request"), method);
        } catch (SocketTimeoutException e) {
            throw new RequestParseException(new Response("408 - Request Timeout", HttpStatus.REQUEST_TIMEOUT), method);
        }
        socket.setSoTimeout(config.getReadTimeoutMs());

        var contentLength = parseContentLength(headers);
        if (contentLength > config.getMaxRequestBodyBytes()) {
            throw new RequestParseException(new Response("413 - Content Too Large", HttpStatus.CONTENT_TOO_LARGE), method);
        }

        var body = contentLength > 0 ? in.readNBytes(contentLength) : new byte[0];
        var charset = parseCharset(headers.get(HttpHeader.CONTENT_TYPE.getValue()));
        return new Request(normalizeMethod(method), path, headers, queryParams, body, charset);
    }

    private Map<String, String> parseHeaders(BufferedInputStream in, RequestHeadDeadline deadline) throws IOException {
        var headers = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        var bytesRead = 0;
        while (true) {
            var parsedLine = readLine(in, MAX_HEADER_BYTES - bytesRead, deadline);
            if (parsedLine != null) {
                bytesRead += parsedLine.bytesRead();
            }
            var headerLine = parsedLine == null ? null : parsedLine.value();
            if (headerLine == null || headerLine.isEmpty()) {
                break;
            }

            var colonIndex = headerLine.indexOf(':');
            if (colonIndex <= 0) {
                throw new InvalidHeaderException();
            }
            var headerName = headerLine.substring(0, colonIndex);
            if (!isValidHeaderName(headerName)) {
                throw new InvalidHeaderException();
            }
            var headerValue = headerLine.substring(colonIndex + 1).trim();
            headers.merge(headerName, headerValue, (existing, next) -> existing + ", " + next);
        }
        return headers;
    }

    private ParsedLine readLine(BufferedInputStream in, int maxBytes, RequestHeadDeadline deadline) throws IOException {
        var line = new ByteArrayOutputStream();
        int current;
        var bytesRead = 0;
        var sawCarriageReturn = false;

        while (true) {
            deadline.prepareRead();
            current = in.read();
            if (current == -1) {
                break;
            }
            if (++bytesRead > maxBytes) {
                throw new RequestHeadTooLargeException();
            }
            if (current == '\r') {
                sawCarriageReturn = true;
                continue;
            }
            if (current == '\n') {
                break;
            }
            if (sawCarriageReturn) {
                line.write('\r');
                sawCarriageReturn = false;
            }
            line.write(current);
        }

        if (current == -1 && line.size() == 0) {
            return null;
        }
        return new ParsedLine(line.toString(StandardCharsets.UTF_8), bytesRead);
    }

    private static boolean isValidHeaderName(String name) {
        if (name.isEmpty()) {
            return false;
        }
        for (var i = 0; i < name.length(); i++) {
            var character = name.charAt(i);
            var isTokenCharacter = character >= '0' && character <= '9'
                || character >= 'A' && character <= 'Z'
                || character >= 'a' && character <= 'z'
                || "!#$%&'*+-.^_`|~".indexOf(character) >= 0;
            if (!isTokenCharacter) {
                return false;
            }
        }
        return true;
    }

    private String extractPath(String fullPath) {
        var questionMarkIndex = fullPath.indexOf('?');
        return questionMarkIndex != -1 ? fullPath.substring(0, questionMarkIndex) : fullPath;
    }

    private Map<String, String> parseQueryParameters(String fullPath) {
        var queryParams = new HashMap<String, String>();
        var questionMarkIndex = fullPath.indexOf('?');
        if (questionMarkIndex == -1) {
            return queryParams;
        }

        var queryString = fullPath.substring(questionMarkIndex + 1);
        if (queryString.isEmpty()) {
            return queryParams;
        }

        var pairs = queryString.split("&");
        for (var pair : pairs) {
            var equalIndex = pair.indexOf('=');
            if (equalIndex != -1) {
                var key = pair.substring(0, equalIndex);
                var value = pair.substring(equalIndex + 1);
                queryParams.put(urlDecode(key), urlDecode(value));
            } else if (!pair.isEmpty()) {
                queryParams.put(urlDecode(pair), "");
            }
        }
        return queryParams;
    }

    private String urlDecode(String encoded) {
        try {
            return java.net.URLDecoder.decode(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encoded;
        }
    }

    private static int parseContentLength(Map<String, String> headers) {
        var value = headers.get(HttpHeader.CONTENT_LENGTH.getValue());
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            var parsed = Integer.parseInt(value.trim());
            return Math.max(parsed, 0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Charset parseCharset(String contentType) {
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        var lower = contentType.toLowerCase();
        var idx = lower.indexOf("charset=");
        if (idx < 0) {
            return StandardCharsets.UTF_8;
        }
        var charsetName = contentType.substring(idx + "charset=".length()).trim();
        var semi = charsetName.indexOf(';');
        if (semi >= 0) {
            charsetName = charsetName.substring(0, semi).trim();
        }
        if (charsetName.startsWith("\"") && charsetName.endsWith("\"") && charsetName.length() >= 2) {
            charsetName = charsetName.substring(1, charsetName.length() - 1);
        }
        try {
            return Charset.forName(charsetName);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            logger.warning("Unsupported charset '" + charsetName + "', falling back to UTF-8");
            return StandardCharsets.UTF_8;
        }
    }

    private static String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return "GET";
        }
        return method.trim().toUpperCase();
    }

    static final class RequestParseException extends IOException {
        private final Response response;
        private final String requestMethod;

        RequestParseException(Response response, String requestMethod) {
            this.response = response;
            this.requestMethod = requestMethod;
        }

        Response response() {
            return response;
        }

        String requestMethod() {
            return requestMethod;
        }
    }

    private record ParsedLine(String value, int bytesRead) {
    }

    private static final class RequestHeadTooLargeException extends IOException {
    }

    private static final class InvalidHeaderException extends IOException {
    }

    private final class RequestHeadDeadline {
        private final Socket socket;
        private final long deadlineNanos;
        private int appliedTimeoutMs;

        private RequestHeadDeadline(Socket socket, int timeoutMs) {
            this.socket = socket;
            this.deadlineNanos = System.nanoTime() + timeoutMs * 1_000_000L;
            this.appliedTimeoutMs = timeoutMs;
        }

        private void prepareRead() throws IOException {
            var remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                throw new SocketTimeoutException("Request head deadline exceeded");
            }
            var remainingMs = Math.clamp((remainingNanos + 999_999L) / 1_000_000L, 1, Integer.MAX_VALUE);
            var timeoutMs = Math.min(config.getReadTimeoutMs(), remainingMs);
            if (timeoutMs < appliedTimeoutMs) {
                socket.setSoTimeout(timeoutMs);
                appliedTimeoutMs = timeoutMs;
            }
        }
    }
}
