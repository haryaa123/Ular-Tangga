import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

public class GraphBoardPanel extends JPanel {
    private GameGraph graph;
    private List<Player> players; 
    private Player activePlayer = null;
    private Color activeStepColor = null;
    private List<Node> highlightedPath = null;
    private BufferedImage bgImage; 
    private static final int R = 22; // Ukuran lingkaran kotak
    
    public GraphBoardPanel(GameGraph g, List<Player> p) {
        this.graph = g; this.players = p;
        setPreferredSize(new Dimension(600, 600));
        
        // Load gambar background secara acak biar variatif
        try {
            Random rand = new Random();
            int nomorAcak = rand.nextInt(3) + 1; // Pilih bg1, bg2, atau bg3
            String path = "C:/KULIAH/ASD/FP_ASD/FP_ASD_Snaknladder_Maze/SnL/ProjectASD/Project/assets/bg" + nomorAcak + ".jpg";
            bgImage = ImageIO.read(new File(path));
        } catch (Exception e) { 
            bgImage = null; // Kalau gagal load, layar putih aja
        }
    }
    
    public void setActivePlayer(Player p, Color c) { activePlayer = p; activeStepColor = c; }
    public void clearActiveEffect() { activePlayer = null; activeStepColor = null; }
    public void setShortestPath(List<Node> p) { highlightedPath = p; }

    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Bikin gambar jadi mulus (Anti-aliasing)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 1. Gambar Background
        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(255, 255, 255, 180)); // Kasih efek transparan putih
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g2d.setColor(Color.WHITE); g2d.fillRect(0,0,getWidth(), getHeight());
        }
        
        int w=getWidth(), h=getHeight(), xStep=w/8, yStep=h/8;
        
        // 2. Petakan Angka (1-64) jadi Koordinat Zig-Zag
        // Baris genap: Kiri ke Kanan. Baris ganjil: Kanan ke Kiri.
        for(int i=1; i<=64; i++) {
            Node n = graph.getNode(i);
            int rowInv = (i-1)/8, col = (i-1)%8;
            int r = 7 - rowInv;
            int c = (rowInv%2==0) ? col : 7-col; 
            n.x = xStep/2 + c*xStep; n.y = yStep/2 + r*yStep;
        }

        // 3. Gambar Garis Penghubung antar kotak
        g2d.setStroke(new BasicStroke(2)); g2d.setColor(new Color(200,200,200));
        for(int i=1; i<64; i++) {
            Node n1=graph.getNode(i), n2=graph.getNode(i+1);
            g2d.drawLine(n1.x, n1.y, n2.x, n2.y);
        }
        
        // 4. Gambar Jalan Pintas (Garis putus-putus Ular/Tangga)
        g2d.setColor(new Color(100, 149, 237));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{9}, 0));
        for(int i=1; i<=64; i++){
            Node n = graph.getNode(i);
            if(n.jumpTo != null) {
                g2d.drawLine(n.x, n.y, n.jumpTo.x, n.jumpTo.y);
                g2d.fillOval((n.x+n.jumpTo.x)/2-5, (n.y+n.jumpTo.y)/2-5, 10, 10);
            }
        }
        
        // 5. Highlight Jalur Primas (Warna Merah kalau fitur Prima aktif)
        if (highlightedPath != null && !highlightedPath.isEmpty()) {
            g2d.setColor(Color.decode("#FF6961"));
            g2d.setStroke(new BasicStroke(3));
            Node prev = null;
            for (Node curr : highlightedPath) {
                if (prev != null) g2d.drawLine(prev.x, prev.y, curr.x, curr.y);
                prev = curr;
            }
        }

        // 6. Gambar Lingkaran Kotak & Koin
        g2d.setStroke(new BasicStroke(1));
        for(int i=1; i<=64; i++) {
            Node n = graph.getNode(i);
            g2d.setColor((i%5==0)?Color.decode("#FFB347"):Color.decode("#FDF5E6"));
            g2d.fillOval(n.x-R, n.y-R, R*2, R*2);
            g2d.setColor(Color.GRAY); g2d.drawOval(n.x-R, n.y-R, R*2, R*2);
            
            if (n.bonusScore > 0) { // Gambar koin kecil kalau ada skor
                g2d.setColor(Color.decode("#FFD700")); 
                g2d.fillOval(n.x + 8, n.y - 18, 14, 14); 
                g2d.setColor(Color.DARK_GRAY); g2d.setFont(new Font("Arial", Font.BOLD, 8));
                g2d.drawString("+" + n.bonusScore, n.x + 8, n.y - 20);
            }
            
            g2d.setColor(Color.BLACK);
            String txt = (i==1)?"START":(i==64?"FINISH":String.valueOf(i));
            g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 10));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(txt, n.x-fm.stringWidth(txt)/2, n.y+fm.getAscent()/2-2);
        }
        
        // 7. Gambar Pemain
        for(Player p : players) drawP(g2d, p);
        if(activePlayer!=null) drawP(g2d, activePlayer);
    }
    
    private void drawP(Graphics2D g, Player p) {
        Node n = graph.getNode(p.position);
        // Geser dikit posisi pemain biar gak numpuk kalau satu kotak
        int id = Integer.parseInt(p.id.substring(1));
        int dx = n.x + (int)(Math.cos(id)*8);
        int dy = n.y + (int)(Math.sin(id)*8);
        
        // Gambar bidak pemain bentuk pentagon/lingkaran
        Path2D s = new Path2D.Double();
        for(int i=0; i<10; i++) {
            double a = i*Math.PI/5 - Math.PI/2;
            double r = (i%2==0)?(R-4):(R-14);
            double px = dx + Math.cos(a)*r, py = dy + Math.sin(a)*r;
            if(i==0) s.moveTo(px,py); else s.lineTo(px,py);
        }
        s.closePath();
        g.setColor(p.color); g.fill(s);
        g.setColor(Color.BLACK); g.setFont(new Font("Arial",Font.BOLD,9));
        g.drawString(p.id, dx-6, dy+4);
    }
}