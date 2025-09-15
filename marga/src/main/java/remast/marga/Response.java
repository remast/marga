package remast.marga;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private String body;
    private byte[] bodyBytes;
    private int statusCode;
    private MediaType mediaType;
    private Map<String, String> headers;
    
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
    
    public Response(String body, int statusCode, MediaType mediaType) {
        this.body = body;
        this.bodyBytes = null;
        this.statusCode = statusCode;
        this.mediaType = mediaType;
        this.headers = new HashMap<>();
    }
    
    public Response(byte[] body, int statusCode, MediaType mediaType) {
        this.body = null;
        this.bodyBytes = body;
        this.statusCode = statusCode;
        this.mediaType = mediaType;
        this.headers = new HashMap<>();
    }

    public Response mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }
    
    public String getBody() {
        if (body != null) {
            return body;
        } else if (bodyBytes != null) {
            return new String(bodyBytes);
        }
        return null;
    }
    
    public byte[] getBodyBytes() {
        if (bodyBytes != null) {
            return bodyBytes;
        } else if (body != null) {
            return body.getBytes();
        }
        return null;
    }
    
    public boolean isBinary() {
        return bodyBytes != null;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public MediaType getMediaType() {
        return mediaType;
    }
    
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }
    
    public Response header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }
    
    public String getHeader(String name) {
        return this.headers.get(name);
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
