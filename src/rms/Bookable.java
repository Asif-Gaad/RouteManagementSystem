package rms;

/** Interface (abstraction): any system that can book and cancel tickets. */
public interface Bookable {
    Ticket bookTicket(Passenger p, String vehicleId, String source, String destination)
            throws InvalidStationException, VehicleFullException;

    void cancelTicket(int ticketId) throws TicketNotFoundException;
}
