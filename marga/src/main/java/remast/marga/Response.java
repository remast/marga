package remast.marga;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private final byte[] body;
    private final int statusCode;
    private final Map<String, String> headers;
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
        this.headers = new HashMap<>();
        if (mediaType != null) {
            this.headers.put(HttpHeader.CONTENT_TYPE.getValue(), mediaType.getValue());
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
        this.headers = new HashMap<>();
        if (mediaType != null) {
            this.headers.put(HttpHeader.CONTENT_TYPE.getValue(), mediaType.getValue());
        }
    }

    public Response mediaType(MediaType mediaType) {
        if (mediaType != null) {
            this.headers.put(HttpHeader.CONTENT_TYPE.getValue(), mediaType.getValue());
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
        String contentType = this.headers.get(HttpHeader.CONTENT_TYPE.getValue());
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
        return new HashMap<>(headers);
    }
    
    public Response header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }
    
    public Response header(HttpHeader header, String value) {
        this.headers.put(header.getValue(), value);
        return this;
    }
    
    public String getHeader(String name) {
        return this.headers.get(name);
    }
    
    public String getHeader(HttpHeader header) {
        return this.headers.get(header.getValue());
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
