package remast.marga;

public enum HttpStatus {

    // 1xx Informational

    /**
     * {@code 100 Continue}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-100-continue">HTTP Semantics, section 15.2.1</a>
     */
    CONTINUE(100, "Continue"),
    /**
     * {@code 101 Switching Protocols}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-101-switching-protocols">HTTP Semantics, section 15.2.2</a>
     */
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    /**
     * {@code 103 Early Hints}.
     * @see <a href="https://tools.ietf.org/html/rfc8297">An HTTP Status Code for Indicating Hints</a>
     */
    EARLY_HINTS(103, "Early Hints"),

    // 2xx Success

    /**
     * {@code 200 OK}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-200-ok">HTTP Semantics, section 15.3.1</a>
     */
    OK(200, "OK"),
    /**
     * {@code 201 Created}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-201-created">HTTP Semantics, section 15.3.2</a>
     */
    CREATED(201, "Created"),
    /**
     * {@code 202 Accepted}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-202-accepted">HTTP Semantics, section 15.3.3</a>
     */
    ACCEPTED(202, "Accepted"),
    /**
     * {@code 203 Non-Authoritative Information}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-203-non-authoritative-infor">HTTP Semantics, section 15.3.4</a>
     */
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    /**
     * {@code 204 No Content}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-204-no-content">HTTP Semantics, section 15.3.5</a>
     */
    NO_CONTENT(204, "No Content"),
    /**
     * {@code 205 Reset Content}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-205-reset-content">HTTP Semantics, section 15.3.6</a>
     */
    RESET_CONTENT(205, "Reset Content"),
    /**
     * {@code 206 Partial Content}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-206-partial-content">HTTP/1.1: Range Requests, section 4.1</a>
     */
    PARTIAL_CONTENT(206, "Partial Content"),
    /**
     * {@code 207 Multi-Status}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc4918#section-11.1">WebDAV</a>
     */
    MULTI_STATUS(207, "Multi-Status"),
    /**
     * {@code 208 Already Reported}.
     * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding Extensions</a>
     */
    ALREADY_REPORTED(208, "Already Reported"),
    /**
     * {@code 226 IM Used}.
     * @see <a href="https://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in HTTP</a>
     */
    IM_USED(226, "IM Used"),

    // 3xx Redirection

    /**
     * {@code 300 Multiple Choices}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-300-multiple-choices">HTTP Semantics, section 15.4.1</a>
     */
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    /**
     * {@code 301 Moved Permanently}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-301-moved-permanently">HTTP Semantics, section 15.4.2</a>
     */
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    /**
     * {@code 302 Found}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-302-found">HTTP Semantics, section 15.4.3</a>
     */
    FOUND(302, "Found"),
    /**
     * {@code 303 See Other}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-303-see-other">HTTP Semantics, section 15.4.4</a>
     */
    SEE_OTHER(303, "See Other"),
    /**
     * {@code 304 Not Modified}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-304-not-modified">HTTP Semantics, section 15.4.5</a>
     */
    NOT_MODIFIED(304, "Not Modified"),
    /**
     * {@code 307 Temporary Redirect}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-307-temporary-redirect">HTTP Semantics, section 15.4.8</a>
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    /**
     * {@code 308 Permanent Redirect}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-308-permanent-redirect">HTTP Semantics, section 15.4.9</a>
     */
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    // --- 4xx Client Error ---

