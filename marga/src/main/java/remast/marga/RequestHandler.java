package remast.marga;

@FunctionalInterface
public interface RequestHandler {
    Response handle(Request request);
}
