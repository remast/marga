package remast.marga;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private static final byte[] EMPTY_BODY = new byte[0];

    private final String method;
    private final String path;
    private final Map<String, String> pathParams;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final byte[] body;
    private final Charset charset;

    public Request(String method, String path) {
        this(method, path, Map.of(), Map.of(), EMPTY_BODY, StandardCharsets.UTF_8);
    }

    public Request(String method, String path, Map<String, String> headers) {
        this(method, path, headers, Map.of(), EMPTY_BODY, StandardCharsets.UTF_8);
    }

    public Request(String method, String path, Map<String, String> headers, Map<String, String> queryParams) {
        this(method, path, headers, queryParams, EMPTY_BODY, StandardCharsets.UTF_8);
    }

    public Request(String method, String path, Map<String, String> headers, Map<String, String> queryParams, byte[] body, Charset charset) {
        this.method = method;
        this.path = path;
        this.pathParams = new HashMap<>();
        this.headers = new HashMap<>(headers);
        this.queryParams = new HashMap<>(queryParams);
        this.body = body == null ? EMPTY_BODY : Arrays.copyOf(body, body.length);
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String pathParam(String name) {
        return pathParams.get(name);
    }

    public void addPathParam(String name, String value) {
        pathParams.put(name, value);
    }

    public Map<String, String> getPathParams() {
        return new HashMap<>(pathParams);
    }

    public String header(String name) {
        return headers.get(name);
    }

    public String header(HttpHeader header) {
        return headers.get(header.getValue());
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public boolean hasHeader(String name) {
        return headers.containsKey(name);
    }

    public boolean hasHeader(HttpHeader header) {
        return headers.containsKey(header.getValue());
    }

    public String queryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return new HashMap<>(queryParams);
    }

    public boolean hasQueryParam(String name) {
        return queryParams.containsKey(name);
    }

    public byte[] getBodyBytes() {
        return Arrays.copyOf(body, body.length);
    }

    public String getBody() {
        return new String(body, charset);
    }

    public Charset getCharset() {
        return charset;
    }

    public int getContentLength() {
        return body.length;
    }

    @Override
    public String toString() {
        return "Request{method=" + method
            + ", path=" + path
            + ", pathParams=" + pathParams
            + ", queryParams=" + queryParams
            + ", headers=" + headers
            + ", contentLength=" + body.length
            + ", charset=" + charset
            + "}";
    }
}
