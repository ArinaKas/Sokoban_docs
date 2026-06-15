package ui;

import java.awt.*;
import java.awt.image.BufferedImage;

public class UIButton {
    private int x, y;
    private int width = 128;
    private int height = 32;
    private String label;
    private BufferedImage sprite;
    private boolean hovered = false;
    private Runnable onClick;
    
    public UIButton(int x, int y, String label, BufferedImage sprite, Runnable onClick) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.sprite = sprite;
        this.onClick = onClick;
    }
    
    public void draw(Graphics2D g2d) {
        if (sprite != null) {
            g2d.drawImage(sprite, x, y, width, height, null);
        } else {
            // Fallback если нет спрайта
            g2d.setColor(hovered ? new Color(100, 150, 255) : new Color(70, 120, 200));
            g2d.fillRect(x, y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x, y, width, height);
        }
        
        // Рисуем текст
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(label)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(label, textX, textY);
    }
    
    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }
    
    public void click() {
        if (onClick != null) {
            onClick.run();
        }
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
