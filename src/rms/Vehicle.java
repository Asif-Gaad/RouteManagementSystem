package rms;

import java.io.Serializable;

/** Abstract parent class. Every vehicle type has its own fare formula (polymorphism). */
public abstract class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    private String vehicleId;
    private int capacity;
    private int bookedSeats;
    private Route route;

    protected Vehicle(String vehicleId, int capacity, Route route) {
        this.vehicleId = vehicleId;
        this.capacity = capacity;
        this.route = route;
    }

    /** Each child class implements its own formula. */
    public abstract double calculateFare(double distanceKm);

    public abstract String getType();

    public void bookSeat() throws VehicleFullException {
        if (bookedSeats >= capacity)
            throw new VehicleFullException(getType() + " " + vehicleId + " is full.");
        bookedSeats++;
    }

    public void releaseSeat() {
        if (bookedSeats > 0) bookedSeats--;
    }

    public String getVehicleId() { return vehicleId; }
    public int getCapacity() { return capacity; }
    public int getBookedSeats() { return bookedSeats; }
    public int getAvailableSeats() { return capacity - bookedSeats; }
    public Route getRoute() { return route; }

    @Override
    public String toString() {
        return getType() + " " + vehicleId + " | Route: " + route.getRouteName()
                + " | Seats: " + bookedSeats + "/" + capacity;
    }
}
