import javax.swing.*;
import java.awt.*;
import java.util.Random;

class DiceVisualizer extends JPanel {
    private int value = 1;
    private Color diceColor = Color.decode("#A3B087"); 
    private javax.swing.Timer rollTimer;
    private int animationSteps = 0;
    
    public DiceVisualizer() {
        setPreferredSize(new Dimension(80, 80));
        setBackground(null); setOpaque(false);
    }
    
    public void roll(int finalValue, boolean isGreen, Runnable onFinish) {
        Color targetColor = isGreen ? Color.decode("#77DD77") : Color.decode("#FF6961");
        animationSteps = 0;
        rollTimer = new javax.swing.Timer(50, null); 
        
        rollTimer.addActionListener(e -> {
            animationSteps++;
            value = new Random().nextInt(6) + 1;
            diceColor = (animationSteps % 2 == 0) ? Color.WHITE : Color.decode("#A3B087");
            repaint();
            
            if (animationSteps > 15) { 
                ((javax.swing.Timer)e.getSource()).stop();
                value = finalValue;
                diceColor = targetColor; 
                repaint();
                onFinish.run(); 
                javax.swing.Timer reset = new javax.swing.Timer(1000, evt -> { diceColor = Color.decode("#A3B087"); repaint(); });
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