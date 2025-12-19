import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Random; 

public class GraphBoardPanel extends JPanel {
    private GameGraph graph;
    private List<Player> players; 
    private Player activePlayer = null;
    private Color activeStepColor = null;
    private List<Node> highlightedPath = null;
    private BufferedImage bgImage; 
    
    private static final int R = 22; // Radius node lingkaran
    
    // Constructor
    public GraphBoardPanel(GameGraph g, List<Player> p) {
        this.graph = g; this.players = p;
        setPreferredSize(new Dimension(600, 600));
        
        // --- LOGIC RANDOM BACKGROUND ---
        try {
            // Ubah angka ini sesuai jumlah gambar background yang kamu punya di folder assets
            // Contoh: Kalau ada bg1.jpg, bg2.jpg, bg3.jpg, bg4.jpg, bg5.jpg -> ubah jadi 5
            int totalVariasiBG = 3; 
            
            Random rand = new Random();
            int nomorAcak = rand.nextInt(totalVariasiBG) + 1; // Akan menghasilkan angka acak 1 sampai totalVariasiBG
            
            String namaFile = "assets/bg" + nomorAcak + ".jpg";
            System.out.println("GraphBoardPanel: Memilih background -> " + namaFile);
            
            bgImage = ImageIO.read(new File(namaFile));
            
        } catch (Exception e) {
            System.out.println("Gagal load background acak. Mencoba load default 'bg.jpg'...");
            // Fallback: Kalau bg nomor acak tidak ada, coba cari bg.jpg biasa
            try {
                bgImage = ImageIO.read(new File("assets/bg.jpg"));
            } catch (Exception ex) {
                System.out.println("Background default juga tidak ditemukan. Menggunakan background putih polos.");
                bgImage = null;
            }
        }
        // -------------------------------
    }
    
    public void setActivePlayer(Player p, Color c) { 
        activePlayer = p; 
        activeStepColor = c; 
    }
    
    public void clearActiveEffect() { 
        activePlayer = null; 
        activeStepColor = null; 
    }
    
    public void setShortestPath(List<Node> p) { 
        highlightedPath = p; 
    }

    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 1. Gambar Background
        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
            // Tambah lapisan putih transparan supaya garis graph tetap kelihatan jelas
            g2d.setColor(new Color(255, 255, 255, 180)); 
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } else {
            // Kalau tidak ada gambar, pakai warna putih polos
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0,0,getWidth(), getHeight());
        }
        
        // 2. Hitung Koordinat Node
        int w=getWidth(), h=getHeight(), xStep=w/8, yStep=h/8;
        for(int i=1; i<=64; i++) {
            Node n = graph.getNode(i);
            int rowInv = (i-1)/8, col = (i-1)%8;
            int r = 7 - rowInv;
            int c = (rowInv%2==0) ? col : 7-col;
            n.x = xStep/2 + c*xStep; n.y = yStep/2 + r*yStep;
        }

        // 3. Gambar Garis Hubung (Next/Prev)
        g2d.setStroke(new BasicStroke(2)); g2d.setColor(new Color(200,200,200));
        for(int i=1; i<64; i++) {
            Node n1=graph.getNode(i), n2=graph.getNode(i+1);
            g2d.drawLine(n1.x, n1.y, n2.x, n2.y);
        }
        
        // 4. Gambar Garis Shortcut (Tangga)
        g2d.setColor(new Color(100, 149, 237)); // Warna Biru Langit
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{9}, 0)); // Garis putus-putus
        for(int i=1; i<=64; i++){
            Node n = graph.getNode(i);
            if(n.jumpTo != null) {
                g2d.drawLine(n.x, n.y, n.jumpTo.x, n.jumpTo.y);
                // Titik penanda di tengah garis
                g2d.fillOval((n.x+n.jumpTo.x)/2-5, (n.y+n.jumpTo.y)/2-5, 10, 10);
            }
        }
        
        // 5. Gambar Highlight Path (Kalau Prima)
        if (highlightedPath != null && !highlightedPath.isEmpty()) {
            g2d.setColor(Color.decode("#FF6961")); // Warna Merah Pastel
            g2d.setStroke(new BasicStroke(3));
            Node prev = null;
            for (Node curr : highlightedPath) {
                if (prev != null) g2d.drawLine(prev.x, prev.y, curr.x, curr.y);
                prev = curr;
            }
        }

        // 6. Gambar Lingkaran Node & Angka
        g2d.setStroke(new BasicStroke(1));
        for(int i=1; i<=64; i++) {
            Node n = graph.getNode(i);
            
            // Warna node selang-seling (Kuning / Putih Tulang)
            g2d.setColor((i%5==0)?Color.decode("#FFB347"):Color.decode("#FDF5E6"));
            g2d.fillOval(n.x-R, n.y-R, R*2, R*2);
            
            // Garis pinggir node
            g2d.setColor(Color.GRAY);
            g2d.drawOval(n.x-R, n.y-R, R*2, R*2);
            
            // Tulis Angka
            g2d.setColor(Color.BLACK);
            String txt = (i==1)?"START":(i==64?"FINISH":String.valueOf(i));
            g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 10));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(txt, n.x-fm.stringWidth(txt)/2, n.y+fm.getAscent()/2-2);
        }
        
        // 7. Gambar Pemain
        for(Player p : players) drawP(g2d, p);
        
        // 8. Gambar Highlight Pemain yang Sedang Jalan
        if(activePlayer!=null) drawP(g2d, activePlayer);
    }
    
    // Method Helper menggambar Player (Bentuk Bintang)
    private void drawP(Graphics2D g, Player p) {
        Node n = graph.getNode(p.position);
        int id = Integer.parseInt(p.id.substring(1));
        
        // Offset sedikit biar kalau numpuk kelihatan
        int dx = n.x + (int)(Math.cos(id)*8);
        int dy = n.y + (int)(Math.sin(id)*8);
        
        // Lingkaran highlight kalau sedang aktif jalan
        if(p==activePlayer && activeStepColor!=null) {
            g.setColor(activeStepColor); g.setStroke(new BasicStroke(2));
            g.drawOval(dx-R, dy-R, R*2, R*2);
        }
        
        // Gambar Bintang
        Path2D s = new Path2D.Double();
        for(int i=0; i<10; i++) {
            double a = i*Math.PI/5 - Math.PI/2;
            double r = (i%2==0)?(R-4):(R-14); // R-4 radius luar, R-14 radius dalam
            double px = dx + Math.cos(a)*r, py = dy + Math.sin(a)*r;
            if(i==0) s.moveTo(px,py); else s.lineTo(px,py);
        }
        s.closePath();
        
        g.setColor(p.color); g.fill(s);
        g.setColor(Color.DARK_GRAY); g.setStroke(new BasicStroke(1)); g.draw(s);
        
        // Tulis ID Player (P1, P2, dst)
        g.setColor(Color.BLACK); g.setFont(new Font("Arial",Font.BOLD,9));
        g.drawString(p.id, dx-6, dy+4);
    }
}