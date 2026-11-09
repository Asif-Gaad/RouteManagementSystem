package rms;

import java.util.Scanner;

/** Console version of the application (the GUI version is MainGUI). */
public class Main {
    private static final Scanner sc = new Scanner(System.in);

    // ---------- Safe input helpers (robustness) ----------
    private static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                double d = Double.parseDouble(readLine(prompt));
                if (d < 0) throw new NumberFormatException();
                return d;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a positive number.");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) return s;
            System.out.println("This field cannot be empty.");
        }
    }

    private static void menu() {
        System.out.println("\n===== ROUTE MANAGEMENT SYSTEM =====");
        System.out.println(" 1. Create Route");
        System.out.println(" 2. Add Station to Route");
        System.out.println(" 3. Insert Station After...");
        System.out.println(" 4. Remove Station");
        System.out.println(" 5. Display Route");
        System.out.println(" 6. Reverse Route");
        System.out.println(" 7. Add Vehicle (Bus/Train)");
        System.out.println(" 8. Show Vehicles");
        System.out.println(" 9. Book Ticket");
        System.out.println("10. Cancel Ticket");
        System.out.println("11. Show Tickets");
        System.out.println("12. Load Sample Data");
        System.out.println(" 0. Save & Exit");
    }

    private static Route pickRoute(TransportSystem sys) {
        if (sys.getRoutes().isEmpty()) {
            System.out.println("Create a route first (option 1).");
            return null;
        }
        for (Route r : sys.getRoutes()) System.out.println("  - " + r);
        Route r = sys.findRoute(readNonEmpty("Route name: "));
        if (r == null) System.out.println("Route not found.");
        return r;
    }

    public static void main(String[] args) {
        TransportSystem sys = FileManager.load();
        int choice = -1;

        while (choice != 0) {
            menu();
            choice = readInt("Choice: ");
            try {
                switch (choice) {
                    case 1: {
                        String name = readNonEmpty("New route name: ");
                        if (sys.findRoute(name) != null) System.out.println("This route already exists.");
                        else { sys.addRoute(new Route(name)); System.out.println("Route created."); }
                        break;
                    }
                    case 2: {
                        Route r = pickRoute(sys);
                        if (r == null) break;
                        String n = readNonEmpty("Station name: ");
                        double d = r.isEmpty() ? 0 : readDouble("Distance from previous station (km): ");
                        r.addStation(new Station(sys.generateStationId(), n, d));
                        System.out.println("Station added.");
                        break;
                    }
                    case 3: {
                        Route r = pickRoute(sys);
                        if (r == null) break;
                        System.out.println(r.display());
                        String after = readNonEmpty("Insert after which station: ");
                        String n = readNonEmpty("New station name: ");
                        double d = readDouble("Distance from '" + after + "' (km): ");
                        r.insertAfter(after, new Station(sys.generateStationId(), n, d));
                        System.out.println("Station inserted.");
                        break;
                    }
                    case 4: {
                        Route r = pickRoute(sys);
                        if (r == null) break;
                        r.removeStation(readNonEmpty("Name of station to remove: "));
                        System.out.println("Station removed.");
                        break;
                    }
                    case 5: {
                        Route r = pickRoute(sys);
                        if (r != null) System.out.println(r.display());
                        break;
                    }
                    case 6: {
                        Route r = pickRoute(sys);
                        if (r != null) { r.reverse(); System.out.println("Route reversed.\n" + r.display()); }
                        break;
                    }
                    case 7: {
                        Route r = pickRoute(sys);
                        if (r == null) break;
                        String id = readNonEmpty("Vehicle ID: ");
                        if (sys.findVehicle(id) != null) { System.out.println("This ID already exists."); break; }
                        int cap = readInt("Seats (capacity): ");
                        if (cap <= 0) { System.out.println("Capacity must be greater than 0."); break; }
                        int type = readInt("Type (1 = Bus, 2 = Train): ");
                        Vehicle v;
                        if (type == 1) v = new Bus(id, cap, r);
                        else if (type == 2) v = new Train(id, cap, r);
                        else { System.out.println("Invalid type."); break; }
                        sys.addVehicle(v);
                        System.out.println("Vehicle added.");
                        break;
                    }
                    case 8: {
                        if (sys.getVehicles().isEmpty()) System.out.println("No vehicles yet.");
                        for (Vehicle v : sys.getVehicles()) System.out.println("  " + v);
                        break;
                    }
                    case 9: {
                        if (sys.getVehicles().isEmpty()) { System.out.println("Add a vehicle first."); break; }
                        for (Vehicle v : sys.getVehicles()) System.out.println("  " + v);
                        String vid = readNonEmpty("Vehicle ID: ");
                        Vehicle v = sys.findVehicle(vid);
                        if (v != null) System.out.println(v.getRoute().display());
                        String pn = readNonEmpty("Passenger name: ");
                        String cnic = readNonEmpty("CNIC: ");
                        String src = readNonEmpty("Source station: ");
                        String dst = readNonEmpty("Destination station: ");
                        Ticket t = sys.bookTicket(new Passenger(pn, cnic), vid, src, dst);
                        System.out.println("Booking successful!\n  " + t);
                        break;
                    }
                    case 10: {
                        sys.cancelTicket(readInt("Ticket ID: "));
                        System.out.println("Ticket cancelled.");
                        break;
                    }
                    case 11: {
                        if (sys.getTickets().isEmpty()) System.out.println("No tickets yet.");
                        for (Ticket t : sys.getTickets()) System.out.println("  " + t);
                        break;
                    }
                    case 12: {
                        if (sys.loadSampleData()) System.out.println("Sample data loaded.");
                        else System.out.println("Sample data is already loaded.");
                        break;
                    }
                    case 0:
                        FileManager.save(sys);
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option, please try again.");
                }
            } catch (InvalidStationException | EmptyRouteException
                     | VehicleFullException | TicketNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
