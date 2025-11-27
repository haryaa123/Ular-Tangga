import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TreeGUI extends JFrame {
    private Tree tree;
    private JTextArea outputArea;
    private TreeVisualizerPanel treePanel;
    
    public TreeGUI(Tree tree) {
        this.tree = tree;
        
        setTitle("City Route Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel visualisasi
        treePanel = new TreeVisualizerPanel(tree.root);
        add(treePanel, BorderLayout.CENTER);
        
        // Panel bawah
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        outputArea = new JTextArea(3, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel Tombol
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        // Tombol Utama
        JButton routeBtn = new JButton("Find Route (Surabaya -> Bali)");
        routeBtn.setBackground(new Color(255, 100, 100)); // Warna agak merah biar menonjol
        routeBtn.setForeground(Color.WHITE);
        
        JButton resetBtn = new JButton("Reset View");
        
        routeBtn.addActionListener(e -> findRouteAndAnimate());
        resetBtn.addActionListener(e -> resetView());
        
        buttonPanel.add(routeBtn);
        buttonPanel.add(resetBtn);
        
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void findRouteAndAnimate() {
        String target = "Bali";
        List<Node> path = tree.findPath(target);
        
        if (!path.isEmpty()) {
            // Tampilkan text rute
            StringBuilder sb = new StringBuilder();
            sb.append("Rute Ditemukan: ");
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i).data);
                if (i < path.size() - 1) sb.append(" -> ");
            }
            outputArea.setText(sb.toString());
            
            // Kirim path ke panel untuk digambar
            treePanel.setHighlightedPath(path);
        } else {
            outputArea.setText("Kota " + target + " tidak ditemukan!");
        }
    }
    
    private void resetView() {
        outputArea.setText("");
        treePanel.clearHighlight();
        treePanel.recalculatePositions();
    }
}