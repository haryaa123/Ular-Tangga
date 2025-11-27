import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class SnakesLaddersGUI extends JFrame {
    private GameGraph graph;
    private GraphBoardPanel boardPanel; 
    private LinkedList<Player> playerQueue; 
    
    // UI Components
    private JLabel turnLabel; 
    private JLabel instructionLabel;
    private JButton rollButton;
    private JPanel queueContainer; 
    private DiceVisualizer diceVisualizer; 
    
    private boolean isAnimating = false; 
    
    // --- PALET WARNA ---
    private final Color COLOR_BG_PANEL   = Color.decode("#FDFCF5"); 
    private final Color COLOR_GREEN_STEP = Color.decode("#77DD77"); 
    private final Color COLOR_RED_STEP   = Color.decode("#FF6961"); 
    private final Color COLOR_BTN_TEXT   = Color.decode("#555555"); 
    private final Color COLOR_DICE_THEME = Color.decode("#A3B087"); 
    
    private final Color[] PLAYER_COLORS = {
        Color.decode("#FDFD96"), Color.decode("#C3B1E1"), 
        Color.decode("#FFB7B2"), Color.decode("#AEC6CF"), 
        Color.decode("#B5EAD7"), Color.decode("#FFDAC1")  
    };

    public SnakesLaddersGUI() {
        graph = new GameGraph(64);
        playerQueue = new LinkedList<>();
        setupPlayers(); 
        
        setTitle("Cute Graph Snakes & Ladders");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        boardPanel = new GraphBoardPanel(graph, playerQueue);
        add(boardPanel, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        controlPanel.setBackground(COLOR_BG_PANEL);
        controlPanel.setPreferredSize(new Dimension(280, 0)); 
        
        turnLabel = new JLabel("Player's Turn");
        turnLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
        turnLabel.setForeground(COLOR_BTN_TEXT);
        turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        diceVisualizer = new DiceVisualizer();
        JPanel diceWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        diceWrapper.setBackground(COLOR_BG_PANEL);
        diceWrapper.add(diceVisualizer);
        diceWrapper.setMaximumSize(new Dimension(300, 100));
        
        instructionLabel = new JLabel("Let's Roll!");
        instructionLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        instructionLabel.setForeground(Color.GRAY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        rollButton = new RoundedButton("ROLL DICE");
        rollButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        rollButton.setFont(new Font("Verdana", Font.BOLD, 14));
        rollButton.setForeground(Color.WHITE);
        rollButton.setBackground(COLOR_DICE_THEME); 
        rollButton.setFocusPainted(false);
        rollButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel qTitle = new JLabel("Waiting List:");
        qTitle.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        qTitle.setForeground(COLOR_BTN_TEXT);
        qTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        queueContainer = new JPanel();
        queueContainer.setLayout(new BoxLayout(queueContainer, BoxLayout.Y_AXIS));
        queueContainer.setBackground(COLOR_BG_PANEL);
        
        JScrollPane scrollQueue = new JScrollPane(queueContainer);
        scrollQueue.setBorder(null);
        scrollQueue.setBackground(COLOR_BG_PANEL);
        scrollQueue.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        rollButton.addActionListener(e -> {
            if (!isAnimating) rollDice();
        });
        
        controlPanel.add(turnLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        controlPanel.add(diceWrapper);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        controlPanel.add(instructionLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        controlPanel.add(rollButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        controlPanel.add(qTitle);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(scrollQueue);
        
        add(controlPanel, BorderLayout.EAST);
        updateUIState();
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void setupPlayers() {
        String input = JOptionPane.showInputDialog(this, "How many players?", "2");
        int count = 2;
        try { count = Integer.parseInt(input); } catch (Exception e) {}
        if(count < 1) count = 1; if(count > 6) count = 6;
        
        for (int i = 0; i < count; i++) {
            String name = JOptionPane.showInputDialog(this, "Name P" + (i+1) + ":", "Player " + (i+1));
            if (name == null || name.trim().isEmpty()) name = "Player " + (i+1);
            playerQueue.add(new Player("P" + (i+1), name, PLAYER_COLORS[i % PLAYER_COLORS.length])); 
        }
    }

    private void updateUIState() {
        queueContainer.removeAll();
        for (Player p : playerQueue) {
            queueContainer.add(new PlayerCard(p));
            queueContainer.add(Box.createRigidArea(new Dimension(0, 8))); 
        }
        queueContainer.revalidate();
        queueContainer.repaint();

        if (!playerQueue.isEmpty()) {
            Player nextP = playerQueue.peek();
            turnLabel.setText(nextP.name + "'s Turn");
        }
    }

    private void rollDice() {
        if (playerQueue.isEmpty()) return;
        isAnimating = true;
        rollButton.setEnabled(false);
        boardPanel.setShortestPath(null); 
        
        Player currentPlayer = playerQueue.poll(); 
        updateUIState(); 

        Random rand = new Random();
        int diceValue = rand.nextInt(6) + 1;
        boolean isGreen = rand.nextDouble() < 0.8; 
        
        List<Node> specialPath = null;
        if (isPrime(currentPlayer.position)) {
            specialPath = graph.getShortestPath(currentPlayer.position);
            if (specialPath != null && !specialPath.isEmpty()) {
                 boardPanel.setShortestPath(specialPath);
                 boardPanel.repaint();
            }
        }
        
        final List<Node> finalSpecialPath = specialPath;

        diceVisualizer.roll(diceValue, isGreen, () -> {
            String typeText;
            Color typeColor;
            
            if (finalSpecialPath != null) {
                typeText = "PRIMA! (Shortest Path)";
                typeColor = Color.MAGENTA; 
            } else {
                typeText = isGreen ? "MAJU (Green)" : "MUNDUR (Red)";
                typeColor = isGreen ? COLOR_GREEN_STEP : COLOR_RED_STEP;
            }
            
            instructionLabel.setText(typeText);
            instructionLabel.setForeground(typeColor);
            
            startPlayerMovement(currentPlayer, diceValue, isGreen, typeColor, finalSpecialPath);
        });
    }
    
    private void startPlayerMovement(Player player, int steps, boolean movingForward, Color stepColor, List<Node> specialPath) {
        Timer timer = new Timer(300, null);
        final int[] stepsLeft = {steps};
        
        timer.addActionListener(e -> {
            boolean hasWon = false;
            
            if (stepsLeft[0] > 0) {
                if (specialPath != null && !specialPath.isEmpty()) {
                    Node nextNode = specialPath.remove(0); 
                    player.position = nextNode.id; 
                    if (player.position == 64) hasWon = true;
                } else {
                    if (movingForward) {
                        player.position++;
                        if (player.position >= 64) { player.position = 64; hasWon = true; }
                    } else {
                        player.position--;
                        if (player.position <= 1) player.position = 1;
                    }
                }
                
                boardPanel.setActivePlayer(player, stepColor);
                boardPanel.repaint();
                stepsLeft[0]--;
                
                if (hasWon) {
                     ((Timer)e.getSource()).stop();
                     finishTurn(player, true);
                     return;
                }
            } else {
                ((Timer)e.getSource()).stop();
                
                if (specialPath == null) {
                    Node currentNode = graph.getNode(player.position);
                    if (currentNode.jumpTo != null) {
                        JOptionPane.showMessageDialog(this, "YAYY NAIK" + currentNode.jumpTo.id);
                        player.position = currentNode.jumpTo.id;
                        boardPanel.repaint();
                    }
                }
                finishTurn(player, false);
            }
        });
        timer.start();
    }
    
    private void finishTurn(Player player, boolean isWinner) {
        isAnimating = false;
        rollButton.setEnabled(true);
        boardPanel.clearActiveEffect();
        instructionLabel.setText("Wait for roll...");
        instructionLabel.setForeground(Color.GRAY);

        if (isWinner) {
            JOptionPane.showMessageDialog(this, "YAY! " + player.name + " WINS! \u2605");
            return; 
        }

        if (player.position % 5 == 0) {
            JOptionPane.showMessageDialog(this, "Bonus Turn! (Posisi " + player.position + ")");
            playerQueue.addFirst(player);
        } else {
            playerQueue.addLast(player);
        }
        
        boardPanel.repaint();
        updateUIState();
    }
    
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) if (n % i == 0) return false;
        return true;
    }
}

// --------------------------------------------------------
// CLASS PENDUKUNG (Dice, Panel, Card, Button)
// --------------------------------------------------------

class DiceVisualizer extends JPanel {
    private int value = 1;
    private Color diceColor = Color.decode("#A3B087"); 
    private Timer rollTimer;
    private int animationSteps = 0;
    
    public DiceVisualizer() {
        setPreferredSize(new Dimension(80, 80));
        setBackground(null); setOpaque(false);
    }
    
    public void roll(int finalValue, boolean isGreen, Runnable onFinish) {
        Color targetColor = isGreen ? Color.decode("#77DD77") : Color.decode("#FF6961");
        animationSteps = 0;
        rollTimer = new Timer(50, null); 
        
        rollTimer.addActionListener(e -> {
            animationSteps++;
            value = new Random().nextInt(6) + 1;
            diceColor = (animationSteps % 2 == 0) ? Color.WHITE : Color.decode("#A3B087");
            repaint();
            
            if (animationSteps > 15) { 
                ((Timer)e.getSource()).stop();
                value = finalValue;
                diceColor = targetColor; 
                repaint();
                onFinish.run(); 
                
                Timer reset = new Timer(1000, evt -> { diceColor = Color.decode("#A3B087"); repaint(); });
                reset.setRepeats(false); reset.start();
            }
        });
        rollTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(diceColor);
        g2d.fillRoundRect(5, 5, 70, 70, 20, 20);
        g2d.setColor(Color.WHITE); g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(5, 5, 70, 70, 20, 20);
        
        g2d.setColor(Color.WHITE);
        int dot=12; int c=40; int l=20; int r=60; int t=20; int b=60;
        if (value%2!=0) fillDot(g2d,c,c,dot); 
        if (value>1) { fillDot(g2d,l,t,dot); fillDot(g2d,r,b,dot); }
        if (value>3) { fillDot(g2d,r,t,dot); fillDot(g2d,l,b,dot); }
        if (value==6) { fillDot(g2d,l,c,dot); fillDot(g2d,r,c,dot); }
    }
    private void fillDot(Graphics2D g, int x, int y, int s) { g.fillOval(x-s/2, y-s/2, s, s); }
}

// --- UPDATE: PLAYER CARD JADI BINTANG ---
class PlayerCard extends JPanel {
    public PlayerCard(Player p) {
        setLayout(new BorderLayout()); setOpaque(false);
        setPreferredSize(new Dimension(200, 40)); setMaximumSize(new Dimension(200, 40));
        
        JPanel icon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gambar Bintang
                int centerX = 20, centerY = 20;
                int outer=15, inner=7;
                Path2D s = new Path2D.Double();
                for(int i=0; i<10; i++) {
                    double a = i*Math.PI/5 - Math.PI/2;
                    double r = (i%2==0)?outer:inner;
                    double px = centerX + Math.cos(a)*r, py = centerY + Math.sin(a)*r;
                    if(i==0) s.moveTo(px,py); else s.lineTo(px,py);
                }
                s.closePath();
                g2d.setColor(p.color); g2d.fill(s);
                g2d.setColor(Color.GRAY); g2d.setStroke(new BasicStroke(1)); g2d.draw(s);
            }
        };
        icon.setPreferredSize(new Dimension(40,40)); icon.setOpaque(false);
        
        JLabel lbl = new JLabel(p.name);
        lbl.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
        lbl.setBorder(new EmptyBorder(0,10,0,0));
        
        add(icon, BorderLayout.WEST); add(lbl, BorderLayout.CENTER);
        setBorder(BorderFactory.createMatteBorder(0,0,1,0, new Color(230,230,230)));
    }
}

class RoundedButton extends JButton {
    public RoundedButton(String t) { super(t); setContentAreaFilled(false); }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getModel().isArmed()?getBackground().darker():getBackground());
        g2.fillRoundRect(0,0,getWidth(),getHeight(),30,30);
        super.paintComponent(g2); g2.dispose();
    }
    @Override protected void paintBorder(Graphics g) {}
}

