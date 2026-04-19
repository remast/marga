package remast.marga;

class Route {
    private final String method;
    private final RequestHandler handler;
    private final String description;
    private final PatternMatcher patternMatcher;
    private final int staticSegmentCount;
    private final int parameterSegmentCount;
    private final int firstParameterIndex;
    
    public Route(String method, RequestHandler handler, String description, String pattern) {
        this.method = method;
        this.handler = handler;
        this.description = description;
        this.patternMatcher = pattern != null ? new PatternMatcher(pattern) : null;
        this.staticSegmentCount = countStaticSegments(pattern);
        this.parameterSegmentCount = countParameterSegments(pattern);
        this.firstParameterIndex = findFirstParameterIndex(pattern);
    }

    public Route(RequestHandler handler, String description, String pattern) {
        this("GET", handler, description, pattern);
    }

    public Route(String method, RequestHandler handler, String description) {
        this(method, handler, description, null);
    }
    
    public Route(RequestHandler handler, String description) {
        this("GET", handler, description, null);
    }
    
    public Route(RequestHandler handler) {
        this("GET", handler, null, null);
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

    public String getMethod() {
        return method;
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

    public int getStaticSegmentCount() {
        return staticSegmentCount;
    }

    public int getParameterSegmentCount() {
        return parameterSegmentCount;
    }

    public int getFirstParameterIndex() {
        return firstParameterIndex;
    }

    private static int countStaticSegments(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return 0;
        }

        var segments = pattern.split("/");
        var count = 0;
        for (var segment : segments) {
            if (segment.isBlank()) {
                continue;
            }
            if (!isParameterSegment(segment)) {
                count++;
            }
        }
        return count;
    }

    private static int countParameterSegments(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return 0;
        }

        var segments = pattern.split("/");
        var count = 0;
        for (var segment : segments) {
            if (isParameterSegment(segment)) {
                count++;
            }
        }
        return count;
    }

    private static int findFirstParameterIndex(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return Integer.MAX_VALUE;
        }

        var segments = pattern.split("/");
        var position = 0;
        for (var segment : segments) {
            if (segment.isBlank()) {
                continue;
            }
            if (isParameterSegment(segment)) {
                return position;
            }
            position++;
        }
        return Integer.MAX_VALUE;
    }

    private static boolean isParameterSegment(String segment) {
        return segment.startsWith("${") && segment.endsWith("}");
    }
}
