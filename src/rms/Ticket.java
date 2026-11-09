package rms;

import java.io.Serializable;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ticketId;
    private Passenger passenger;
    private Vehicle vehicle;
    private String source;
    private String destination;
    private double fare;

    public Ticket(int ticketId, Passenger passenger, Vehicle vehicle,
                  String source, String destination, double fare) {
        this.ticketId = ticketId;
        this.passenger = passenger;
        this.vehicle = vehicle;
        this.source = source;
        this.destination = destination;
        this.fare = fare;
    }

    public int getTicketId() { return ticketId; }
    public Passenger getPassenger() { return passenger; }
    public Vehicle getVehicle() { return vehicle; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public double getFare() { return fare; }

    @Override
    public String toString() {
        return "Ticket #" + ticketId + " | " + passenger.getName() + " | "
                + vehicle.getType() + " " + vehicle.getVehicleId() + " | "
                + source + " -> " + destination + " | Rs " + fare;
    }
}
