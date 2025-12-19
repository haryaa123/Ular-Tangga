import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class SnakesLaddersGUI extends JFrame {
    private GameGraph graph;
    private GraphBoardPanel boardPanel; 
    
    // [QUEUE]: Antrian Pemain
    // Kita pakai Queue biar gilirannya urut (FIFO: First In First Out)
    private LinkedList<Player> playerQueue; 
    
    private List<Player> allPlayersForLeaderboard; 
    private SoundManager soundManager; 
    
    // Komponen GUI
    private JLabel turnLabel, instructionLabel;
    private JButton rollButton;
    private JPanel queueContainer; 
    private DiceVisualizer diceVisualizer; 
    
    // [PRIORITY QUEUE]: Leaderboard Otomatis
    // Skor diurutkan otomatis dari terbesar ke terkecil
    private static PriorityQueue<Player> globalHighScores = new PriorityQueue<>((p1, p2) -> Integer.compare(p2.score, p1.score));
    private static Map<String, Integer> globalWinCounts = new HashMap<>();

    private boolean isAnimating = false; 
    private final Color COLOR_BG_PANEL    = Color.decode("#FDFCF5"); 
    private final Color COLOR_DICE_THEME = Color.decode("#A3B087"); 
    
    private final Color[] PLAYER_COLORS = {
        Color.decode("#FDFD96"), Color.decode("#C3B1E1"), 
        Color.decode("#FFB7B2"), Color.decode("#AEC6CF"), 
        Color.decode("#B5EAD7"), Color.decode("#FFDAC1")  
    };

    public SnakesLaddersGUI() {
        soundManager = new SoundManager();
        playerQueue = new LinkedList<>();
        allPlayersForLeaderboard = new ArrayList<>();
        
        setupGame(); 
        initUI();    
    }
    
    private void setupGame() {
        graph = new GameGraph(64); 
        if (playerQueue.isEmpty()) { 
             setupPlayers();
        } else { 
             for(Player p : playerQueue) { p.position = 1; p.score = 0; }
        }
    }
    
    private void initUI() {
        setTitle("Snakes & Ladders Final");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        if(boardPanel != null) remove(boardPanel);
        boardPanel = new GraphBoardPanel(graph, playerQueue);
        add(boardPanel, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        controlPanel.setBackground(COLOR_BG_PANEL);
        controlPanel.setPreferredSize(new Dimension(280, 0)); 
        
        turnLabel = new JLabel("Player's Turn");
        turnLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
        turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        diceVisualizer = new DiceVisualizer();
        JPanel diceWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        diceWrapper.setBackground(COLOR_BG_PANEL);
        diceWrapper.add(diceVisualizer);
        
        instructionLabel = new JLabel("Let's Roll!");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        rollButton = new RoundedButton("ROLL DICE");
        rollButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        rollButton.setBackground(COLOR_DICE_THEME);
        rollButton.setForeground(Color.WHITE);
        
        queueContainer = new JPanel();
        queueContainer.setLayout(new BoxLayout(queueContainer, BoxLayout.Y_AXIS));
        queueContainer.setBackground(COLOR_BG_PANEL);
        
        JButton btnStats = new JButton("View Global Stats");
        btnStats.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStats.addActionListener(e -> showGlobalStats());
        
        rollButton.addActionListener(e -> {
            if (!isAnimating) {
                try { soundManager.playSound("roll.wav"); } catch(Exception ex) {}
                rollDice();
            }
        });
        
        controlPanel.add(turnLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        controlPanel.add(diceWrapper);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        controlPanel.add(instructionLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        controlPanel.add(rollButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(new JScrollPane(queueContainer)); 
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(btnStats);
        
        add(controlPanel, BorderLayout.EAST);
        updateUIState();
        pack(); setLocationRelativeTo(null); setVisible(true);
    }
    
    private void setupPlayers() {
        int count = 2;
        try {
            String input = JOptionPane.showInputDialog(this, "How many players?", "2");
            if(input != null) count = Integer.parseInt(input);
        } catch (Exception e) {}
        
        if(count < 1) count = 1; if(count > 6) count = 6;
        for (int i = 0; i < count; i++) {
            Player p = new Player("P" + (i+1), "Player " + (i+1), PLAYER_COLORS[i % PLAYER_COLORS.length]);
            playerQueue.add(p);
            allPlayersForLeaderboard.add(p);
        }
    }

    private void updateUIState() {
        queueContainer.removeAll();
        allPlayersForLeaderboard.sort((p1, p2) -> Integer.compare(p2.score, p1.score));
        for (Player p : allPlayersForLeaderboard) {
            queueContainer.add(new PlayerCard(p));
            queueContainer.add(Box.createRigidArea(new Dimension(0, 8))); 
        }
        queueContainer.revalidate(); queueContainer.repaint();
        if (!playerQueue.isEmpty()) {
            Player nextP = playerQueue.peek();
            turnLabel.setText(nextP.name + "'s Turn");
            try { turnLabel.setForeground(nextP.color.darker()); } catch (Exception e) {}
        }
    }

    private void rollDice() {
        if (playerQueue.isEmpty()) return;
        isAnimating = true;
        rollButton.setEnabled(false); 
        
        try { boardPanel.setShortestPath(null); } catch (Exception e) {}
        
        Player currentPlayer = playerQueue.poll(); 
        updateUIState(); 

        Random rand = new Random();
        int diceValue = rand.nextInt(6) + 1;
        boolean isGreen = rand.nextDouble() < 0.7; 
        
        List<Node> specialPath = null;
        
        // Cek Fitur Prima (Bonus Shortest Path)
        if (isPrime(currentPlayer.position)) {
            try {
                specialPath = graph.getShortestPath(currentPlayer.position);
                if (specialPath != null && !specialPath.isEmpty()) {
                     boardPanel.setShortestPath(specialPath);
                     boardPanel.repaint();
                }
            } catch (Exception e) { }
        }
        
        final List<Node> finalSpecialPath = specialPath;
        diceVisualizer.roll(diceValue, isGreen, () -> {
            String typeText = (finalSpecialPath != null) ? "PRIMA! (Shortest Path)" : (isGreen ? "MAJU (Green)" : "MUNDUR (Red)");
            Color typeColor = (finalSpecialPath != null) ? Color.MAGENTA : (isGreen ? Color.decode("#77DD77") : Color.decode("#FF6961"));
            
            instructionLabel.setText(typeText);
            instructionLabel.setForeground(typeColor);
            startPlayerMovement(currentPlayer, diceValue, isGreen, typeColor, finalSpecialPath);
        });
    }
    
    private void startPlayerMovement(Player player, int steps, boolean movingForward, Color stepColor, List<Node> specialPath) {
        // [ANIMASI TIMER]
        // Pakai Timer supaya bidak jalannya pelan-pelan (animasi), gak langsung pindah
        Timer timer = new Timer(400, null);
        final int[] stepsLeft = {steps};
        
        timer.addActionListener(e -> {
            try {
                boolean hasWon = false;
                if (stepsLeft[0] > 0) {
                    try { soundManager.playSound("step.wav"); } catch(Exception ex) {}
                    
                    if (specialPath != null && !specialPath.isEmpty()) {
                        Node nextNode = specialPath.remove(0); 
                        player.position = nextNode.id; 
                        if (player.position == 64) hasWon = true;
                    } else {
                        if (movingForward) {
                            player.position++;
                            player.score += 10; 
                            if (player.position >= 64) { player.position = 64; hasWon = true; }
                            
                            // Cek Tangga
                            Node currentNode = graph.getNode(player.position);
                            if (currentNode != null && currentNode.jumpTo != null) {
                                try { soundManager.playSound("jump.wav"); } catch(Exception ex) {}
                                player.position = currentNode.jumpTo.id;
                                player.score += 50; 
                            }
                        } else {
                            // Cek Ular
                            Node currentNode = graph.getNode(player.position);
                            if (currentNode != null && currentNode.jumpFrom != null) {
                                player.position = currentNode.jumpFrom.id;
                                try { soundManager.playSound("jump.wav"); } catch(Exception ex) {}
                            } else {
                                player.position--;
                            }
                            player.score -= 5; 
                            if (player.position <= 1) player.position = 1;
                        }
                    }
                    try { boardPanel.setActivePlayer(player, stepColor); } catch(Exception ex){}
                    boardPanel.repaint();
                    stepsLeft[0]--;
                    
                    if (hasWon) {
                         ((Timer)e.getSource()).stop();
                         finishTurn(player, true);
                         return;
                    }
                } else {
                    ((Timer)e.getSource()).stop();
                    // Cek Bonus Koin pas berhenti
                    Node landingNode = graph.getNode(player.position);
                    if (landingNode != null && landingNode.bonusScore > 0) {
                        player.score += landingNode.bonusScore;
                        try { soundManager.playSound("coin.wav"); } catch(Exception ex) {}
                        instructionLabel.setText("BONUS SCORE! +" + landingNode.bonusScore);
                        landingNode.bonusScore = 0; 
                        boardPanel.repaint();
                    }
                    finishTurn(player, false);
                }
            } catch (Exception ex) {
                ((Timer)e.getSource()).stop();
                finishTurn(player, false);
            }
        });
        timer.start();
    }
    
    private void finishTurn(Player player, boolean isWinner) {
        isAnimating = false;
        rollButton.setEnabled(true);
        try { boardPanel.clearActiveEffect(); } catch(Exception e){} 
        instructionLabel.setText("Wait for roll...");

        if (isWinner) {
            try { soundManager.playSound("win.wav"); } catch(Exception ex) {}
            player.score += 500; 
            globalWinCounts.put(player.name, globalWinCounts.getOrDefault(player.name, 0) + 1);
            try { globalHighScores.add(new Player(player)); } catch(Exception e) { globalHighScores.add(player); }
            
            int choice = JOptionPane.showConfirmDialog(this, 
                "YAY! " + player.name + " WINS! \nScore: " + player.score + "\n\nMain lagi? (Reset Map)", 
                "Game Over", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                setupGame(); initUI(); 
            } else {
                System.exit(0);
            }
            return; 
        }
        playerQueue.addLast(player);
        boardPanel.repaint();
        updateUIState();
    }
    
    private void showGlobalStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== HALL OF FAME ===\n\n[ Top 3 High Scores ]\n");
        PriorityQueue<Player> tempQ = new PriorityQueue<>(globalHighScores);
        int rank = 1;
        while (!tempQ.isEmpty() && rank <= 3) {
            Player p = tempQ.poll();
            sb.append(rank + ". " + p.name + " : " + p.score + "\n");
            rank++;
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Global Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) if (n % i == 0) return false;
        return true;
    }
}