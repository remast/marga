package remast.marga;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> pathParams;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    
    public Request(String method, String path) {
        this.method = method;
        this.path = path;
        this.pathParams = new HashMap<>();
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
    }
    
    public Request(String method, String path, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.pathParams = new HashMap<>();
        this.headers = new HashMap<>(headers);
        this.queryParams = new HashMap<>();
    }
    
    public Request(String method, String path, Map<String, String> headers, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.pathParams = new HashMap<>();
        this.headers = new HashMap<>(headers);
        this.queryParams = new HashMap<>(queryParams);
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
}
