package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void defaultConstructor() {
        var config = new Config();
        
        assertEquals("localhost", config.getHost());
        assertEquals(8080, config.getPort());
        assertEquals("http://localhost:8080", config.getServerUrl());
    }

    @Test
    void toStringMethod() {
        var config = new Config();
        var result = config.toString();
        
        assertTrue(result.contains("Config{host='localhost', port=8080}"));
    }

    @Test
    void getters() {
        var config = new Config();
        
        assertNotNull(config.getHost());
        assertNotNull(config.getServerUrl());
        assertTrue(config.getPort() > 0);
    }
}
