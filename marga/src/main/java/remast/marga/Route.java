package remast.marga;

class Route {
    private final RequestHandler handler;
    private final String description;
    private final PatternMatcher patternMatcher;
    
    public Route(RequestHandler handler, String description, String pattern) {
        this.handler = handler;
        this.description = description;
        this.patternMatcher = pattern != null ? new PatternMatcher(pattern) : null;
    }
    
    public Route(RequestHandler handler, String description) {
        this(handler, description, null);
    }
    
    public Route(RequestHandler handler) {
        this(handler, null, null);
    }
    
    public boolean matches(String path) {
        if (patternMatcher == null) {
            return false;
        }
        return patternMatcher.matches(path);
    }
    
    public void extractParameters(String path, Request request) {
        if (patternMatcher == null) {
            return;
        }
        
        var parameters = patternMatcher.extractParameters(path);
        for (var entry : parameters.entrySet()) {
            request.addPathParam(entry.getKey(), entry.getValue());
        }
    }
    
    public RequestHandler getHandler() {
        return handler;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDescriptionOrDefault() {
        return description != null ? description : handler.getClass().getSimpleName();
    }
    
    public String getPattern() {
        return patternMatcher != null ? patternMatcher.getPattern() : null;
    }
}
