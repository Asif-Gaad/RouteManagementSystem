package rms;

import java.io.Serializable;

public class Passenger implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String cnic;

    public Passenger(String name, String cnic) {
        this.name = name;
        this.cnic = cnic;
    }

    public String getName() { return name; }
    public String getCnic() { return cnic; }

    @Override
    public String toString() { return name + " (CNIC: " + cnic + ")"; }
}
