package remast.marga;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;


class PatternMatcherTest {

    @Test
    void nullPattern() {
        var matcher = new PatternMatcher(null);
        
        assertFalse(matcher.matches("/test"));
        assertTrue(matcher.extractParameters("/test").isEmpty());
        assertFalse(matcher.hasParameters());
        assertNull(matcher.getPattern());
        assertTrue(matcher.getParameterNames().isEmpty());
    }

    @Test
    void exactMatch() {
        var matcher = new PatternMatcher("/users");
        
        assertTrue(matcher.matches("/users"));
        assertFalse(matcher.matches("/user"));
        assertFalse(matcher.matches("/users/123"));
        assertFalse(matcher.hasParameters());
        assertEquals("/users", matcher.getPattern());
    }

    @Test
    void singleParameter() {
        var matcher = new PatternMatcher("/users/${id}");
        
        assertTrue(matcher.matches("/users/123"));
        assertTrue(matcher.matches("/users/abc"));
        assertFalse(matcher.matches("/users"));
        assertFalse(matcher.matches("/users/123/posts"));
        assertTrue(matcher.hasParameters());
        assertEquals("/users/${id}", matcher.getPattern());
        assertEquals(1, matcher.getParameterNames().size());
        assertTrue(matcher.getParameterNames().contains("id"));
    }

    @Test
    void multipleParameters() {
        var matcher = new PatternMatcher("/users/${userId}/posts/${postId}");
        
        assertTrue(matcher.matches("/users/123/posts/456"));
        assertTrue(matcher.matches("/users/john/posts/my-post"));
        assertFalse(matcher.matches("/users/123"));
        assertFalse(matcher.matches("/users/123/posts"));
        assertTrue(matcher.hasParameters());
        assertEquals(2, matcher.getParameterNames().size());
        assertTrue(matcher.getParameterNames().contains("userId"));
        assertTrue(matcher.getParameterNames().contains("postId"));
    }

    @Test
    void extractSingleParameter() {
        var matcher = new PatternMatcher("/users/${id}");
        
        var params = matcher.extractParameters("/users/123");
        assertEquals(1, params.size());
        assertEquals("123", params.get("id"));
        
        var emptyParams = matcher.extractParameters("/users");
        assertTrue(emptyParams.isEmpty());
    }

    @Test
    void extractMultipleParameters() {
        var matcher = new PatternMatcher("/users/${userId}/posts/${postId}");
        
        var params = matcher.extractParameters("/users/123/posts/456");
        assertEquals(2, params.size());
        assertEquals("123", params.get("userId"));
        assertEquals("456", params.get("postId"));
    }

    @Test
    void parameterWithSpecialCharacters() {
        var matcher = new PatternMatcher("/files/${filename}");
        
        assertTrue(matcher.matches("/files/document.pdf"));
        assertTrue(matcher.matches("/files/my-file_123.txt"));
        assertFalse(matcher.matches("/files/document.pdf/extra"));
        
        var params = matcher.extractParameters("/files/document.pdf");
        assertEquals("document.pdf", params.get("filename"));
    }

    @Test
    void complexPattern() {
        var matcher = new PatternMatcher("/api/v1/users/${userId}/posts/${postId}/comments/${commentId}");
        
        assertTrue(matcher.matches("/api/v1/users/123/posts/456/comments/789"));
        assertFalse(matcher.matches("/api/v1/users/123/posts/456"));
        
        var params = matcher.extractParameters("/api/v1/users/123/posts/456/comments/789");
        assertEquals(3, params.size());
        assertEquals("123", params.get("userId"));
        assertEquals("456", params.get("postId"));
        assertEquals("789", params.get("commentId"));
    }

    @Test
    void emptyParameterName() {
        // Empty parameter names should not be allowed as they create invalid regex
        assertThrows(Exception.class, () -> new PatternMatcher("/test/${}"));
    }

    @Test
    void noParameters() {
        var matcher = new PatternMatcher("/static/path");
        
        assertTrue(matcher.matches("/static/path"));
        assertFalse(matcher.hasParameters());
        assertTrue(matcher.getParameterNames().isEmpty());
        assertTrue(matcher.extractParameters("/static/path").isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "/users/${id}, /users/123, true",
        "/users/${id}, /users, false",
        "/users/${id}, /users/123/posts, false",
        "/api/${version}/users, /api/v1/users, true",
        "/api/${version}/users, /api/users, false"
    })
    void parameterizedMatching(String pattern, String path, boolean shouldMatch) {
        var matcher = new PatternMatcher(pattern);
        assertEquals(shouldMatch, matcher.matches(path));
    }
}
