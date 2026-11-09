package rms;

/** A station was not found, or the data is invalid. */
public class InvalidStationException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidStationException(String message) { super(message); }
}
