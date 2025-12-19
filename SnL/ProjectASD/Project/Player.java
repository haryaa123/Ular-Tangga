import java.awt.Color;

public class Player {
    String id;
    String name;
    int position;   // Posisi kotak sekarang
    Color color;    // Warna bidak
    int score;      // Total skor

    public Player(String id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.position = 1;
        this.score = 0;
    }
    
    // Fitur copy pemain buat disimpan di Hall of Fame (Leaderboard)
    public Player(Player p) {
        this.id = p.id;
        this.name = p.name;
        this.color = p.color;
        this.position = p.position;
        this.score = p.score;
    }
    
    @Override
    public String toString() {
        return name + " (" + score + ")";
    }
}