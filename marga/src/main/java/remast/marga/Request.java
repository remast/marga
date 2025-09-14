package remast.marga;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> pathParams;
    
    public Request(String method, String path) {
        this.method = method;
        this.path = path;
        this.pathParams = new HashMap<>();
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
}
