package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HttpHeaderTest {

    @Test
    void getValue_returnsCorrectHeaderName() {
        assertEquals("Content-Type", HttpHeader.CONTENT_TYPE.getValue());
        assertEquals("Content-Length", HttpHeader.CONTENT_LENGTH.getValue());
        assertEquals("Authorization", HttpHeader.AUTHORIZATION.getValue());
        assertEquals("User-Agent", HttpHeader.USER_AGENT.getValue());
    }

    @Test
    void toString_returnsCorrectHeaderName() {
        assertEquals("Content-Type", HttpHeader.CONTENT_TYPE.toString());
        assertEquals("Content-Length", HttpHeader.CONTENT_LENGTH.toString());
        assertEquals("Authorization", HttpHeader.AUTHORIZATION.toString());
        assertEquals("User-Agent", HttpHeader.USER_AGENT.toString());
    }

    @Test
    void fromString_withValidHeaderName_returnsCorrectEnum() {
        assertEquals(HttpHeader.CONTENT_TYPE, HttpHeader.fromString("Content-Type"));
        assertEquals(HttpHeader.CONTENT_TYPE, HttpHeader.fromString("content-type"));
        assertEquals(HttpHeader.CONTENT_TYPE, HttpHeader.fromString("CONTENT-TYPE"));
        assertEquals(HttpHeader.AUTHORIZATION, HttpHeader.fromString("Authorization"));
        assertEquals(HttpHeader.USER_AGENT, HttpHeader.fromString("user-agent"));
    }

    @Test
    void fromString_withInvalidHeaderName_returnsNull() {
        assertNull(HttpHeader.fromString("Invalid-Header"));
        assertNull(HttpHeader.fromString(""));
        assertNull(HttpHeader.fromString(null));
    }

    @Test
    void allHeadersHaveValidNames() {
        for (HttpHeader header : HttpHeader.values()) {
            assertNotNull(header.getValue());
            assertFalse(header.getValue().trim().isEmpty());
            // Most HTTP headers contain hyphens, but some don't (like ETag)
            assertTrue(header.getValue().length() > 0);
        }
    }

    @Test
    void commonHeadersArePresent() {
        assertNotNull(HttpHeader.CONTENT_TYPE);
        assertNotNull(HttpHeader.CONTENT_LENGTH);
        assertNotNull(HttpHeader.CONTENT_ENCODING);
        assertNotNull(HttpHeader.AUTHORIZATION);
        assertNotNull(HttpHeader.USER_AGENT);
        assertNotNull(HttpHeader.ACCEPT);
        assertNotNull(HttpHeader.CACHE_CONTROL);
        assertNotNull(HttpHeader.CONNECTION);
    }
}