class GraphBoardPanel extends JPanel {
    private GameGraph graph;
    private List<Player> players; 
    private Player activePlayer = null;
    private Color activeStepColor = null;
    private List<Node> highlightedPath = null;
    
    private static final int R = 22; // Radius
    
    public GraphBoardPanel(GameGraph g, List<Player> p) {
        this.graph = g; this.players = p;
        setBackground(Color.WHITE); setPreferredSize(new Dimension(600, 600));
    }
    public void setActivePlayer(Player p, Color c) { activePlayer = p; activeStepColor = c; }
    public void clearActiveEffect() { activePlayer = null; activeStepColor = null; }
    public void setShortestPath(List<Node> p) { highlightedPath = p; }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w=getWidth(), h=getHeight(), xStep=w/8, yStep=h/8;
        for(int i=1; i<=64; i++) {
            Node n = graph.getNode(i);
            int rowInv = (i-1)/8, col = (i-1)%8;
            int r = 7 - rowInv;
            int c = (rowInv%2==0) ? col : 7-col;
            n.x = xStep/2 + c*xStep; n.y = yStep/2 + r*yStep;
        }

        g2d.setStroke(new BasicStroke(2)); g2d.setColor(new Color(220,220,220));
        for(int i=1; i<64; i++) {
            Node n1=graph.getNode(i), n2=graph.getNode(i+1);
            g2d.drawLine(n1.x, n1.y, n2.x, n2.y);
        }
        
