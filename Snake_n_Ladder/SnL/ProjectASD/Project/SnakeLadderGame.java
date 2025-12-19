import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.*;
import javax.sound.sampled.*; // Untuk Audio

public class SnakeLadderGame extends JFrame {
    private GameGraph graph;
    private GraphBoardPanel boardPanel;
    
    // --- FITUR TURN MANAGEMENT (LINKED LIST) ---
    private LinkedList<Player> playerQueue; 
    
    // --- FITUR STATISTICS ---
    // Top Score (Setiap round)
    private PriorityQueue<Player> topScores; 
    // Top Win (Global History - Static biar tidak hilang pas restart)
    private static Map<String, Integer> globalWinHistory = new HashMap<>(); 
    
    private DiceVisualizer diceVis;
    private JLabel infoLabel;
    private JButton rollBtn;
    private JPanel leaderboardPanel;
    
    private SoundManager soundManager;
    private boolean isAnimating = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeLadderGame());
    }

    public SnakeLadderGame() {
        soundManager = new SoundManager();
        playerQueue = new LinkedList<>();
        topScores = new PriorityQueue<>((p1, p2) -> p2.score - p1.score); // Descending Score
        
        setupGameData();
        initUI();
        soundManager.playBGM("bgm.wav"); // Pastikan ada file bgm.wav di folder assets
    }
    
    private void setupGameData() {
        graph = new GameGraph(64); // Bisa diubah jumlah nodenya
        
        // Input Player
        String input = JOptionPane.showInputDialog("How many players?");
        int count = 2;
        try { count = Integer.parseInt(input); } catch(Exception e){}
        
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE};
        
        for(int i=0; i<count; i++) {
            String name = JOptionPane.showInputDialog("Name Player " + (i+1));
            if(name == null || name.isEmpty()) name = "P" + (i+1);
            Player p = new Player(name, colors[i % colors.length]);
            
            // Masukkan ke Queue (Belakang)
            playerQueue.addLast(p);
            
            // Inisialisasi Win History jika player baru
            globalWinHistory.putIfAbsent(p.name, 0);
        }
    }

    private void initUI() {
        setTitle("Snake & Ladder: Advanced Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Center Board
        boardPanel = new GraphBoardPanel(graph, playerQueue);
        add(boardPanel, BorderLayout.CENTER);

        // Right Control Panel
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(new EmptyBorder(10,10,10,10));
        sidePanel.setPreferredSize(new Dimension(250, 600));

        diceVis = new DiceVisualizer();
        infoLabel = new JLabel("Welcome!");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        rollBtn = new JButton("ROLL DICE");
        
        // Leaderboard Panel
        leaderboardPanel = new JPanel();
        leaderboardPanel.setLayout(new BoxLayout(leaderboardPanel, BoxLayout.Y_AXIS));
        updateLeaderboardUI();

        sidePanel.add(new JLabel("Current Turn:"));
        sidePanel.add(diceVis);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(infoLabel);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(rollBtn);
        sidePanel.add(Box.createVerticalStrut(30));
        sidePanel.add(new JLabel("--- STATISTICS ---"));
        sidePanel.add(leaderboardPanel);

        add(sidePanel, BorderLayout.EAST);
        
        rollBtn.addActionListener(e -> doTurn());
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // --- CORE LOGIC ---
    private void doTurn() {
        if(isAnimating) return;
        isAnimating = true;
        rollBtn.setEnabled(false);

        // 1. Ambil Giliran (POLL dari depan)
        Player currPlayer = playerQueue.poll();
        infoLabel.setText(currPlayer.name + " Rolling...");

        // 2. Roll Logic (1-6)
        Random rand = new Random();
        int steps = rand.nextInt(6) + 1;
        
        // 3. Probability Logic: 80% Green (Maju), 20% Red (Mundur)
        boolean isGreen = rand.nextDouble() < 0.8; 
        
        soundManager.playSound("roll.wav");
        
        diceVis.animateRoll(steps, isGreen, -> {
            // Callback setelah animasi dadu selesai
            String dirText = isGreen ? "MAJU (Green)" : "MUNDUR (Red)";
            infoLabel.setText(currPlayer.name + ": " + steps + " " + dirText);
            
            // Proses langkah animasi
            processMovement(currPlayer, steps, isGreen);
        });
    }

    private void processMovement(Player p, int steps, boolean forward) {
        Timer t = new Timer(300, null); // Animasi per langkah
        final int[] stepCount = {0};
        
        t.addActionListener(evt -> {
            // Update Posisi
            if(forward) p.position++;
            else p.position--;
            
            // Batasan Papan
            if(p.position > graph.size) p.position = graph.size;
            if(p.position < 1) p.position = 1;
            
            // Tambah Score setiap jalan
            if(forward) p.score += 5; 
            else p.score -= 2;

            boardPanel.setActivePlayer(p, forward ? Color.GREEN : Color.RED);
            boardPanel.repaint();
            soundManager.playSound("step.wav");
            
            stepCount[0]++;
            
            // Cek Selesai Langkah
            if(stepCount[0] >= steps) {
                t.stop();
                checkNodeEvent(p); // Cek Star, Score, Win
            }
        });
        t.start();
    }
    
    private void checkNodeEvent(Player p) {
        boardPanel.clearActiveEffect();
        Node currNode = graph.getNode(p.position);

        // 1. Cek Random Score di Node
        if(currNode.bonusScore > 0) {
            p.score += currNode.bonusScore;
            soundManager.playSound("coin.wav");
            JOptionPane.showMessageDialog(this, "Bonus Score! +" + currNode.bonusScore);
            currNode.bonusScore = 0; // Score diambil
        }
        
        // 2. Cek Menang
        if(p.position == graph.size) {
            handleWin(p);
            return;
        }
        
        // 3. Logic Giliran Selanjutnya (Star Node)
        if (p.position % 5 == 0) {
            // FITUR: STAR NODE = DOUBLE TURN (Add First)
            soundManager.playSound("powerup.wav");
            infoLabel.setText("STAR NODE! Double Turn!");
            playerQueue.addFirst(p); // Masukkan lagi di paling DEPAN
        } else {
            // Normal Turn (Add Last)
            playerQueue.addLast(p); // Masukkan ke paling BELAKANG
        }
        
        updateLeaderboardUI(); // Update UI Score
        
        isAnimating = false;
        rollBtn.setEnabled(true);
        
        // Update Label siapa next
        if(!playerQueue.isEmpty()) infoLabel.setText("Next: " + playerQueue.peek().name);
    }
    
    private void handleWin(Player p) {
        soundManager.playSound("win.wav");
        p.score += 500; // Bonus Win
        
        // Update Global Win History
        int wins = globalWinHistory.get(p.name);
        globalWinHistory.put(p.name, wins + 1);
        
        int choice = JOptionPane.showConfirmDialog(this, 
            p.name + " WIN!\nScore: " + p.score + "\nMain Lagi?", 
            "Game Over", JOptionPane.YES_NO_OPTION);
            
        if(choice == JOptionPane.YES_OPTION) {
            // Reset Game tapi simpan history win
            restartGame();
        } else {
            System.exit(0);
        }
    }
    
    private void restartGame() {
        // Reset posisi player tapi pertahankan objeknya
        for(Player p : playerQueue) p.reset();
        // Kalau player yang menang tadi ada di tengah antrian, kembalikan queue seperti semula atau acak
        dispose();
        new SnakeLadderGame(); // Rebuild fresh
    }

    private void updateLeaderboardUI() {
        leaderboardPanel.removeAll();
        
        // Update Priority Queue untuk Score saat ini
        topScores.clear();
        topScores.addAll(playerQueue);
        if(playerQueue.isEmpty()) return; // Saat awal init
        
        leaderboardPanel.add(new JLabel("--- Top Scores (Round) ---"));
        Object[] sortedArr = topScores.toArray();
        Arrays.sort(sortedArr, (a,b) -> ((Player)b).score - ((Player)a).score);
        
        for(int i=0; i<Math.min(3, sortedArr.length); i++) {
            Player p = (Player) sortedArr[i];
            leaderboardPanel.add(new JLabel((i+1) + ". " + p.name + ": " + p.score));
        }
        
        leaderboardPanel.add(Box.createVerticalStrut(10));
        leaderboardPanel.add(new JLabel("--- Top Wins (Global) ---"));
        
        // Sort Map Win History
        globalWinHistory.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                leaderboardPanel.add(new JLabel(entry.getKey() + ": " + entry.getValue() + " wins"));
            });
            
        leaderboardPanel.revalidate();
        leaderboardPanel.repaint();
    }
    
    // INNER CLASS: SOUND MANAGER SIMPLE
    class SoundManager {
        public void playSound(String f) {
            try {
                File file = new File("assets/" + f);
                if(file.exists()) {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    clip.start();
                }
            } catch(Exception e) { System.out.println("SFX Error: " + e.getMessage()); }
        }
        public void playBGM(String f) {
            // Implementasi BGM looping (Sama seperti sebelumnya)
        }
    }
}