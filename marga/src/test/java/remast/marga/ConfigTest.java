package remast.marga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void defaultConstructor() {
        var config = new Config();
        
        assertEquals("localhost", config.getHost());
        assertEquals(8080, config.getPort());
        assertEquals(30_000, config.getReadTimeoutMs());
        assertEquals(0, config.getAcceptBacklog());
        assertEquals(1_048_576, config.getMaxRequestBodyBytes());
        assertEquals("http://localhost:8080", config.getServerUrl());
    }

    @Test
    void toStringMethod() {
        var config = new Config();
        var result = config.toString();
        
        assertTrue(result.contains("host='localhost'"));
        assertTrue(result.contains("port=8080"));
        assertTrue(result.contains("readTimeoutMs=30000"));
        assertTrue(result.contains("acceptBacklog=0"));
    }

    @Test
    void getters() {
        var config = new Config();
        
        assertNotNull(config.getHost());
        assertNotNull(config.getServerUrl());
        assertTrue(config.getPort() > 0);
        assertTrue(config.getReadTimeoutMs() > 0);
    }

    @Test
    void builderShouldOverrideDefaults() {
        var config = Config.builder()
            .host("127.0.0.1")
            .port(9090)
            .readTimeoutMs(5_000)
            .acceptBacklog(100)
            .maxRequestBodyBytes(2048)
            .build();

        assertEquals("127.0.0.1", config.getHost());
        assertEquals(9090, config.getPort());
        assertEquals(5_000, config.getReadTimeoutMs());
        assertEquals(100, config.getAcceptBacklog());
        assertEquals(2048, config.getMaxRequestBodyBytes());
    }

    @Test
    void builderShouldValidateValues() {
        assertThrows(IllegalArgumentException.class, () -> Config.builder().host(" ").build());
        assertThrows(IllegalArgumentException.class, () -> Config.builder().port(-1).build());
        assertThrows(IllegalArgumentException.class, () -> Config.builder().port(70_000).build());
        assertThrows(IllegalArgumentException.class, () -> Config.builder().readTimeoutMs(0).build());
        assertThrows(IllegalArgumentException.class, () -> Config.builder().acceptBacklog(-1).build());
        assertThrows(IllegalArgumentException.class, () -> Config.builder().maxRequestBodyBytes(0).build());
    }
}
