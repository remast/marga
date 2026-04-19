package remast.marga;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

final class HttpResponseWriter {
    private static final String CRLF = "\r\n";

    void write(OutputStream outputStream, Response response) throws IOException {
        var bodyBytes = response.getBodyBytes();
        var headers = new HashMap<>(response.getHeaders());

        if (!containsHeader(headers, HttpHeader.CONTENT_TYPE.getValue()) && response.getMediaType() != null) {
            headers.put(HttpHeader.CONTENT_TYPE.getValue(), response.getMediaType().getValue());
        }

        appendCharsetIfNeeded(headers, response);
        headers.put(HttpHeader.CONTENT_LENGTH.getValue(), String.valueOf(bodyBytes.length));
        headers.putIfAbsent(HttpHeader.CONNECTION.getValue(), "close");
        headers.putIfAbsent(HttpHeader.DATE.getValue(), rfc1123Now());

        for (var entry : headers.entrySet()) {
            validateHeaderValue(entry.getKey(), entry.getValue());
        }

        writeAsciiLine(outputStream, buildStatusLine(response.getStatusCode()));
        for (var entry : headers.entrySet()) {
            writeAsciiLine(outputStream, entry.getKey() + ": " + entry.getValue());
        }
        writeAsciiLine(outputStream, "");
        outputStream.write(bodyBytes);
        outputStream.flush();
    }

    private static String buildStatusLine(int statusCode) {
        var status = HttpStatus.fromCode(statusCode);
        if (status != null) {
            return "HTTP/1.1 " + status;
        }
        return "HTTP/1.1 " + statusCode + " Status";
    }

    private static void writeAsciiLine(OutputStream outputStream, String line) throws IOException {
        outputStream.write(line.getBytes(StandardCharsets.US_ASCII));
        outputStream.write(CRLF.getBytes(StandardCharsets.US_ASCII));
    }

    private static void appendCharsetIfNeeded(Map<String, String> headers, Response response) {
        var headerName = findHeaderName(headers, HttpHeader.CONTENT_TYPE.getValue());
        if (headerName == null) {
            return;
        }

        var contentType = headers.get(headerName);
        if (contentType == null || contentType.contains("charset=")) {
            return;
        }

        var mediaType = response.getMediaType();
        if (mediaType == null) {
            return;
        }

        var value = mediaType.value().toLowerCase();
        var addCharset = value.startsWith("text/") || value.contains("json") || value.contains("xml");
        if (addCharset) {
            headers.put(headerName, contentType + "; charset=" + response.getCharset().name());
        }
    }

    private static boolean containsHeader(Map<String, String> headers, String expectedName) {
        return findHeaderName(headers, expectedName) != null;
    }

    private static String findHeaderName(Map<String, String> headers, String expectedName) {
        for (var name : headers.keySet()) {
            if (name.equalsIgnoreCase(expectedName)) {
                return name;
            }
        }
        return null;
    }

    private static String rfc1123Now() {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
    }

    private static void validateHeaderValue(String name, String value) {
        if (value == null) {
            return;
        }
        for (var i = 0; i < value.length(); i++) {
            var c = value.charAt(i);
            if (c == '\r' || c == '\n') {
                throw new IllegalArgumentException("Invalid header value for '" + name + "': contains CR/LF");
            }
        }
    }
}
