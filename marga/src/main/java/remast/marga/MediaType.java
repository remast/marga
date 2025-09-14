package remast.marga;

public record MediaType(String value) {
    public static final MediaType TEXT_PLAIN = new MediaType("text/plain");
    public static final MediaType TEXT_HTML = new MediaType("text/html");
    public static final MediaType TEXT_CSS = new MediaType("text/css");
    public static final MediaType TEXT_JAVASCRIPT = new MediaType("text/javascript");
    public static final MediaType APPLICATION_JSON = new MediaType("application/json");
    public static final MediaType APPLICATION_XML = new MediaType("application/xml");
    public static final MediaType APPLICATION_PDF = new MediaType("application/pdf");
    public static final MediaType IMAGE_JPEG = new MediaType("image/jpeg");
    public static final MediaType IMAGE_PNG = new MediaType("image/png");
    public static final MediaType IMAGE_GIF = new MediaType("image/gif");
    public static final MediaType IMAGE_SVG = new MediaType("image/svg+xml");

    public MediaType {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Media type value cannot be null or empty");
        }
        value = value.trim();
    }

    public String getValue() {
        return value;
    }
}
