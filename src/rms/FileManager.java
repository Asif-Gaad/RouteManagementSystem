package rms;

import java.io.*;

/** Saves and loads the whole system to/from data.ser using Java serialization. */
public class FileManager {
    private static final String FILE_NAME = "data.ser";

    public static void save(TransportSystem system) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(system);
            System.out.println("Data saved (" + FILE_NAME + ").");
        } catch (IOException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    public static TransportSystem load() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return new TransportSystem();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            TransportSystem s = (TransportSystem) in.readObject();
            System.out.println("Previous data loaded.");
            return s;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Load error (" + e.getMessage() + "). Starting with an empty system.");
            return new TransportSystem();
        }
    }
}
