import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Menjalankan tampilan game di thread yang aman biar gak nge-lag
        SwingUtilities.invokeLater(() -> {
            new SnakesLaddersGUI();
        });
    }
}