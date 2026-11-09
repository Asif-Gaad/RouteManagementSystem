package rms;

/** The vehicle has no free seats left. */
public class VehicleFullException extends Exception {
    private static final long serialVersionUID = 1L;

    public VehicleFullException(String message) { super(message); }
}
