package remast.marga.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "marga")
public class MargaProperties {
    private String host = "localhost";
    private int port = 8080;
    private int readTimeoutMs = 30_000;
    private int acceptBacklog;
    private int maxRequestBodyBytes = 1_048_576;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getAcceptBacklog() {
        return acceptBacklog;
    }

    public void setAcceptBacklog(int acceptBacklog) {
        this.acceptBacklog = acceptBacklog;
    }

    public int getMaxRequestBodyBytes() {
        return maxRequestBodyBytes;
    }

    public void setMaxRequestBodyBytes(int maxRequestBodyBytes) {
        this.maxRequestBodyBytes = maxRequestBodyBytes;
    }
}
