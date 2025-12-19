import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;

public class SnakesLaddersGUI extends JFrame {
    private GameGraph graph;
    private GraphBoardPanel boardPanel; 
    private LinkedList<Player> playerQueue; 
    private List<Player> allPlayersForLeaderboard; // Untuk tracking leaderboard
    private SoundManager soundManager; // REVISI: Audio Manager
    
    // UI Components
    private JLabel turnLabel; 
    private JLabel instructionLabel;
    private JButton rollButton;
    private JPanel queueContainer; 
    private DiceVisualizer diceVisualizer; 
    
    private boolean isAnimating = false; 
    
    // Warna
    private final Color COLOR_BG_PANEL   = Color.decode("#FDFCF5"); 
    private final Color COLOR_GREEN_STEP = Color.decode("#77DD77"); 
    private final Color COLOR_RED_STEP   = Color.decode("#FF6961"); 
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
        
        setupGame(); // Setup Graph & Players
        initUI();    // Setup GUI
        
        soundManager.playBGM("bgm.wav"); // Play Music
    }
    
    private void setupGame() {
        graph = new GameGraph(64);
        if (playerQueue.isEmpty()) { // First run
             setupPlayers();
        } else { // Replay (Reset posisi)
             for(Player p : playerQueue) {
                 p.position = 1;
                 p.score = 0;
             }
        }
    }
    
    private void initUI() {
        setTitle("Cute Graph Snakes & Ladders");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel Board (Tengah)
        if(boardPanel != null) remove(boardPanel); // Remove old panel if replay
        boardPanel = new GraphBoardPanel(graph, playerQueue);
        add(boardPanel, BorderLayout.CENTER);
        
        // Panel Kanan (Kontrol)
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
        rollButton.setFont(new Font("Verdana", Font.BOLD, 14));
        rollButton.setBackground(COLOR_DICE_THEME);
        rollButton.setForeground(Color.WHITE);
        
        JLabel qTitle = new JLabel("Leaderboard (Score):");
        qTitle.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        qTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        queueContainer = new JPanel();
        queueContainer.setLayout(new BoxLayout(queueContainer, BoxLayout.Y_AXIS));
        queueContainer.setBackground(COLOR_BG_PANEL);
        
        rollButton.addActionListener(e -> {
            if (!isAnimating) {
                soundManager.playSound("roll.wav");
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
        controlPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        controlPanel.add(qTitle);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(new JScrollPane(queueContainer)); // REVISI: Scrollable list
        
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
            Player p = new Player("P" + (i+1), name, PLAYER_COLORS[i % PLAYER_COLORS.length]);
            playerQueue.add(p);
            allPlayersForLeaderboard.add(p);
        }
    }

    private void updateUIState() {
        queueContainer.removeAll();
        // REVISI: Tampilkan Leaderboard berdasarkan Skor Tertinggi
        allPlayersForLeaderboard.sort((p1, p2) -> Integer.compare(p2.score, p1.score));
        
        for (Player p : allPlayersForLeaderboard) {
            queueContainer.add(new PlayerCard(p));
            queueContainer.add(Box.createRigidArea(new Dimension(0, 8))); 
        }
        queueContainer.revalidate(); queueContainer.repaint();

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
        boolean isGreen = rand.nextDouble() < 0.7; // 70% chance maju
        
        // Cek Prima untuk Shortest Path
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
        Timer timer = new Timer(400, null);
        final int[] stepsLeft = {steps};
        
        timer.addActionListener(e -> {
            boolean hasWon = false;
            
            if (stepsLeft[0] > 0) {
                soundManager.playSound("step.wav"); // REVISI: Suara langkah
                
                // --- MOVEMENT LOGIC ---
                if (specialPath != null && !specialPath.isEmpty()) {
                    // Logika Prima (Shortest Path)
                    Node nextNode = specialPath.remove(0); 
                    player.position = nextNode.id; 
                    if (player.position == 64) hasWon = true;
                } else {
                    if (movingForward) {
                        // Maju Normal
                        player.position++;
                        player.score += 10; // Tambah skor kalau maju
                        if (player.position >= 64) { player.position = 64; hasWon = true; }
                        
                        // REVISI: Cek Instant Jump saat lewat garis biru (Maju)
                        Node currentNode = graph.getNode(player.position);
                        if (currentNode.jumpTo != null) {
                            soundManager.playSound("jump.wav");
                            player.position = currentNode.jumpTo.id;
                            player.score += 50; // Bonus naik tangga/turun ular
                        }
                        
                    } else {
                        // REVISI: Mundur (Dadu Merah) dengan Logika Tangga
                        Node currentNode = graph.getNode(player.position);
                        
                        if (currentNode.jumpFrom != null) {
                            // Kalau posisi sekarang adalah ATAS tangga, mundur ke BAWAH tangga
                            player.position = currentNode.jumpFrom.id;
                            soundManager.playSound("jump.wav");
                        } else {
                            // Mundur biasa
                            player.position--;
                        }
                        
                        player.score -= 5; // Kurang skor kalau mundur
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
            soundManager.playSound("win.wav");
            player.score += 500; // Bonus menang
            int choice = JOptionPane.showConfirmDialog(this, 
                "YAY! " + player.name + " WINS! \nScore: " + player.score + "\n\nMain lagi? (Reset Map)", 
                "Game Over", JOptionPane.YES_NO_OPTION);
                
            if (choice == JOptionPane.YES_OPTION) {
                // REVISI: Reset Game
                setupGame();
                initUI(); // Rebuild UI
            } else {
                System.exit(0);
            }
            return; 
        }

        playerQueue.addLast(player);
        boardPanel.repaint();
        updateUIState();
    }
    
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) if (n % i == 0) return false;
        return true;
    }
}