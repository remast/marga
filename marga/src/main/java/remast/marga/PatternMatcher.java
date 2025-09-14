package remast.marga;

import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PatternMatcher {
    private final String pattern;
    private final Pattern compiledPattern;
    private final List<String> parameterNames;
    
    public PatternMatcher(String pattern) {
        this.pattern = pattern;
        this.parameterNames = new ArrayList<>();
        this.compiledPattern = compilePattern(pattern);
    }
    
    private Pattern compilePattern(String pattern) {
        if (pattern == null) {
            return null;
        }
        
        // Convert pattern like "/greet/${name}" to regex
        // and extract parameter names
        var regex = pattern.replaceAll("\\$\\{([^}]+)\\}", "([^/]+)");
        var matcher = Pattern.compile("\\$\\{([^}]+)\\}").matcher(pattern);
        
        while (matcher.find()) {
            parameterNames.add(matcher.group(1));
        }
        
        return Pattern.compile("^" + regex + "$");
    }
    
    public boolean matches(String path) {
        if (compiledPattern == null) {
            return false;
        }
        return compiledPattern.matcher(path).matches();
    }
    
    public Map<String, String> extractParameters(String path) {
        var parameters = new HashMap<String, String>();
        
        if (compiledPattern == null || parameterNames.isEmpty()) {
            return parameters;
        }
        
        var matcher = compiledPattern.matcher(path);
        if (matcher.matches()) {
            for (int i = 0; i < parameterNames.size(); i++) {
                var paramName = parameterNames.get(i);
                var paramValue = matcher.group(i + 1);
                parameters.put(paramName, paramValue);
            }
        }
        
        return parameters;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public List<String> getParameterNames() {
        return new ArrayList<>(parameterNames);
    }
    
    public boolean hasParameters() {
        return !parameterNames.isEmpty();
    }
}
