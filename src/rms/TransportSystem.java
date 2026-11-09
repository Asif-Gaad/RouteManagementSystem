package rms;

import java.io.Serializable;
import java.util.ArrayList;

/** Main manager class. Composition: it holds ArrayLists of routes, vehicles and tickets. */
public class TransportSystem implements Bookable, Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Route> routes = new ArrayList<>();
    private ArrayList<Vehicle> vehicles = new ArrayList<>();
    private ArrayList<Ticket> tickets = new ArrayList<>();
    private int nextTicketId = 1;
    private int nextStationId = 1;

    // ---------- Routes ----------
    public void addRoute(Route r) { routes.add(r); }

    public Route findRoute(String name) {
        for (Route r : routes)
            if (r.getRouteName().equalsIgnoreCase(name)) return r;
        return null;
    }

    public ArrayList<Route> getRoutes() { return routes; }
    public int generateStationId() { return nextStationId++; }

    // ---------- Vehicles ----------
    public void addVehicle(Vehicle v) { vehicles.add(v); }

    public Vehicle findVehicle(String id) {
        for (Vehicle v : vehicles)
            if (v.getVehicleId().equalsIgnoreCase(id)) return v;
        return null;
    }

    public ArrayList<Vehicle> getVehicles() { return vehicles; }
    public ArrayList<Ticket> getTickets() { return tickets; }

    // ---------- Bookable interface ----------
    @Override
    public Ticket bookTicket(Passenger p, String vehicleId, String source, String destination)
            throws InvalidStationException, VehicleFullException {
        Vehicle v = findVehicle(vehicleId);
        if (v == null)
            throw new InvalidStationException("Vehicle '" + vehicleId + "' was not found.");
        double km = v.getRoute().getDistance(source, destination);
        if (km < 0)
            throw new InvalidStationException("Source or destination is not on this vehicle's route.");
        if (km == 0)
            throw new InvalidStationException("Source and destination cannot be the same.");

        v.bookSeat(); // throws VehicleFullException if the vehicle is full
        Ticket t = new Ticket(nextTicketId++, p, v, source, destination, v.calculateFare(km));
        tickets.add(t);
        return t;
    }

    @Override
    public void cancelTicket(int ticketId) throws TicketNotFoundException {
        for (Ticket t : tickets) {
            if (t.getTicketId() == ticketId) {
                t.getVehicle().releaseSeat();
                tickets.remove(t);
                return;
            }
        }
        throw new TicketNotFoundException("Ticket #" + ticketId + " was not found.");
    }

    // ---------- Utilities ----------
    /** Removes everything and starts fresh. */
    public void clearAll() {
        routes.clear();
        vehicles.clear();
        tickets.clear();
        nextTicketId = 1;
        nextStationId = 1;
    }

    /** Adds demo data. Returns false if the sample route already exists. */
    public boolean loadSampleData() {
        if (findRoute("Karachi-Khairpur") != null) return false;
        Route r = new Route("Karachi-Khairpur");
        String[] names = {"Karachi", "Hyderabad", "Nawabshah", "Sukkur", "Khairpur"};
        double[] dist  = {0, 165, 130, 125, 20};
        for (int i = 0; i < names.length; i++)
            r.addStation(new Station(generateStationId(), names[i], dist[i]));
        addRoute(r);
        if (findVehicle("BUS-101") == null) addVehicle(new Bus("BUS-101", 40, r));
        if (findVehicle("TRN-201") == null) addVehicle(new Train("TRN-201", 300, r));
        return true;
    }
}
