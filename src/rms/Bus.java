package rms;

/** Bus: base fare 50 + 4 Rs per km. */
public class Bus extends Vehicle {
    private static final long serialVersionUID = 1L;
    private static final double BASE = 50, RATE = 4;

    public Bus(String id, int capacity, Route route) { super(id, capacity, route); }

    @Override
    public double calculateFare(double km) { return BASE + RATE * km; }

    @Override
    public String getType() { return "Bus"; }
}
