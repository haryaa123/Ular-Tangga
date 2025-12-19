import java.awt.Color;

public class Player {
    String id;
    String name;
    int position;
    Color color;
    int score; // REVISI: Tambah skor

    public Player(String id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.position = 1;
        this.score = 0;
    }
    
    @Override
    public String toString() {
        return name + " (Score: " + score + ")";
    }
}