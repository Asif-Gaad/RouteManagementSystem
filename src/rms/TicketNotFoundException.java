package rms;

/** The ticket ID does not exist. */
public class TicketNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public TicketNotFoundException(String message) { super(message); }
}
