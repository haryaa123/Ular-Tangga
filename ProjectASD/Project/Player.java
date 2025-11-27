import java.awt.Color;

public class Player {
    String id;      // P1, P2, dst
    String name;    // Nama inputan user
    int position;
    Color color;

    public Player(String id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.position = 1; // Semua mulai dari start
    }
    
    @Override
    public String toString() {
        return id + ": " + name;
    }
}