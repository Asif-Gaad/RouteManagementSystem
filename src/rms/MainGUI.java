package rms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.List;

/** Swing GUI. The logic lives in the same classes as the console version. */
public class MainGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    // ---------- Colours ----------
    static final Color NAVY = new Color(15, 23, 42), INDIGO = new Color(30, 58, 138);
    static final Color BLUE = new Color(37, 99, 235), BLUE_D = new Color(29, 78, 216);
    static final Color RED = new Color(220, 38, 38), RED_D = new Color(185, 28, 28);
    static final Color SLATE = new Color(226, 232, 240), SLATE_D = new Color(203, 213, 225);
    static final Color BG = new Color(241, 245, 249), BORDER = new Color(226, 232, 240);
    static final Color TEXT = new Color(30, 41, 59), MUTED = new Color(100, 116, 139);
    static final Color GREEN = new Color(22, 163, 74);

    private final TransportSystem sys;

    // Navigation
    private final CardLayout cards = new CardLayout();
    private final JPanel views = new JPanel(cards);
    private final Btn navRoutes = new Btn("Routes"), navVehicles = new Btn("Vehicles"), navTickets = new Btn("Tickets");

    // Stat cards
    private final JLabel statRoutes = bigNumber(), statStations = bigNumber(),
                         statVehicles = bigNumber(), statTickets = bigNumber();

    // Routes view
    private final JComboBox<String> routeCombo = new JComboBox<>();
    private final RoutePanel routePanel = new RoutePanel();
    private final JLabel routeInfo = new JLabel(" ");

    // Vehicles view
    private final DefaultTableModel vehicleModel = readOnlyModel("Type", "Vehicle ID", "Route", "Booked", "Capacity", "Available");

    // Tickets view
    private final DefaultTableModel ticketModel = readOnlyModel("Ticket #", "Passenger", "CNIC", "Vehicle", "From", "To", "Fare (Rs)");
    private final JTable ticketTable = new JTable(ticketModel);
    private final JComboBox<String> bookVehicle = new JComboBox<>();
    private final JComboBox<String> bookFrom = new JComboBox<>();
    private final JComboBox<String> bookTo = new JComboBox<>();
    private final JTextField bookName = new JTextField(16);
    private final JTextField bookCnic = new JTextField(16);
    private final JLabel fareLabel = new JLabel("Fare: -");

    // Status bar
    private final JLabel status = new JLabel("Ready");

    private boolean updating = false;
    private String forceRoute = null;

    public MainGUI(TransportSystem sys) {
        super("Route Management System");
        this.sys = sys;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                FileManager.save(MainGUI.this.sys);
                dispose();
                System.exit(0);
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 20, 8, 20));
        body.add(buildStats(), BorderLayout.NORTH);

        views.setOpaque(false);
        views.add(buildRoutesView(), "routes");
        views.add(buildVehiclesView(), "vehicles");
        views.add(buildTicketsView(), "tickets");
        body.add(views, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);

        setMinimumSize(new Dimension(900, 620));
        setSize(1060, 700);
        setLocationRelativeTo(null);
        showView("routes");
        refreshAll();
    }

    // =====================================================================
    //  Small custom UI components
    // =====================================================================
    /** Rounded button with hover effect. */
    static class Btn extends JButton {
        private static final long serialVersionUID = 1L;
        private Color base = SLATE, hover = SLATE_D;
        private boolean over;

        Btn(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(getFont().deriveFont(Font.BOLD, 13f));
            setForeground(TEXT);
            setBorder(new EmptyBorder(9, 18, 9, 18));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
            });
        }

        Btn colors(Color base, Color hover, Color fg) {
            this.base = base; this.hover = hover; setForeground(fg);
            repaint();
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(over ? hover : base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static Btn primary(String t) { return new Btn(t).colors(BLUE, BLUE_D, Color.WHITE); }
    private static Btn danger(String t) { return new Btn(t).colors(RED, RED_D, Color.WHITE); }
    private static Btn secondary(String t) { return new Btn(t).colors(SLATE, SLATE_D, TEXT); }

    /** White rounded card with a thin border. */
    static class Card extends JPanel {
        private static final long serialVersionUID = 1L;

        Card(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 16, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Dark blue gradient header. */
    static class Header extends JPanel {
        private static final long serialVersionUID = 1L;

        Header() { super(new BorderLayout()); setOpaque(false); setBorder(new EmptyBorder(14, 24, 14, 24)); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, NAVY, getWidth(), 0, INDIGO));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static JLabel bigNumber() {
        JLabel l = new JLabel("0");
        l.setFont(l.getFont().deriveFont(Font.BOLD, 28f));
        l.setForeground(TEXT);
        return l;
    }

    private static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 17f));
        l.setForeground(TEXT);
        return l;
    }

    private static JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setForeground(MUTED);
        return l;
    }

    private static DefaultTableModel readOnlyModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private static void styleField(JComponent c) {
        c.setPreferredSize(new Dimension(140, 34));
        c.setMinimumSize(new Dimension(110, 34));
        if (c instanceof JTextField)
            c.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SLATE_D, 1, true), new EmptyBorder(2, 8, 2, 8)));
    }

    private static void styleTable(JTable t) {
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setFocusable(false);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(10, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setOpaque(true);
                l.setBackground(NAVY);
                l.setForeground(Color.WHITE);
                l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                return l;
            }
        });
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, sel, false, r, c);
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                if (!sel) {
                    l.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    l.setForeground(TEXT);
                }
                return l;
            }
        });
    }

    private static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private static JPanel row(int align, JComponent... comps) {
        JPanel p = new JPanel(new FlowLayout(align, 10, 0));
        p.setOpaque(false);
        for (JComponent c : comps) p.add(c);
        return p;
    }

    // =====================================================================
    //  Layout
    // =====================================================================
    private JPanel buildHeader() {
        Header h = new Header();
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Route Management System");
        t.setFont(t.getFont().deriveFont(Font.BOLD, 22f));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Manage routes with a doubly linked list, vehicles and ticket bookings");
        s.setFont(s.getFont().deriveFont(Font.PLAIN, 13f));
        s.setForeground(new Color(186, 200, 230));
        left.add(t);
        left.add(Box.createVerticalStrut(2));
        left.add(s);
        h.add(left, BorderLayout.WEST);

        navRoutes.addActionListener(e -> showView("routes"));
        navVehicles.addActionListener(e -> showView("vehicles"));
        navTickets.addActionListener(e -> showView("tickets"));
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        nav.setOpaque(false);
        nav.add(navRoutes);
        nav.add(navVehicles);
        nav.add(navTickets);
        h.add(nav, BorderLayout.EAST);
        return h;
    }

    private void showView(String name) {
        cards.show(views, name);
        Btn[] all = {navRoutes, navVehicles, navTickets};
        String[] names = {"routes", "vehicles", "tickets"};
        for (int i = 0; i < all.length; i++) {
            if (names[i].equals(name)) all[i].colors(Color.WHITE, Color.WHITE, NAVY);
            else all[i].colors(new Color(255, 255, 255, 30), new Color(255, 255, 255, 60), Color.WHITE);
        }
    }

    private JPanel buildStats() {
        JPanel p = new JPanel(new GridLayout(1, 4, 14, 0));
        p.setOpaque(false);
        p.add(statCard(statRoutes, "Routes"));
        p.add(statCard(statStations, "Stations"));
        p.add(statCard(statVehicles, "Vehicles"));
        p.add(statCard(statTickets, "Tickets booked"));
        return p;
    }

    private Card statCard(JLabel number, String caption) {
        Card c = new Card(new BorderLayout(0, 2));
        c.setBorder(new EmptyBorder(10, 18, 10, 18));
        JLabel cap = new JLabel(caption);
        cap.setForeground(MUTED);
        cap.setFont(cap.getFont().deriveFont(Font.PLAIN, 13f));
        c.add(number, BorderLayout.CENTER);
        c.add(cap, BorderLayout.SOUTH);
        return c;
    }

    private JPanel buildFooter() {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(6, 20, 14, 20));
        status.setForeground(MUTED);
        status.setFont(status.getFont().deriveFont(Font.PLAIN, 13f));
        f.add(status, BorderLayout.WEST);

        Btn sample = primary("Load Sample Data");
        sample.addActionListener(e -> {
            if (sys.loadSampleData()) { changed(); setStatus("Sample data loaded.", true); }
            else setStatus("The sample route already exists.", false);
        });
        Btn save = secondary("Save");
        save.addActionListener(e -> { FileManager.save(sys); setStatus("Data saved to data.ser.", true); });
        Btn clear = danger("Clear All Data");
        clear.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Delete all routes, vehicles and tickets?", "Clear All Data",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                sys.clearAll();
                changed();
                setStatus("All data cleared.", true);
            }
        });
        f.add(row(FlowLayout.RIGHT, sample, save, clear), BorderLayout.EAST);
        return f;
    }

    private Card buildRoutesView() {
        Card c = new Card(new BorderLayout(0, 12));

        routeCombo.addActionListener(e -> { if (!updating) onRouteChanged(); });
        routeCombo.setPreferredSize(new Dimension(230, 34));
        Btn newRoute = primary("+ New Route");
        newRoute.addActionListener(e -> newRoute());
        routeInfo.setForeground(MUTED);
        routeInfo.setFont(routeInfo.getFont().deriveFont(Font.PLAIN, 13f));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(title("Route (Doubly Linked List: HEAD <-> ... <-> TAIL)"), BorderLayout.NORTH);
        top.add(row(FlowLayout.LEFT, lbl("Select route:"), routeCombo, newRoute, routeInfo), BorderLayout.CENTER);
        c.add(top, BorderLayout.NORTH);

        c.add(scroll(routePanel), BorderLayout.CENTER);

        Btn add = primary("Add Station"), ins = primary("Insert After..."),
            rem = danger("Remove Station"), rev = secondary("Reverse Route"), find = secondary("Search Station");
        add.addActionListener(e -> addStation());
        ins.addActionListener(e -> insertAfter());
        rem.addActionListener(e -> removeStation());
        rev.addActionListener(e -> reverse());
        find.addActionListener(e -> search());
        c.add(row(FlowLayout.LEFT, add, ins, rem, rev, find), BorderLayout.SOUTH);
        return c;
    }

    private Card buildVehiclesView() {
        Card c = new Card(new BorderLayout(0, 12));
        JTable t = new JTable(vehicleModel);
        styleTable(t);
        Btn add = primary("+ Add Vehicle");
        add.addActionListener(e -> addVehicle());
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title("Vehicles"), BorderLayout.WEST);
        top.add(add, BorderLayout.EAST);
        c.add(top, BorderLayout.NORTH);
        c.add(scroll(t), BorderLayout.CENTER);
        return c;
    }

    private Card buildTicketsView() {
        Card c = new Card(new BorderLayout(0, 12));

        // ----- booking form -----
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 10);
        g.anchor = GridBagConstraints.WEST;
        for (JComponent f : new JComponent[]{bookVehicle, bookFrom, bookTo, bookName, bookCnic}) styleField(f);

        bookName.setPreferredSize(new Dimension(170, 34));
        bookCnic.setPreferredSize(new Dimension(160, 34));
        g.gridy = 0; g.gridx = 0; form.add(lbl("Vehicle"), g);
        g.gridx = 1; form.add(lbl("From"), g);
        g.gridx = 2; form.add(lbl("To"), g);
        g.gridx = 3; form.add(lbl("Passenger name"), g);
        g.gridx = 4; form.add(lbl("CNIC (13 digits)"), g);
        g.gridy = 1; g.gridx = 0; form.add(bookVehicle, g);
        g.gridx = 1; form.add(bookFrom, g);
        g.gridx = 2; form.add(bookTo, g);
        g.gridx = 3; form.add(bookName, g);
        g.gridx = 4; form.add(bookCnic, g);
        g.gridx = 5; g.gridy = 0; g.weightx = 1.0; form.add(Box.createHorizontalGlue(), g);

        fareLabel.setFont(fareLabel.getFont().deriveFont(Font.BOLD, 14f));
        fareLabel.setForeground(BLUE);
        Btn book = primary("Book Ticket");
        book.addActionListener(e -> bookTicket());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(fareLabel, BorderLayout.WEST);
        bottom.add(book, BorderLayout.EAST);

        JPanel formBox = new JPanel(new BorderLayout(0, 8));
        formBox.setOpaque(false);
        formBox.add(title("Book a Ticket"), BorderLayout.NORTH);
        formBox.add(form, BorderLayout.CENTER);
        formBox.add(bottom, BorderLayout.SOUTH);
        c.add(formBox, BorderLayout.NORTH);

        bookVehicle.addActionListener(e -> { if (!updating) { fillBookingStations(); updateFare(); } });
        bookFrom.addActionListener(e -> { if (!updating) updateFare(); });
        bookTo.addActionListener(e -> { if (!updating) updateFare(); });

        // ----- tickets table -----
        styleTable(ticketTable);
        JPanel mid = new JPanel(new BorderLayout(0, 8));
        mid.setOpaque(false);
        mid.add(title("Booked Tickets"), BorderLayout.NORTH);
        mid.add(scroll(ticketTable), BorderLayout.CENTER);
        c.add(mid, BorderLayout.CENTER);

        Btn cancel = danger("Cancel Selected Ticket");
        cancel.addActionListener(e -> cancelTicket());
        c.add(row(FlowLayout.LEFT, cancel), BorderLayout.SOUTH);
        return c;
    }

    // =====================================================================
    //  Helpers
    // =====================================================================
    private void setStatus(String m, boolean ok) {
        status.setText(m);
        status.setForeground(ok ? GREEN : RED);
    }

    private void error(String m) {
        setStatus(m, false);
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private Route currentRoute() {
        Object sel = routeCombo.getSelectedItem();
        return sel == null ? null : sys.findRoute(sel.toString());
    }

    private Route requireRoute() {
        Route r = currentRoute();
        if (r == null) error("Please create a route first (click \"+ New Route\").");
        return r;
    }

    private static double parseKm(String s) {
        double d = Double.parseDouble(s.trim());
        if (d < 0) throw new NumberFormatException("negative");
        return d;
    }

    private static String[] stationNames(Route r) {
        List<Station> l = r.toList();
        String[] names = new String[l.size()];
        for (int i = 0; i < names.length; i++) names[i] = l.get(i).getName();
        return names;
    }

    /** Saves to file and refreshes the whole screen. */
    private void changed() {
        FileManager.save(sys);
        refreshAll();
    }

    // =====================================================================
    //  Refresh
    // =====================================================================
    private void refreshAll() {
        updating = true;
        Object keep = forceRoute != null ? forceRoute : routeCombo.getSelectedItem();
        forceRoute = null;
        routeCombo.removeAllItems();
        for (Route r : sys.getRoutes()) routeCombo.addItem(r.getRouteName());
        if (keep != null) routeCombo.setSelectedItem(keep);
        if (routeCombo.getSelectedItem() == null && routeCombo.getItemCount() > 0) routeCombo.setSelectedIndex(0);

        vehicleModel.setRowCount(0);
        for (Vehicle v : sys.getVehicles())
            vehicleModel.addRow(new Object[]{v.getType(), v.getVehicleId(), v.getRoute().getRouteName(),
                    v.getBookedSeats(), v.getCapacity(), v.getAvailableSeats()});

        ticketModel.setRowCount(0);
        for (Ticket t : sys.getTickets())
            ticketModel.addRow(new Object[]{t.getTicketId(), t.getPassenger().getName(), t.getPassenger().getCnic(),
                    t.getVehicle().getType() + " " + t.getVehicle().getVehicleId(),
                    t.getSource(), t.getDestination(), String.format("%,.0f", t.getFare())});

        Object keepV = bookVehicle.getSelectedItem();
        bookVehicle.removeAllItems();
        for (Vehicle v : sys.getVehicles()) bookVehicle.addItem(v.getVehicleId());
        if (keepV != null) bookVehicle.setSelectedItem(keepV);
        updating = false;

        int stations = 0;
        for (Route r : sys.getRoutes()) stations += r.getSize();
        statRoutes.setText(String.valueOf(sys.getRoutes().size()));
        statStations.setText(String.valueOf(stations));
        statVehicles.setText(String.valueOf(sys.getVehicles().size()));
        statTickets.setText(String.valueOf(sys.getTickets().size()));

        fillBookingStations();
        onRouteChanged();
        updateFare();
    }

    private void onRouteChanged() {
        Route r = currentRoute();
        routePanel.setHighlight(null);
        routePanel.setRoute(r, sys.getRoutes().isEmpty());
        if (r == null) {
            routeInfo.setText(" ");
        } else if (r.isEmpty()) {
            routeInfo.setText("Stations: 0");
        } else {
            double total = 0;
            for (Station s : r.toList()) total += s.getDistanceFromPrevious();
            routeInfo.setText("Stations: " + r.getSize() + "   |   Total distance: " + total + " km");
        }
    }

    private void fillBookingStations() {
        updating = true;
        bookFrom.removeAllItems();
        bookTo.removeAllItems();
        Object sel = bookVehicle.getSelectedItem();
        Vehicle v = sel == null ? null : sys.findVehicle(sel.toString());
        if (v != null) {
            for (String n : stationNames(v.getRoute())) { bookFrom.addItem(n); bookTo.addItem(n); }
            if (bookTo.getItemCount() > 1) bookTo.setSelectedIndex(bookTo.getItemCount() - 1);
        }
        updating = false;
    }

    private void updateFare() {
        Object vs = bookVehicle.getSelectedItem(), f = bookFrom.getSelectedItem(), t = bookTo.getSelectedItem();
        if (vs == null || f == null || t == null) { fareLabel.setText("Add a vehicle with a route to book tickets"); return; }
        Vehicle v = sys.findVehicle(vs.toString());
        double km = v.getRoute().getDistance(f.toString(), t.toString());
        if (km <= 0) fareLabel.setText("Choose different From and To stations");
        else fareLabel.setText("Distance: " + km + " km   |   Fare: Rs " + String.format("%,.0f", v.calculateFare(km))
                + "   |   Seats available: " + v.getAvailableSeats());
    }

    // =====================================================================
    //  Actions
    // =====================================================================
    private void newRoute() {
        String name = JOptionPane.showInputDialog(this, "New route name:", "New Route", JOptionPane.QUESTION_MESSAGE);
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) { error("The route name cannot be empty."); return; }
        if (sys.findRoute(name) != null) { error("A route with this name already exists."); return; }
        sys.addRoute(new Route(name));
        forceRoute = name;
        changed();
        setStatus("Route \"" + name + "\" created. Now add some stations.", true);
    }

    private void addStation() {
        Route r = requireRoute();
        if (r == null) return;
        JTextField name = new JTextField(16), dist = new JTextField("0", 16);
        styleField(name); styleField(dist);
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Station name:")); p.add(name);
        if (!r.isEmpty()) { p.add(new JLabel("Distance from previous station (km):")); p.add(dist); }
        if (JOptionPane.showConfirmDialog(this, p, "Add Station", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) return;
        try {
            String n = name.getText().trim();
            if (n.isEmpty()) { error("Please enter a station name."); return; }
            if (r.search(n) != null) { error("This station is already on the route."); return; }
            double d = r.isEmpty() ? 0 : parseKm(dist.getText());
            r.addStation(new Station(sys.generateStationId(), n, d));
            changed();
            setStatus("Station \"" + n + "\" added.", true);
        } catch (NumberFormatException ex) {
            error("Please enter a valid (positive) number for the distance.");
        }
    }

    private void insertAfter() {
        Route r = requireRoute();
        if (r == null) return;
        if (r.isEmpty()) { error("The route is empty. Add a station first."); return; }
        JComboBox<String> after = new JComboBox<>(stationNames(r));
        JTextField name = new JTextField(16), dist = new JTextField(16);
        styleField(after); styleField(name); styleField(dist);
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Insert after station:")); p.add(after);
        p.add(new JLabel("New station name:")); p.add(name);
        p.add(new JLabel("Distance from that station (km):")); p.add(dist);
        if (JOptionPane.showConfirmDialog(this, p, "Insert Station", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) return;
        try {
            String n = name.getText().trim();
            if (n.isEmpty()) { error("Please enter a station name."); return; }
            if (r.search(n) != null) { error("This station is already on the route."); return; }
            double d = parseKm(dist.getText());
            r.insertAfter(after.getSelectedItem().toString(), new Station(sys.generateStationId(), n, d));
            changed();
            routePanel.setHighlight(n);
            setStatus("Station \"" + n + "\" inserted.", true);
        } catch (NumberFormatException ex) {
            error("Please enter a valid (positive) number for the distance.");
        } catch (InvalidStationException ex) {
            error(ex.getMessage());
        }
    }

    private void removeStation() {
        Route r = requireRoute();
        if (r == null) return;
        try {
            if (r.isEmpty()) r.removeStation(""); // throws EmptyRouteException
            Object sel = JOptionPane.showInputDialog(this, "Which station do you want to remove?", "Remove Station",
                    JOptionPane.QUESTION_MESSAGE, null, stationNames(r), null);
            if (sel == null) return;
            r.removeStation(sel.toString());
            changed();
            setStatus("Station \"" + sel + "\" removed.", true);
        } catch (EmptyRouteException | InvalidStationException ex) {
            error(ex.getMessage());
        }
    }

    private void reverse() {
        Route r = requireRoute();
        if (r == null) return;
        if (r.isEmpty()) { error("The route is empty."); return; }
        r.reverse();
        changed();
        setStatus("Route reversed.", true);
    }

    private void search() {
        Route r = requireRoute();
        if (r == null) return;
        String n = JOptionPane.showInputDialog(this, "Station name to search for:", "Search Station", JOptionPane.QUESTION_MESSAGE);
        if (n == null || n.trim().isEmpty()) return;
        Station s = r.search(n.trim());
        if (s == null) { routePanel.setHighlight(null); error("Station \"" + n.trim() + "\" was not found."); }
        else { routePanel.setHighlight(s.getName()); setStatus("Found: " + s, true); }
    }

    private void addVehicle() {
        if (sys.getRoutes().isEmpty()) { error("Please create a route first."); return; }
        JTextField id = new JTextField(16), cap = new JTextField(16);
        JComboBox<String> type = new JComboBox<>(new String[]{"Bus", "Train"});
        JComboBox<String> route = new JComboBox<>();
        for (Route r : sys.getRoutes()) route.addItem(r.getRouteName());
        styleField(id); styleField(cap); styleField(type); styleField(route);
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Vehicle ID:")); p.add(id);
        p.add(new JLabel("Type:")); p.add(type);
        p.add(new JLabel("Seats (capacity):")); p.add(cap);
        p.add(new JLabel("Route:")); p.add(route);
        if (JOptionPane.showConfirmDialog(this, p, "Add Vehicle", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) return;
        try {
            String vid = id.getText().trim();
            if (vid.isEmpty()) { error("Please enter a vehicle ID."); return; }
            if (sys.findVehicle(vid) != null) { error("This vehicle ID already exists."); return; }
            int c = Integer.parseInt(cap.getText().trim());
            if (c <= 0) { error("Capacity must be greater than 0."); return; }
            Route r = sys.findRoute(route.getSelectedItem().toString());
            Vehicle v = type.getSelectedIndex() == 0 ? new Bus(vid, c, r) : new Train(vid, c, r);
            sys.addVehicle(v);
            changed();
            setStatus(v.getType() + " " + vid + " added.", true);
        } catch (NumberFormatException ex) {
            error("Please enter a whole number for the capacity.");
        }
    }

    private void bookTicket() {
        Object vs = bookVehicle.getSelectedItem(), f = bookFrom.getSelectedItem(), t = bookTo.getSelectedItem();
        if (vs == null) { error("Please add a vehicle first (Vehicles tab)."); return; }
        String name = bookName.getText().trim();
        String cnic = bookCnic.getText().trim();
        if (name.isEmpty()) { error("Please enter the passenger name."); return; }
        if (!cnic.replace("-", "").matches("\\d{13}")) { error("CNIC must have 13 digits (for example 45101-1234567-1)."); return; }
        try {
            Ticket tk = sys.bookTicket(new Passenger(name, cnic), vs.toString(), String.valueOf(f), String.valueOf(t));
            bookName.setText("");
            bookCnic.setText("");
            changed();
            setStatus("Ticket #" + tk.getTicketId() + " booked.", true);
            JOptionPane.showMessageDialog(this, "Booking successful!\n\n" + tk, "Ticket", JOptionPane.INFORMATION_MESSAGE);
        } catch (InvalidStationException | VehicleFullException ex) {
            error(ex.getMessage());
        }
    }

    private void cancelTicket() {
        int row = ticketTable.getSelectedRow();
        if (row < 0) { error("Select a ticket in the table first."); return; }
        int id = (Integer) ticketModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Cancel ticket #" + id + "?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            sys.cancelTicket(id);
            changed();
            setStatus("Ticket #" + id + " cancelled.", true);
        } catch (TicketNotFoundException ex) {
            error(ex.getMessage());
        }
    }

    // =====================================================================
    //  Start
    // =====================================================================
    private static void applyFont() {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object k = keys.nextElement();
            if (UIManager.get(k) instanceof FontUIResource)
                UIManager.put(k, new FontUIResource(new Font("Segoe UI", Font.PLAIN, 14)));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
            applyFont();
            new MainGUI(FileManager.load()).setVisible(true);
        });
    }
}
