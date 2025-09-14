package remast.marga;

public class Response {
    private final String body;
    private final int statusCode;
    
    public Response(String body, HttpStatus status) {
        this(body, status.getCode());
    }
    
    public Response(String body, int statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }
    
    public Response(String body) {
        this(body, HttpStatus.OK);
    }
    
    public String getBody() {
        return body;
    }
    
    public int getStatusCode() {
        return statusCode;
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
}
