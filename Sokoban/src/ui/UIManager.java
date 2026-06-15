package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import graphics.SpriteManager;

public class UIManager {
    private List<UIWindow> windows = new ArrayList<>();
    private BufferedImage windowSprite;
    private BufferedImage buttonSprite;
    
    public UIManager(SpriteManager spriteManager) {
        loadUISprites(spriteManager);
    }
    
    private void loadUISprites(SpriteManager spriteManager) {
        try {
            windowSprite = spriteManager.loadImage("resources/images/ui/window.png");
            buttonSprite = spriteManager.loadImage("resources/images/ui/button.png");
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке UI спрайтов: " + e.getMessage());
        }
    }
    
    public UIWindow createWindow(int x, int y) {
        UIWindow window = new UIWindow(x, y, windowSprite);
        windows.add(window);
        return window;
    }
    
    public void removeWindow(UIWindow window) {
        windows.remove(window);
    }
    
    public void drawAll(Graphics2D g2d) {
        for (UIWindow window : windows) {
            window.draw(g2d);
        }
    }
    
    public void handleMouseClick(int px, int py) {
        // Обрабатываем окна в обратном порядке (верхние окна имеют приоритет)
        for (int i = windows.size() - 1; i >= 0; i--) {
            UIWindow window = windows.get(i);
            if (px >= window.getX() && px <= window.getX() + window.getWidth() &&
                py >= window.getY() && py <= window.getY() + window.getHeight()) {
                window.handleMouseClick(px, py);
                break;
            }
        }
    }
    
    public void handleMouseMove(int px, int py) {
        for (UIWindow window : windows) {
            window.handleMouseMove(px, py);
        }
    }
    
    public List<UIWindow> getWindows() {
        return windows;
    }
    
    public BufferedImage getButtonSprite() {
        return buttonSprite;
    }
}
