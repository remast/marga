package remast.marga;

public final class Config {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_READ_TIMEOUT_MS = 30_000;
    private static final int DEFAULT_ACCEPT_BACKLOG = 0;
    private static final int DEFAULT_MAX_REQUEST_BODY_BYTES = 1_048_576;

    private final String host;
    private final int port;
    private final int readTimeoutMs;
    private final int acceptBacklog;
    private final int maxRequestBodyBytes;

    public Config() {
        this(builder());
    }

    private Config(Builder builder) {
        this.host = requireHost(builder.host);
        this.port = requirePort(builder.port);
        this.readTimeoutMs = requirePositive(builder.readTimeoutMs, "readTimeoutMs");
        this.acceptBacklog = requireNonNegative(builder.acceptBacklog, "acceptBacklog");
        this.maxRequestBodyBytes = requirePositive(builder.maxRequestBodyBytes, "maxRequestBodyBytes");
    }

    public static Config defaults() {
        return new Config();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Config fromEnv() {
        var builder = builder();
        var host = System.getenv("MARGA_HOST");
        var port = System.getenv("MARGA_PORT");
        var readTimeout = System.getenv("MARGA_READ_TIMEOUT_MS");
        var acceptBacklog = System.getenv("MARGA_ACCEPT_BACKLOG");
        var maxRequestBodyBytes = System.getenv("MARGA_MAX_REQUEST_BODY_BYTES");

        if (host != null && !host.isBlank()) {
            builder.host(host);
        }
        if (port != null && !port.isBlank()) {
            builder.port(Integer.parseInt(port));
        }
        if (readTimeout != null && !readTimeout.isBlank()) {
            builder.readTimeoutMs(Integer.parseInt(readTimeout));
        }
        if (acceptBacklog != null && !acceptBacklog.isBlank()) {
            builder.acceptBacklog(Integer.parseInt(acceptBacklog));
        }
        if (maxRequestBodyBytes != null && !maxRequestBodyBytes.isBlank()) {
            builder.maxRequestBodyBytes(Integer.parseInt(maxRequestBodyBytes));
        }

        return builder.build();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public int getAcceptBacklog() {
        return acceptBacklog;
    }

    public int getMaxRequestBodyBytes() {
        return maxRequestBodyBytes;
    }

    public String getServerUrl() {
        return "http://" + host + ":" + port;
    }

    public String getServerUrl(int boundPort) {
        return "http://" + host + ":" + boundPort;
    }

    @Override
    public String toString() {
        return String.format(
            "Config{host='%s', port=%d, readTimeoutMs=%d, acceptBacklog=%d, maxRequestBodyBytes=%d}",
            host,
            port,
            readTimeoutMs,
            acceptBacklog,
            maxRequestBodyBytes
        );
    }

    private static String requireHost(String host) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host cannot be null or blank");
        }
        return host;
    }

    private static int requirePort(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
        return port;
    }

    private static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }
        return value;
    }

    private static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be >= 0");
        }
        return value;
    }

    public static final class Builder {
        private String host = DEFAULT_HOST;
        private int port = DEFAULT_PORT;
        private int readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
        private int acceptBacklog = DEFAULT_ACCEPT_BACKLOG;
        private int maxRequestBodyBytes = DEFAULT_MAX_REQUEST_BODY_BYTES;

        private Builder() {
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder readTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        public Builder acceptBacklog(int acceptBacklog) {
            this.acceptBacklog = acceptBacklog;
            return this;
        }

        public Builder maxRequestBodyBytes(int maxRequestBodyBytes) {
            this.maxRequestBodyBytes = maxRequestBodyBytes;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }
}
