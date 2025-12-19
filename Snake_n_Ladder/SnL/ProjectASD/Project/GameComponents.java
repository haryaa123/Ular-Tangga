import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.io.File;
import java.util.List;
import java.util.Random;

// 1. PANEL PAPAN UTAMA
class GraphBoardPanel extends JPanel {
    private GameGraph graph;
    private List<Player> players;
    private Player activePlayer = null;
    private Color activeStepColor = null;
    private Image bgImage;

    private static final int R = 24; // Radius Node

    public GraphBoardPanel(GameGraph g, List<Player> p) {
        this.graph = g;
        this.players = p;
        setPreferredSize(new Dimension(800, 600)); // Lebih lebar biar path jelas
        
        try {
            bgImage = ImageIO.read(new File("assets/bg.jpg")); // Pastikan ada file ini atau hapus try-catch
        } catch (Exception e) { bgImage = null; }
    }

    public void setActivePlayer(Player p, Color c) { activePlayer = p; activeStepColor = c; }
    public void clearActiveEffect() { activePlayer = null; activeStepColor = null; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        if(bgImage != null) g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        else { g2d.setColor(Color.decode("#F0F8FF")); g2d.fillRect(0,0,getWidth(), getHeight()); }

        // Kalkulasi Posisi Node (Zig-Zag Path Lebar)
        int w = getWidth(), h = getHeight();
        int rows = 8, cols = 8;
        int xStep = w / cols, yStep = h / rows;

        for (int i = 1; i <= graph.size; i++) {
            Node n = graph.getNode(i);
            int r = (rows - 1) - (i - 1) / cols;
            int c = ((rows - 1 - r) % 2 == 0) ? (i - 1) % cols : (cols - 1) - ((i - 1) % cols);
            n.x = xStep / 2 + c * xStep;
            n.y = yStep / 2 + r * yStep;
        }

        // Gambar Koneksi (Jalan)
        g2d.setStroke(new BasicStroke(4));
        g2d.setColor(new Color(200, 200, 200));
        for (int i = 1; i < graph.size; i++) {
            Node n1 = graph.getNode(i), n2 = graph.getNode(i + 1);
            g2d.drawLine(n1.x, n1.y, n2.x, n2.y);
        }
        
        // Gambar Shortcut (Garis Putus-Putus Biru)
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{9}, 0));
        for(int i=1; i<=graph.size; i++){
             if(graph.getNode(i).jumpTo != null) {
                 Node start = graph.getNode(i);
                 Node end = start.jumpTo;
                 g2d.drawLine(start.x, start.y, end.x, end.y);
             }
        }

        // Gambar Nodes
        g2d.setStroke(new BasicStroke(2));
        for (int i = 1; i <= graph.size; i++) {
            Node n = graph.getNode(i);
            
            // Warna dasar node
            if (i % 5 == 0) g2d.setColor(Color.decode("#FFD700")); // STAR NODE (Kuning Emas)
            else g2d.setColor(Color.WHITE);
            
            g2d.fillOval(n.x - R, n.y - R, R * 2, R * 2);
            g2d.setColor(Color.GRAY);
            g2d.drawOval(n.x - R, n.y - R, R * 2, R * 2);

            // Tanda STAR (Bintang) untuk kelipatan 5
            if (i % 5 == 0) {
                g2d.setColor(Color.RED);
                g2d.drawString("★", n.x - 4, n.y + 4);
            }
            
            // Tanda SCORE (Coin)
            if (n.bonusScore > 0) {
                g2d.setColor(Color.GREEN);
                g2d.drawString("+$", n.x - 8, n.y - R - 2);
            }

            // Nomor Node
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString(String.valueOf(i), n.x - 5, n.y + 5);
        }

        // Gambar Pemain
        for (Player p : players) drawPlayer(g2d, p);
        if (activePlayer != null) drawPlayer(g2d, activePlayer);
    }

    private void drawPlayer(Graphics2D g, Player p) {
        Node n = graph.getNode(p.position);
        int offset = players.indexOf(p) * 5; // Sedikit geser biar ga numpuk total
        
        g.setColor(p.color);
        g.fillOval(n.x - 10 + offset, n.y - 10 + offset, 20, 20);
        
        // Highlight active step color (Green/Red halo)
        if(p == activePlayer && activeStepColor != null) {
            g.setColor(activeStepColor);
            g.setStroke(new BasicStroke(2));
            g.drawOval(n.x - 12 + offset, n.y - 12 + offset, 24, 24);
        }
        
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawOval(n.x - 10 + offset, n.y - 10 + offset, 20, 20);
        g.drawString(p.name, n.x - 10 + offset, n.y - 15 + offset);
    }
}

// 2. DADU VISUALIZER
class DiceVisualizer extends JPanel {
    private int value = 1;
    private Color color = Color.WHITE;
    
    public DiceVisualizer() {
        setPreferredSize(new Dimension(80, 80));
        setOpaque(false);
    }
    
    public void animateRoll(int finalVal, boolean isGreen, Runnable onFinish) {
        Timer t = new Timer(50, null);
        final int[] count = {0};
        t.addActionListener(e -> {
            value = new Random().nextInt(6) + 1;
            color = (count[0] % 2 == 0) ? Color.WHITE : Color.LIGHT_GRAY;
            repaint();
            count[0]++;
            if(count[0] > 15) {
                t.stop();
                value = finalVal;
                // FITUR: Warna Dadu (Green 80%, Red 20%)
                color = isGreen ? Color.decode("#77DD77") : Color.decode("#FF6961");
                repaint();
                onFinish.run();
            }
        });
        t.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(color);
        g.fillRoundRect(5, 5, 70, 70, 20, 20);
        g.setColor(Color.BLACK);
        g.drawRoundRect(5, 5, 70, 70, 20, 20);
        g.drawString(String.valueOf(value), 35, 45); // Simple number render
    }
}