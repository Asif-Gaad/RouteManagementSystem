package rms;

import java.io.Serializable;

/** A single station on a route. Encapsulation: all fields are private. */
public class Station implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private double distanceFromPrevious; // km from the previous station

    public Station(int id, String name, double distanceFromPrevious) {
        this.id = id;
        this.name = name;
        this.distanceFromPrevious = distanceFromPrevious;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getDistanceFromPrevious() { return distanceFromPrevious; }
    public void setDistanceFromPrevious(double d) { this.distanceFromPrevious = d; }

    @Override
    public String toString() {
        return name + " (ID " + id + ", " + distanceFromPrevious + " km from previous)";
    }
}
