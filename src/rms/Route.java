package rms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom DOUBLY LINKED LIST that represents a route of stations.
 * Java's built-in LinkedList is NOT used: everything is implemented manually.
 */
public class Route implements Serializable {
    private static final long serialVersionUID = 1L;

    private String routeName;
    private RouteNode head;
    private RouteNode tail;
    private int size;

    public Route(String routeName) { this.routeName = routeName; }

    public String getRouteName() { return routeName; }
    public int getSize() { return size; }
    public boolean isEmpty() { return head == null; }

    /** Adds a station at the end of the route. */
    public void addStation(Station s) {
        RouteNode node = new RouteNode(s);
        if (head == null) {
            head = tail = node;
            s.setDistanceFromPrevious(0);
        } else {
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
        }
        size++;
    }

    /** Inserts a new station right after an existing one. */
    public void insertAfter(String existingName, Station s) throws InvalidStationException {
        RouteNode cur = findNode(existingName);
        if (cur == null)
            throw new InvalidStationException("Station '" + existingName + "' was not found in the route.");
        RouteNode node = new RouteNode(s);
        RouteNode nxt = cur.getNext();
        node.setPrev(cur);
        node.setNext(nxt);
        cur.setNext(node);
        if (nxt != null) {
            nxt.setPrev(node);
            // The next station is now measured from the newly inserted one
            double remaining = nxt.getData().getDistanceFromPrevious() - s.getDistanceFromPrevious();
            nxt.getData().setDistanceFromPrevious(Math.max(remaining, 0));
        } else {
            tail = node;
        }
        size++;
    }

    /** Removes a station from the route. */
    public void removeStation(String name) throws EmptyRouteException, InvalidStationException {
        if (head == null)
            throw new EmptyRouteException("The route is empty. There is nothing to remove.");
        RouteNode cur = findNode(name);
        if (cur == null)
            throw new InvalidStationException("Station '" + name + "' was not found in the route.");

        RouteNode p = cur.getPrev();
        RouteNode n = cur.getNext();
        if (p != null) p.setNext(n); else head = n;
        if (n != null) {
            n.setPrev(p);
            if (p != null) // merge the distances
                n.getData().setDistanceFromPrevious(
                    n.getData().getDistanceFromPrevious() + cur.getData().getDistanceFromPrevious());
            else
                n.getData().setDistanceFromPrevious(0); // new head
        } else {
            tail = p;
        }
        size--;
    }

    /** Finds a station by name (case-insensitive). Returns null if not found. */
    public Station search(String name) {
        RouteNode n = findNode(name);
        return n == null ? null : n.getData();
    }

    private RouteNode findNode(String name) {
        RouteNode cur = head;
        while (cur != null) {
            if (cur.getData().getName().equalsIgnoreCase(name)) return cur;
            cur = cur.getNext();
        }
        return null;
    }

    /** Reverses the whole route by swapping the pointers of every node. */
    public void reverse() {
        if (size < 2) return;
        // Save distances first
        List<Station> before = toList();
        double[] d = new double[before.size()];
        for (int i = 0; i < d.length; i++) d[i] = before.get(i).getDistanceFromPrevious();

        RouteNode cur = head;
        while (cur != null) {
            RouteNode tmp = cur.getNext();
            cur.setNext(cur.getPrev());
            cur.setPrev(tmp);
            cur = tmp;
        }
        RouteNode t = head; head = tail; tail = t;

        // Re-assign distances for the new order
        List<Station> after = toList();
        int n = after.size();
        after.get(0).setDistanceFromPrevious(0);
        for (int j = 1; j < n; j++)
            after.get(j).setDistanceFromPrevious(d[n - j]);
    }

    /** Distance in km between two stations, or -1 if either is not on the route. */
    public double getDistance(String from, String to) {
        double cum = 0, a = -1, b = -1;
        RouteNode cur = head;
        while (cur != null) {
            cum += cur.getData().getDistanceFromPrevious();
            if (cur.getData().getName().equalsIgnoreCase(from)) a = cum;
            if (cur.getData().getName().equalsIgnoreCase(to)) b = cum;
            cur = cur.getNext();
        }
        if (a < 0 || b < 0) return -1;
        return Math.abs(b - a);
    }

    public List<Station> toList() {
        List<Station> list = new ArrayList<>();
        for (RouteNode c = head; c != null; c = c.getNext()) list.add(c.getData());
        return list;
    }

    public String display() {
        if (head == null) return "[Route '" + routeName + "' is empty]";
        StringBuilder sb = new StringBuilder("Route: " + routeName + "\n  ");
        for (RouteNode c = head; c != null; c = c.getNext()) {
            sb.append(c.getData().getName());
            if (c.getNext() != null)
                sb.append(" --(").append(c.getNext().getData().getDistanceFromPrevious()).append(" km)--> ");
        }
        return sb.toString();
    }

    @Override
    public String toString() { return routeName + " (" + size + " stations)"; }
}
