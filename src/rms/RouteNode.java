package rms;

import java.io.Serializable;

/** Node of the doubly linked list: holds a Station plus next/prev pointers. */
public class RouteNode implements Serializable {
    private static final long serialVersionUID = 1L;

    private Station data;
    private RouteNode next;
    private RouteNode prev;

    public RouteNode(Station data) { this.data = data; }

    public Station getData() { return data; }
    public RouteNode getNext() { return next; }
    public void setNext(RouteNode next) { this.next = next; }
    public RouteNode getPrev() { return prev; }
    public void setPrev(RouteNode prev) { this.prev = prev; }
}
