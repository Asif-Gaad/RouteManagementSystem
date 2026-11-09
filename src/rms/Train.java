package rms;

/** Train: base fare 100 + 2.5 Rs per km (cheaper per km on long trips). */
public class Train extends Vehicle {
    private static final long serialVersionUID = 1L;
    private static final double BASE = 100, RATE = 2.5;

    public Train(String id, int capacity, Route route) { super(id, capacity, route); }

    @Override
    public double calculateFare(double km) { return BASE + RATE * km; }

    @Override
    public String getType() { return "Train"; }
}
