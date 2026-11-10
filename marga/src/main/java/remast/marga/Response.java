package remast.marga;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Response {
    private final byte[] body;
    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final Charset charset;
    private final boolean binary;
    
    public Response(String body, HttpStatus status) {
        this(body, status.getCode(), MediaType.TEXT_PLAIN);
    }
    
    public Response(String body, int statusCode) {
        this(body, statusCode, MediaType.TEXT_PLAIN);
    }
    
    public Response(String body) {
        this(body, HttpStatus.OK, MediaType.TEXT_PLAIN);
    }
    
    public Response(String body, HttpStatus status, MediaType mediaType) {
        this(body, status.getCode(), mediaType);
    }

    public Response(String body, HttpStatus status, MediaType mediaType, Charset charset) {
        this(body, status.getCode(), mediaType, charset);
    }
     
    public Response(String body, int statusCode, MediaType mediaType) {
        this(body, statusCode, mediaType, StandardCharsets.UTF_8);
    }

    public Response(String body, int statusCode, MediaType mediaType, Charset charset) {
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
        this.body = body == null ? new byte[0] : body.getBytes(this.charset);
        this.binary = false;
        this.statusCode = statusCode;
        this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (mediaType != null) {
            setHeader(HttpHeader.CONTENT_TYPE.getValue(), mediaType.getValue());
        }
    }
     
    public Response(byte[] body, int statusCode, MediaType mediaType) {
        this(body, statusCode, mediaType, StandardCharsets.UTF_8);
    }

    public Response(byte[] body, int statusCode, MediaType mediaType, Charset charset) {
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
        this.body = body == null ? new byte[0] : Arrays.copyOf(body, body.length);
        this.binary = true;
        this.statusCode = statusCode;
        this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (mediaType != null) {
            setHeader(HttpHeader.CONTENT_TYPE.getValue(), mediaType.getValue());
        }
    }

    public Response mediaType(MediaType mediaType) {
        if (mediaType != null) {
            setHeader(HttpHeader.CONTENT_TYPE.getValue(), mediaType.getValue());
        }
        return this;
    }
    
    public String getBody() {
        return new String(body, charset);
    }
     
    public byte[] getBodyBytes() {
        return Arrays.copyOf(body, body.length);
    }
     
    public boolean isBinary() {
        return binary;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public MediaType getMediaType() {
        var contentTypes = this.headers.get(HttpHeader.CONTENT_TYPE.getValue());
        if (contentTypes == null || contentTypes.isEmpty()) {
            return null;
        }
        var contentType = contentTypes.getLast();
        if (contentType == null) {
            return null;
        }
        var typeOnly = contentType.split(";", 2)[0].trim();
        return new MediaType(typeOnly);
    }

    public Charset getCharset() {
        return charset;
    }
    
    public Map<String, String> getHeaders() {
        var copy = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        headers.forEach((name, values) -> copy.put(name, values.getLast()));
        return copy;
    }

    public List<String> getHeaderValues(String name) {
        var values = headers.get(name);
        return values == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(values));
    }

    public Map<String, List<String>> getAllHeaders() {
        var copy = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        headers.forEach((name, values) -> copy.put(name, Collections.unmodifiableList(new ArrayList<>(values))));
        return Collections.unmodifiableMap(copy);
    }
    
    public Response header(String name, String value) {
        setHeader(name, value);
        return this;
    }
    
    public Response header(HttpHeader header, String value) {
        return header(header.getValue(), value);
    }

    public Response addHeader(String name, String value) {
        headers.computeIfAbsent(name, ignored -> new ArrayList<>()).add(value);
        return this;
    }

    public Response addHeader(HttpHeader header, String value) {
        return addHeader(header.getValue(), value);
    }

    public String getHeader(String name) {
        var values = this.headers.get(name);
        return values == null ? null : values.getLast();
    }

    public String getHeader(HttpHeader header) {
        return getHeader(header.getValue());
    }

    private void setHeader(String name, String value) {
        var values = new ArrayList<String>();
        values.add(value);
        headers.put(name, values);
    }
    
    public static Response ok(String body) {
        return new Response(body, HttpStatus.OK);
    }
    
    public static Response notFound(String body) {
        return new Response(body, HttpStatus.NOT_FOUND);
    }
    
    public static Response serverError(String body) {
        return new Response(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    public static Response badRequest(String body) {
        return new Response(body, HttpStatus.BAD_REQUEST);
    }
    
    public static Response unauthorized(String body) {
        return new Response(body, HttpStatus.UNAUTHORIZED);
    }
    
    public static Response forbidden(String body) {
        return new Response(body, HttpStatus.FORBIDDEN);
    }
    
    public static Response methodNotAllowed(String body) {
        return new Response(body, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    public static Response created(String body) {
        return new Response(body, HttpStatus.CREATED);
    }
    
    public static Response noContent() {
        return new Response("", HttpStatus.NO_CONTENT);
    }
    
    public static Response ok(String body, MediaType mediaType) {
        return new Response(body, HttpStatus.OK, mediaType);
    }
    
    public static Response json(String body) {
        return new Response(body, HttpStatus.OK, MediaType.APPLICATION_JSON);
    }
    
    public static Response html(String body) {
        return new Response(body, HttpStatus.OK, MediaType.TEXT_HTML);
    }
}
