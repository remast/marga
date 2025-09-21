package remast.marga;

/**
 * Enum representing common HTTP headers with their standard names.
 * This provides type-safe access to HTTP header names and helps prevent
 * typos when working with headers.
 */
public enum HttpHeader {
    // Content headers
    CONTENT_TYPE("Content-Type"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_ENCODING("Content-Encoding"),
    CONTENT_DISPOSITION("Content-Disposition"),
    CONTENT_LANGUAGE("Content-Language"),
    CONTENT_LOCATION("Content-Location"),
    CONTENT_RANGE("Content-Range"),
    CONTENT_SECURITY_POLICY("Content-Security-Policy"),
    
    // Caching headers
    CACHE_CONTROL("Cache-Control"),
    ETAG("ETag"),
    EXPIRES("Expires"),
    LAST_MODIFIED("Last-Modified"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),
    IF_RANGE("If-Range"),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
    
    // Authentication headers
    AUTHORIZATION("Authorization"),
    WWW_AUTHENTICATE("WWW-Authenticate"),
    PROXY_AUTHORIZATION("Proxy-Authorization"),
    PROXY_AUTHENTICATE("Proxy-Authenticate"),
    
    // CORS headers
    ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
    ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),
    ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers"),
    ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),
    ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),
    ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),
    ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method"),
    ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers"),
    
    // Request headers
    ACCEPT("Accept"),
    ACCEPT_CHARSET("Accept-Charset"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_LANGUAGE("Accept-Language"),
    USER_AGENT("User-Agent"),
    REFERER("Referer"),
    HOST("Host"),
    ORIGIN("Origin"),
    
    // Response headers
    LOCATION("Location"),
    REFRESH("Refresh"),
    RETRY_AFTER("Retry-After"),
    SERVER("Server"),
    VARY("Vary"),
    
    // Connection headers
    CONNECTION("Connection"),
    KEEP_ALIVE("Keep-Alive"),
    UPGRADE("Upgrade"),
    
    // Security headers
    STRICT_TRANSPORT_SECURITY("Strict-Transport-Security"),
    X_FRAME_OPTIONS("X-Frame-Options"),
    X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options"),
    X_XSS_PROTECTION("X-XSS-Protection"),
    REFERRER_POLICY("Referrer-Policy"),
    
    // Custom headers (commonly used)
    X_REQUEST_ID("X-Request-ID"),
    X_CORRELATION_ID("X-Correlation-ID"),
    X_FORWARDED_FOR("X-Forwarded-For"),
    X_FORWARDED_PROTO("X-Forwarded-Proto"),
    X_FORWARDED_HOST("X-Forwarded-Host"),
    X_REAL_IP("X-Real-IP"),
    X_CUSTOM_HEADER("X-Custom-Header");
    
    private final String value;
    
    HttpHeader(String value) {
        this.value = value;
    }
    
    /**
     * Returns the standard HTTP header name as a string.
     * @return the header name
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Returns the header name as a string (same as getValue()).
     * This method is provided for convenience and consistency with other enums.
     * @return the header name
     */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * Finds an HttpHeader enum by its string value (case-insensitive).
     * @param headerName the header name to search for
     * @return the matching HttpHeader enum, or null if not found
     */
    public static HttpHeader fromString(String headerName) {
        if (headerName == null) {
            return null;
        }
        
        for (HttpHeader header : values()) {
            if (header.value.equalsIgnoreCase(headerName)) {
                return header;
            }
        }
        return null;
    }
}
