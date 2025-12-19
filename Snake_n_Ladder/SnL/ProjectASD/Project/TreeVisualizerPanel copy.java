import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class TreeVisualizerPanel extends JPanel {
    private Node root;
    private Map<Node, Point> nodePositions;
    private Node draggedNode = null;
    private Point dragOffset = new Point();
    
    // List untuk menyimpan rute yang harus diwarnai
    private List<Node> highlightedPath = new ArrayList<>();
    
    private static final int NODE_RADIUS = 35; // Diperbesar dikit buat nama kota
    private static final int VERTICAL_GAP = 80;
    
    public TreeVisualizerPanel(Node root) {
        this.root = root;
        this.nodePositions = new HashMap<>();
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        
        calculateInitialPositions();
        
        // Mouse Listeners (Drag and Drop Logic)
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                draggedNode = findNodeAt(e.getPoint());
                if (draggedNode != null) {
                    Point nodePos = nodePositions.get(draggedNode);
                    dragOffset.x = e.getX() - nodePos.x;
                    dragOffset.y = e.getY() - nodePos.y;
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedNode != null) {
                    Point newPos = new Point(e.getX() - dragOffset.x, e.getY() - dragOffset.y);
                    nodePositions.put(draggedNode, newPos);
                    repaint();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                draggedNode = null;
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }
    
    // --- Method Baru untuk Set Path ---
    public void setHighlightedPath(List<Node> path) {
        this.highlightedPath = path;
        repaint();
    }
    
    public void clearHighlight() {
        this.highlightedPath.clear();
        repaint();
    }
    // ----------------------------------

    private void calculateInitialPositions() {
        if (root != null) {
            int width = getWidth() > 0 ? getWidth() : 800;
            calculatePositions(root, width / 2, 50, width / 4);
        }
    }
    
    public void recalculatePositions() {
        nodePositions.clear();
        calculateInitialPositions();
        repaint();
    }
    
    private void calculatePositions(Node node, int x, int y, int horizontalSpacing) {
        if (node == null) return;
        nodePositions.put(node, new Point(x, y));
        if (node.left != null) calculatePositions(node.left, x - horizontalSpacing, y + VERTICAL_GAP, horizontalSpacing / 2);
        if (node.right != null) calculatePositions(node.right, x + horizontalSpacing, y + VERTICAL_GAP, horizontalSpacing / 2);
    }
    
    private Node findNodeAt(Point p) {
        for (Map.Entry<Node, Point> entry : nodePositions.entrySet()) {
            Point nodePos = entry.getValue();
            if (p.distance(nodePos) <= NODE_RADIUS) return entry.getKey();
        }
        return null;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (root != null) {
            drawTree(g2d, root);
        }
    }
    
    private void drawTree(Graphics2D g2d, Node node) {
        if (node == null) return;
        Point nodePos = nodePositions.get(node);
        
        // 1. Gambar Garis ke Child
        if (node.left != null) drawConnection(g2d, node, node.left);
        if (node.right != null) drawConnection(g2d, node, node.right);
        
        // 2. Rekursif gambar child
        drawTree(g2d, node.left);
        drawTree(g2d, node.right);
        
        // 3. Gambar Node (Lingkaran)
        boolean isInPath = highlightedPath.contains(node);
        
        // Jika bagian dari rute, warna oranye/kuning, jika bukan biru standar
        if (isInPath) {
            g2d.setColor(new Color(255, 165, 0)); // Oranye
        } else {
            g2d.setColor(new Color(100, 149, 237)); // Biru Cornflower
        }
        
        g2d.fillOval(nodePos.x - NODE_RADIUS, nodePos.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        
        // Border Node
        if (isInPath) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
        } else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
        }
        g2d.drawOval(nodePos.x - NODE_RADIUS, nodePos.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        
        // Teks Nama Kota
        g2d.setColor(isInPath ? Color.BLACK : Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(node.data);
        g2d.drawString(node.data, nodePos.x - textWidth / 2, nodePos.y + fm.getAscent() / 2 - 2);
    }
    
    private void drawConnection(Graphics2D g2d, Node parent, Node child) {
        Point p1 = nodePositions.get(parent);
        Point p2 = nodePositions.get(child);
        
        // Logika pewarnaan garis
        // Garis diwarnai MERAH jika Parent ada di path DAN Child ada di path
        // (Artinya rute melewati hubungan ini)
        boolean isPathLine = highlightedPath.contains(parent) && highlightedPath.contains(child);
        
        if (isPathLine) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(4)); // Garis tebal banget
        } else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1)); // Garis tipis biasa
        }
        
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
}