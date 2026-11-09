package rms;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/** Draws the linked list as boxes joined by double arrows (snake layout). */
public class RoutePanel extends JPanel implements Scrollable {
    private static final long serialVersionUID = 1L;

    private Route route;
    private String highlight;
    private boolean noRoutes;

    private static final int BW = 128, BH = 58, GAP_X = 90, GAP_Y = 84, M = 40;

    public RoutePanel() { setBackground(Color.WHITE); }

    /** noRoutes = true when the system has no route at all (different hint text). */
    public void setRoute(Route r, boolean noRoutes) {
        this.route = r;
        this.noRoutes = noRoutes;
        revalidate();
        repaint();
    }

    public void setHighlight(String name) { this.highlight = name; repaint(); }

    private static String km(double d) {
        return d == Math.rint(d) ? String.valueOf((int) d) : String.valueOf(d);
    }

    private void drawCentered(Graphics2D g, String text, int y, Font f, Color c) {
        g.setFont(f);
        g.setColor(c);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, y);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (route == null || route.isEmpty()) {
            int cy = Math.max(getHeight() / 2, 90);
            Font big = getFont().deriveFont(Font.BOLD, 20f), small = getFont().deriveFont(Font.PLAIN, 14f);
            Color muted = new Color(100, 116, 139);
            if (route == null || noRoutes) {
                drawCentered(g, "No routes yet", cy - 10, big, new Color(51, 65, 85));
                drawCentered(g, "Click \"New Route\" above to create one, or \"Load Sample Data\" below to try a demo.",
                        cy + 22, small, muted);
            } else {
                drawCentered(g, "This route has no stations", cy - 10, big, new Color(51, 65, 85));
                drawCentered(g, "Click \"Add Station\" below to add the first station (the HEAD of the list).",
                        cy + 22, small, muted);
            }
            g.dispose();
            return;
        }

        List<Station> list = route.toList();
        int n = list.size();
        int perRow = Math.max(1, (Math.max(getWidth(), 300) - 2 * M + GAP_X) / (BW + GAP_X));
        int rows = (n + perRow - 1) / perRow;
        int neededH = 2 * M + rows * BH + (rows - 1) * GAP_Y + 10;
        if (getPreferredSize().height != neededH) {
            setPreferredSize(new Dimension(10, neededH));
            SwingUtilities.invokeLater(this::revalidate);
        }

        int[] x = new int[n], y = new int[n];
        for (int i = 0; i < n; i++) {
            int row = i / perRow, col = i % perRow;
            if (row % 2 == 1) col = perRow - 1 - col; // snake layout
            x[i] = M + col * (BW + GAP_X);
            y[i] = M + row * (BH + GAP_Y);
        }

        // Connectors (double arrow = prev + next pointers)
        Font small = getFont().deriveFont(Font.BOLD, 12f);
        for (int i = 0; i + 1 < n; i++) {
            int x1, y1, x2, y2;
            boolean vertical = (i / perRow) != ((i + 1) / perRow);
            if (vertical) {
                x1 = x[i] + BW / 2; y1 = y[i] + BH;
                x2 = x[i + 1] + BW / 2; y2 = y[i + 1];
            } else if (x[i + 1] > x[i]) {
                x1 = x[i] + BW; y1 = y[i] + BH / 2; x2 = x[i + 1]; y2 = y1;
            } else {
                x1 = x[i]; y1 = y[i] + BH / 2; x2 = x[i + 1] + BW; y2 = y1;
            }
            g.setColor(new Color(100, 116, 139));
            g.setStroke(new BasicStroke(2f));
            g.drawLine(x1, y1, x2, y2);
            arrowHead(g, x1, y1, x2, y2);
            arrowHead(g, x2, y2, x1, y1);

            String label = km(list.get(i + 1).getDistanceFromPrevious()) + " km";
            g.setFont(small);
            FontMetrics fm = g.getFontMetrics();
            int lw = fm.stringWidth(label);
            int lx = vertical ? x1 + 10 : (x1 + x2) / 2 - lw / 2;
            int ly = vertical ? (y1 + y2) / 2 + 4 : y1 - 9;
            g.setColor(new Color(194, 65, 12));
            g.drawString(label, lx, ly);
        }

        // Boxes
        for (int i = 0; i < n; i++) {
            Station s = list.get(i);
            boolean isHighlight = highlight != null && s.getName().equalsIgnoreCase(highlight);
            Color fill = new Color(224, 238, 255), border = new Color(37, 99, 235);
            if (i == 0) { fill = new Color(220, 245, 226); border = new Color(22, 163, 74); }
            if (i == n - 1 && n > 1) { fill = new Color(255, 237, 213); border = new Color(234, 88, 12); }
            if (isHighlight) { fill = new Color(254, 243, 160); border = new Color(202, 138, 4); }

            // soft shadow
            g.setColor(new Color(15, 23, 42, 22));
            g.fill(new RoundRectangle2D.Double(x[i] + 2, y[i] + 4, BW, BH, 16, 16));

            RoundRectangle2D box = new RoundRectangle2D.Double(x[i], y[i], BW, BH, 16, 16);
            g.setColor(fill);
            g.fill(box);
            g.setColor(border);
            g.setStroke(new BasicStroke(isHighlight ? 3f : 2f));
            g.draw(box);

            g.setColor(new Color(15, 23, 42));
            g.setFont(getFont().deriveFont(Font.BOLD, 15f));
            FontMetrics fm = g.getFontMetrics();
            String name = fit(s.getName(), fm, BW - 14);
            g.drawString(name, x[i] + (BW - fm.stringWidth(name)) / 2, y[i] + 26);
            g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            fm = g.getFontMetrics();
            String id = "ID " + s.getId();
            g.setColor(new Color(71, 85, 105));
            g.drawString(id, x[i] + (BW - fm.stringWidth(id)) / 2, y[i] + 44);

            String tag = i == 0 ? "HEAD" : (i == n - 1 ? "TAIL" : null);
            if (n == 1) tag = "HEAD / TAIL";
            if (tag != null) {
                g.setFont(getFont().deriveFont(Font.BOLD, 12f));
                g.setColor(border);
                g.drawString(tag, x[i] + 4, y[i] - 8);
            }
        }
        g.dispose();
    }

    private static String fit(String s, FontMetrics fm, int w) {
        if (fm.stringWidth(s) <= w) return s;
        while (s.length() > 1 && fm.stringWidth(s + "...") > w) s = s.substring(0, s.length() - 1);
        return s + "...";
    }

    /** Draws an arrowhead at (tx, ty) pointing away from (fx, fy). */
    private static void arrowHead(Graphics2D g, int fx, int fy, int tx, int ty) {
        double ang = Math.atan2(ty - fy, tx - fx), len = 11;
        int[] px = {tx, (int) (tx - len * Math.cos(ang - 0.4)), (int) (tx - len * Math.cos(ang + 0.4))};
        int[] py = {ty, (int) (ty - len * Math.sin(ang - 0.4)), (int) (ty - len * Math.sin(ang + 0.4))};
        g.fillPolygon(px, py, 3);
    }

    // Scrollable: width follows the viewport, height follows the content
    @Override public Dimension getPreferredScrollableViewportSize() { return new Dimension(700, 300); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 100; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    // Empty state: fill the whole viewport so the hint text is visible
    @Override public boolean getScrollableTracksViewportHeight() { return route == null || route.isEmpty(); }
}
