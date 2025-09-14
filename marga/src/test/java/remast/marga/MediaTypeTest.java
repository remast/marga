package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MediaTypeTest {

    @Test
    void predefinedConstants() {
        assertEquals("text/plain", MediaType.TEXT_PLAIN.getValue());
        assertEquals("text/html", MediaType.TEXT_HTML.getValue());
        assertEquals("text/css", MediaType.TEXT_CSS.getValue());
        assertEquals("text/javascript", MediaType.TEXT_JAVASCRIPT.getValue());
        assertEquals("application/json", MediaType.APPLICATION_JSON.getValue());
        assertEquals("application/xml", MediaType.APPLICATION_XML.getValue());
        assertEquals("application/pdf", MediaType.APPLICATION_PDF.getValue());
        assertEquals("image/jpeg", MediaType.IMAGE_JPEG.getValue());
        assertEquals("image/png", MediaType.IMAGE_PNG.getValue());
        assertEquals("image/gif", MediaType.IMAGE_GIF.getValue());
        assertEquals("image/svg+xml", MediaType.IMAGE_SVG.getValue());
    }

    @Test
    void toStringReturnsValue() {
        assertEquals("MediaType[value=text/plain]", MediaType.TEXT_PLAIN.toString());
        assertEquals("MediaType[value=application/json]", MediaType.APPLICATION_JSON.toString());
        assertEquals("MediaType[value=image/jpeg]", MediaType.IMAGE_JPEG.toString());
    }

    @Test
    void customMediaType() {
        var customType = new MediaType("application/custom");
        assertEquals("application/custom", customType.getValue());
        assertEquals("MediaType[value=application/custom]", customType.toString());
    }

    @Test
    void mediaTypeWithWhitespace() {
        var mediaType = new MediaType("  text/plain  ");
        assertEquals("text/plain", mediaType.getValue());
    }

    @Test
    void nullValueThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new MediaType(null));
    }

    @Test
    void emptyValueThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new MediaType(""));
        assertThrows(IllegalArgumentException.class, () -> new MediaType("   "));
    }

    @Test
    void equalsAndHashCode() {
        var type1 = new MediaType("text/plain");
        var type2 = new MediaType("text/plain");
        var type3 = new MediaType("application/json");

        assertEquals(type1, type2);
        assertNotEquals(type1, type3);
        assertEquals(type1.hashCode(), type2.hashCode());
        assertNotEquals(type1.hashCode(), type3.hashCode());
    }

    @Test
    void equalsWithPredefinedConstants() {
        var customType = new MediaType("text/plain");
        assertEquals(MediaType.TEXT_PLAIN, customType);
        assertEquals(customType, MediaType.TEXT_PLAIN);
    }
}
