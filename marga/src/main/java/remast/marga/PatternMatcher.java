package remast.marga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternMatcher {
    private static final String[] EMPTY_SEGMENTS = new String[0];

    private final String pattern;
    // For each segment: the literal text, or null if this segment is a parameter slot.
    private final String[] literalSegments;
    // For each segment: the parameter name if this is a parameter slot, else null.
    private final String[] parameterAtIndex;
    private final List<String> parameterNames;
    private final int segmentCount;

    public PatternMatcher(String pattern) {
        this.pattern = pattern;
        if (pattern == null) {
            this.literalSegments = EMPTY_SEGMENTS;
            this.parameterAtIndex = EMPTY_SEGMENTS;
            this.parameterNames = Collections.emptyList();
            this.segmentCount = 0;
            return;
        }

        var rawSegments = splitSegments(pattern);
        var literals = new String[rawSegments.size()];
        var params = new String[rawSegments.size()];
        var names = new ArrayList<String>();
        for (var i = 0; i < rawSegments.size(); i++) {
            var segment = rawSegments.get(i);
            if (isParameterSegment(segment)) {
                var name = segment.substring(2, segment.length() - 1);
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Empty parameter name in pattern: " + pattern);
                }
                params[i] = name;
                names.add(name);
            } else {
                literals[i] = segment;
            }
        }
        this.literalSegments = literals;
        this.parameterAtIndex = params;
        this.parameterNames = names;
        this.segmentCount = rawSegments.size();
    }

    public boolean matches(String path) {
        if (pattern == null || path == null) {
            return false;
        }
        return matchSegments(path, null);
    }

    public Map<String, String> extractParameters(String path) {
        var captured = new HashMap<String, String>();
        if (pattern == null || path == null || parameterNames.isEmpty()) {
            return captured;
        }
        if (matchSegments(path, captured)) {
            return captured;
        }
        captured.clear();
        return captured;
    }

    /**
     * Fast path: match and, on success, write captured parameters directly into the request.
     * Returns true on match, false otherwise. No partial writes on failure.
     */
    public boolean matchInto(String path, Request request) {
        if (pattern == null || path == null) {
            return false;
        }
        if (parameterNames.isEmpty()) {
            return matchSegments(path, null);
        }
        // Two-phase: locate segment boundaries, verify full match, then commit captures.
        // Avoids polluting the request when a partial literal matches but a later segment fails.
        var length = path.length();
        var starts = new int[segmentCount];
        var ends = new int[segmentCount];
        var cursor = (length > 0 && path.charAt(0) == '/') ? 1 : 0;
        var segmentIndex = 0;
        while (cursor <= length) {
            var next = indexOfSlash(path, cursor, length);
            var end = next < 0 ? length : next;
            if (segmentIndex >= segmentCount) {
                return false;
            }
            var paramName = parameterAtIndex[segmentIndex];
            if (paramName == null) {
                var literal = literalSegments[segmentIndex];
                if (!regionMatches(path, cursor, end, literal)) {
                    return false;
                }
            } else if (end == cursor) {
                return false;
            }
            starts[segmentIndex] = cursor;
            ends[segmentIndex] = end;
            segmentIndex++;
            if (next < 0) {
                break;
            }
            cursor = next + 1;
        }
        if (segmentIndex != segmentCount) {
            return false;
        }
        for (var i = 0; i < segmentCount; i++) {
            var paramName = parameterAtIndex[i];
            if (paramName != null) {
                request.addPathParam(paramName, path.substring(starts[i], ends[i]));
            }
        }
        return true;
    }

    private boolean matchSegments(String path, Map<String, String> captures) {
        var length = path.length();
        var cursor = (length > 0 && path.charAt(0) == '/') ? 1 : 0;
        var segmentIndex = 0;
        while (cursor <= length) {
            var next = indexOfSlash(path, cursor, length);
            var end = next < 0 ? length : next;
            if (segmentIndex >= segmentCount) {
                return false;
            }
            var paramName = parameterAtIndex[segmentIndex];
            if (paramName == null) {
                var literal = literalSegments[segmentIndex];
                if (!regionMatches(path, cursor, end, literal)) {
                    return false;
                }
            } else {
                if (end == cursor) {
                    return false;
                }
                if (captures != null) {
                    captures.put(paramName, path.substring(cursor, end));
                }
            }
            segmentIndex++;
            if (next < 0) {
                break;
            }
            cursor = next + 1;
        }
        return segmentIndex == segmentCount;
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

    private static List<String> splitSegments(String pattern) {
        var segments = new ArrayList<String>();
        var length = pattern.length();
        var cursor = (length > 0 && pattern.charAt(0) == '/') ? 1 : 0;
        while (cursor <= length) {
            var next = indexOfSlash(pattern, cursor, length);
            var end = next < 0 ? length : next;
            segments.add(pattern.substring(cursor, end));
            if (next < 0) {
                break;
            }
            cursor = next + 1;
        }
        // Preserve the behavior that a trailing slash produces an empty trailing segment
        // only when there is one; mirrors the original regex semantics which required
        // exact match including any trailing slash.
        return segments;
    }

    private static int indexOfSlash(String s, int from, int length) {
        for (var i = from; i < length; i++) {
            if (s.charAt(i) == '/') {
                return i;
            }
        }
        return -1;
    }

    private static boolean regionMatches(String path, int start, int end, String literal) {
        var segmentLen = end - start;
        if (literal.length() != segmentLen) {
            return false;
        }
        for (var i = 0; i < segmentLen; i++) {
            if (path.charAt(start + i) != literal.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isParameterSegment(String segment) {
        return segment.startsWith("${") && segment.endsWith("}");
    }
}