    /**
     * {@code 400 Bad Request}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-400-bad-request">HTTP Semantics, section 15.5.1</a>
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * {@code 401 Unauthorized}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-401-unauthorized">HTTP Semantics, section 15.5.2</a>
     */
    UNAUTHORIZED(401, "Unauthorized"),
    /**
     * {@code 402 Payment Required}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-402-payment-required">HTTP Semantics, section 15.5.3</a>
     */
    PAYMENT_REQUIRED(402, "Payment Required"),
    /**
     * {@code 403 Forbidden}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-403-forbidden">HTTP Semantics, section 15.5.4</a>
     */
    FORBIDDEN(403, "Forbidden"),
    /**
     * {@code 404 Not Found}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-404-not-found">HTTP Semantics, section 15.5.5</a>
     */
    NOT_FOUND(404, "Not Found"),
    /**
     * {@code 405 Method Not Allowed}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-405-method-not-allowed">HTTP Semantics, section 15.5.6</a>
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /**
     * {@code 406 Not Acceptable}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-406-not-acceptable">HTTP Semantics, section 15.5.7</a>
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    /**
     * {@code 407 Proxy Authentication Required}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-407-proxy-authentication-re">HTTP Semantics, section 15.5.8</a>
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    /**
     * {@code 408 Request Timeout}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-408-request-timeout">HTTP Semantics, section 15.5.9</a>
     */
    REQUEST_TIMEOUT(408, "Request Timeout"),
    /**
     * {@code 409 Conflict}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-409-conflict">HTTP Semantics, section 15.5.10</a>
     */
    CONFLICT(409, "Conflict"),
    /**
     * {@code 410 Gone}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-410-gone">
     *     HTTP Semantics, section 15.5.11</a>
     */
    GONE(410, "Gone"),
    /**
     * {@code 411 Length Required}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-411-length-required">
     *     HTTP Semantics, section 15.5.12</a>
     */
    LENGTH_REQUIRED(411, "Length Required"),
    /**
     * {@code 412 Precondition failed}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-412-precondition-failed">
     *     HTTP Semantics, section 15.5.13</a>
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /**
     * {@code 413 Content Too Large}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-413-content-too-large">
     *     HTTP Semantics, section 15.5.14</a>
     */
    CONTENT_TOO_LARGE(413, "Content Too Large"),
    /**
     * {@code 414 URI Too Long}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-414-uri-too-long">
     *     HTTP Semantics, section 15.5.15</a>
     */
    URI_TOO_LONG(414, "URI Too Long"),
    /**
     * {@code 415 Unsupported Media Type}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-415-unsupported-media-type">
     *     HTTP Semantics, section 15.5.16</a>
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /**
     * {@code 416 Requested Range Not Satisfiable}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-416-range-not-satisfiable">
     *     HTTP Semantics, section 15.5.17</a>
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested range not satisfiable"),
    /**
     * {@code 417 Expectation Failed}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-417-expectation-failed">
     *     HTTP Semantics, section 15.5.18</a>
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),
    /**
     * {@code 421 Misdirected Request}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-421-misdirected-request">
     *     HTTP Semantics, section 15.5.20</a>
     */
    MISDIRECTED_REQUEST(421, "Misdirected Request"),
    /**
     * {@code 422 Unprocessable Content}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-422-unprocessable-content">
     *     HTTP Semantics, section 15.5.21</a>
     */
    UNPROCESSABLE_CONTENT(422, "Unprocessable Content"),
    /**
     * {@code 423 Locked}.
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
     */
    LOCKED(423, "Locked"),
    /**
     * {@code 424 Failed Dependency}.
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
     */
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    /**
     * {@code 425 Too Early}.
     * @see <a href="https://tools.ietf.org/html/rfc8470">RFC 8470</a>
     */
    TOO_EARLY(425, "Too Early"),
    /**
     * {@code 426 Upgrade Required}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-426-upgrade-required">
     *      HTTP Semantics, section 15.5.22</a>
     */
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    /**
     * {@code 428 Precondition Required}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status Codes</a>
     */
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    /**
     * {@code 429 Too Many Requests}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status Codes</a>
     */
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    /**
     * {@code 431 Request Header Fields Too Large}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status Codes</a>
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    /**
     * {@code 451 Unavailable For Legal Reasons}.
     * @see <a href="https://tools.ietf.org/html/draft-ietf-httpbis-legally-restricted-status-04">
     * An HTTP Status Code to Report Legal Obstacles</a>
     */
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    // --- 5xx Server Error ---

    /**
     * {@code 500 Internal Server Error}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#section-15.6.1">HTTP Semantics, section 15.6.1</a>
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    /**
     * {@code 501 Not Implemented}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-501-not-implemented">HTTP Semantics, section 15.6.2</a>
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),
    /**
     * {@code 502 Bad Gateway}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-502-bad-gateway">HTTP Semantics, section 15.6.3</a>
     */
    BAD_GATEWAY(502, "Bad Gateway"),
    /**
     * {@code 503 Service Unavailable}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-503-service-unavailable">HTTP Semantics, section 15.6.4</a>
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    /**
     * {@code 504 Gateway Timeout}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-504-gateway-timeout">HTTP Semantics, section 15.6.5</a>
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    /**
     * {@code 505 HTTP Version Not Supported}.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-505-http-version-not-suppor">HTTP Semantics, section 15.6.6</a>
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version not supported"),
    /**
     * {@code 506 Variant Also Negotiates}
     * @see <a href="https://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content Negotiation</a>
     */
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
    /**
     * {@code 507 Insufficient Storage}
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
     */
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    /**
     * {@code 508 Loop Detected}
     * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding Extensions</a>
     */
    LOOP_DETECTED(508, "Loop Detected"),
    /**
     * {@code 511 Network Authentication Required}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status Codes</a>
     */
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");
    
    private final int code;
    private final String reasonPhrase;
    
    HttpStatus(int code, String reasonPhrase) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public static HttpStatus fromCode(int statusCode) {
        for (var status : values()) {
            if (status.code == statusCode) {
                return status;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return code + " " + reasonPhrase;
    }
}
