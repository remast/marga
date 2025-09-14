package remast.marga;

public class Config {
    private final String host;
    private final int port;
    
    public Config() {
        this.host = "localhost";
        this.port = 8080;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getServerUrl() {
        return "http://" + host + ":" + port;
    }
    
    @Override
    public String toString() {
        return String.format("Config{host='%s', port=%d}", host, port);
    }
}
