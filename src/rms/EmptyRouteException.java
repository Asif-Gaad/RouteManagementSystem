package rms;

/** An operation was attempted on an empty route. */
public class EmptyRouteException extends Exception {
    private static final long serialVersionUID = 1L;

    public EmptyRouteException(String message) { super(message); }
}
