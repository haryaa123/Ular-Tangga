import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Random;

// CLASS DiceVisualizer
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
        int dot=12, c=40, l=20, r=60, t=20, b=60;
        if (value%2!=0) fillDot(g2d,c,c,dot); 
        if (value>1) { fillDot(g2d,l,t,dot); fillDot(g2d,r,b,dot); }
        if (value>3) { fillDot(g2d,r,t,dot); fillDot(g2d,l,b,dot); }
        if (value==6) { fillDot(g2d,l,c,dot); fillDot(g2d,r,c,dot); }
    }
    private void fillDot(Graphics2D g, int x, int y, int s) { g.fillOval(x-s/2, y-s/2, s, s); }
}

// CLASS PlayerCard (Untuk Leaderboard UI)
class PlayerCard extends JPanel {
    public PlayerCard(Player p) {
        setLayout(new BorderLayout()); setOpaque(false);
        setPreferredSize(new Dimension(200, 40)); setMaximumSize(new Dimension(200, 40));
        
        JPanel icon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int centerX = 20, centerY = 20;
                Path2D s = new Path2D.Double();
                for(int i=0; i<10; i++) {
                    double a = i*Math.PI/5 - Math.PI/2;
                    double r = (i%2==0)?15:7;
                    double px = centerX + Math.cos(a)*r, py = centerY + Math.sin(a)*r;
                    if(i==0) s.moveTo(px,py); else s.lineTo(px,py);
                }
                s.closePath();
                g2d.setColor(p.color); g2d.fill(s);
                g2d.setColor(Color.GRAY); g2d.setStroke(new BasicStroke(1)); g2d.draw(s);
            }
        };
        icon.setPreferredSize(new Dimension(40,40)); icon.setOpaque(false);
        
        // REVISI: Tampilkan Skor
        JLabel lbl = new JLabel(p.name + " (" + p.score + ")");
        lbl.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
        lbl.setBorder(new EmptyBorder(0,10,0,0));
        
        add(icon, BorderLayout.WEST); add(lbl, BorderLayout.CENTER);
        setBorder(BorderFactory.createMatteBorder(0,0,1,0, new Color(230,230,230)));
    }
}

// CLASS RoundedButton
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