package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class UIWindow {
    private int x, y;
    private int width = 540;
    private int height = 600;
    private String title = "";
    private BufferedImage windowSprite;
    private boolean showCross = true;
    private List<UIButton> buttons = new ArrayList<>();
    private Runnable onClose;
    
    private static final int TITLE_HEIGHT = 50;
    private static final int CROSS_SIZE = 32;
    private static final int BUTTON_WIDTH = 128;
    private static final int BUTTON_HEIGHT = 32;
    private static final int PADDING = 10;
    
    public UIWindow(int x, int y, BufferedImage windowSprite) {
        this.x = x;
        this.y = y;
        this.windowSprite = windowSprite;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setShowCross(boolean show) {
        this.showCross = show;
    }
    
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }
    
    public void addButton(String label, BufferedImage buttonSprite, Runnable onClick) {
        int buttonCount = buttons.size();
        int totalButtonWidth = (buttonCount + 1) * BUTTON_WIDTH + buttonCount * PADDING;
        int startX = x + width - totalButtonWidth - PADDING;
        int buttonY = y + height - BUTTON_HEIGHT - PADDING;
        int buttonX = startX + buttonCount * (BUTTON_WIDTH + PADDING);
        
        UIButton button = new UIButton(buttonX, buttonY, label, buttonSprite, onClick);
        buttons.add(button);
    }
    
    public void clearButtons() {
        buttons.clear();
    }
    
    public void draw(Graphics2D g2d) {
        // Рисуем окно
        if (windowSprite != null) {
            g2d.drawImage(windowSprite, x, y, width, height, null);
        } else {
            // Fallback
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect(x, y, width, height);
            g2d.setColor(new Color(100, 100, 100));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x, y, width, height);
        }
        
        // Рисуем текст заголовка (спрайт окна уже содержит фон заголовка)
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + PADDING;
        int textY = y + (TITLE_HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(title, textX, textY);
        
        // Рисуем кнопки
        for (UIButton button : buttons) {
            button.draw(g2d);
        }
    }
    
    public UIButton getButtonAt(int px, int py) {
        for (UIButton button : buttons) {
            if (button.contains(px, py)) {
                return button;
            }
        }
        return null;
    }
    
    public void handleMouseClick(int px, int py) {
        UIButton button = getButtonAt(px, py);
        if (button != null) {
            button.click();
        }
    }
    
    public void handleMouseMove(int px, int py) {
        for (UIButton button : buttons) {
            button.setHovered(button.contains(px, py));
        }
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