        g2d.setColor(new Color(135, 206, 250));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{9}, 0));
        for(int i=1; i<=64; i++){
            Node n = graph.getNode(i);
            if(n.jumpTo != null) {
                g2d.drawLine(n.x, n.y, n.jumpTo.x, n.jumpTo.y);
                g2d.fillOval((n.x+n.jumpTo.x)/2-3, (n.y+n.jumpTo.y)/2-3, 6, 6);
            }
        }
        
        if (highlightedPath != null && !highlightedPath.isEmpty()) {
            g2d.setColor(Color.decode("#FF6961")); g2d.setStroke(new BasicStroke(3));
            Node prev = null;
            for (Node curr : highlightedPath) {
                if (prev != null) g2d.drawLine(prev.x, prev.y, curr.x, curr.y);
                prev = curr;
            }
        }

        g2d.setStroke(new BasicStroke(1));
        for(int i=1; i<=64; i++) {
            Node n = graph.getNode(i);
            g2d.setColor((i%5==0)?Color.decode("#D2B48C"):Color.decode("#FCF5EE"));
            g2d.fillOval(n.x-R, n.y-R, R*2, R*2);
            g2d.setColor(Color.decode("#E0E0E0"));
            g2d.drawOval(n.x-R, n.y-R, R*2, R*2);
            
            g2d.setColor((i%5==0)?Color.WHITE:Color.BLACK);
            String txt = (i==1)?"START":(i==64?"64":String.valueOf(i));
            g2d.setFont(new Font("Comic Sans MS", (i==1||i==64)?Font.BOLD:Font.PLAIN, 10));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(txt, n.x-fm.stringWidth(txt)/2, n.y+fm.getAscent()/2-2);
        }
        
        for(Player p : players) drawP(g2d, p);
        if(activePlayer!=null) drawP(g2d, activePlayer);
    }
    
    private void drawP(Graphics2D g, Player p) {
        Node n = graph.getNode(p.position);
        int id = Integer.parseInt(p.id.substring(1));
        int dx = n.x + (int)(Math.cos(id)*8);
        int dy = n.y + (int)(Math.sin(id)*8);
        
        if(p==activePlayer && activeStepColor!=null) {
            g.setColor(activeStepColor); g.setStroke(new BasicStroke(2));
            g.drawOval(dx-R, dy-R, R*2, R*2);
        }
        
        Path2D s = new Path2D.Double();
        for(int i=0; i<10; i++) {
            double a = i*Math.PI/5 - Math.PI/2;
            double r = (i%2==0)?(R-4):(R-14);
            double px = dx + Math.cos(a)*r, py = dy + Math.sin(a)*r;
            if(i==0) s.moveTo(px,py); else s.lineTo(px,py);
        }
        s.closePath();
        g.setColor(p.color); g.fill(s);
        g.setColor(Color.DARK_GRAY); g.setStroke(new BasicStroke(1)); g.draw(s);
        
        g.setColor(Color.BLACK); g.setFont(new Font("Arial",Font.BOLD,9));
        g.drawString(p.id, dx-6, dy+4);
    }
}