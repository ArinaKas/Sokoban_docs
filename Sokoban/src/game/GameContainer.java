package game;

import javax.swing.*;

import graphics.SpriteManager;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import ui.UIWindow;
import ui.UIManager;
import score.ScoreManager;
import score.ScoreRecord;
import java.util.ArrayList;
import java.util.Collections;

public class GameContainer extends JPanel {
    private GamePanel gamePanel;
    private ui.UIManager uiManager;
    private UIWindow scoresWindow;
    private ScoreManager scoreManager;
    private SpriteManager spriteManager;
    private boolean showScores = false;
    private boolean showVictory = false;
    private ScoreRecord currentRecord = null;
    
    public GameContainer(GamePanel gamePanel, ScoreManager scoreManager, SpriteManager spriteManager) {
        this.gamePanel = gamePanel;
        this.scoreManager = scoreManager;
        this.spriteManager = spriteManager;
        this.uiManager = new UIManager(spriteManager);
        
        setLayout(null);
        setPreferredSize(new Dimension(640, 640));
        setBackground(new Color(40, 40, 40));
        
        // Обработчик мыши
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (showScores || showVictory) {
                    uiManager.handleMouseClick(e.getX(), e.getY());
                    repaint();
                }
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (showScores || showVictory) {
                    uiManager.handleMouseMove(e.getX(), e.getY());
                    repaint();
                }
            }
        });
    }
    
    public void showScoresWindow() {
        if (showScores) {
            hideScoresWindow();
            return;
        }
        
        showScores = true;
        gamePanel.pause(); // Останавливаем таймер
        uiManager.getWindows().clear();
        
        scoresWindow = uiManager.createWindow(50, 20);
        scoresWindow.setTitle("Таблица рекордов");
        scoresWindow.setShowCross(true);
        
        // Кнопка закрытия
        try {
            BufferedImage buttonSprite = spriteManager.loadImage("resources/images/ui/button.png");
            scoresWindow.addButton("Закрыть", buttonSprite, this::hideScoresWindow);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки спрайта кнопки: " + e.getMessage());
        }
        
        // Крестик закрывает окно
        scoresWindow.setOnClose(this::hideScoresWindow);
        
        repaint();
    }
    
    public void hideScoresWindow() {
        showScores = false;
        gamePanel.resume(); // Возобновляем таймер
        uiManager.getWindows().clear();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Рисуем игру
        gamePanel.paintComponent(g2d);
        
        // Если показываем рекорды или победу, рисуем полупрозрачный оверлей и окно
        if (showScores || showVictory) {
            // Полупрозрачный оверлей
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Рисуем UI окно
            uiManager.drawAll(g2d);
            
            // Рисуем рекорды
            drawScores(g2d);
        }
    }

    private void drawScores(Graphics2D g2d) {
        // Получаем абсолютно все рекорды из менеджера
        java.util.List<ScoreRecord> allScores = scoreManager.getAllScores();

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        int startY = 90;
        int lineHeight = 22;
        int sectionGap = 15;
        boolean foundCurrent = false;

        // Массивы с кодами сложностей и их текстовыми названиями
        int[] difficulties = {2, 4, 6};
        String[] titles = {"Легкий (2 ящика)", "Средний (4 ящика)", "Сложный (6 ящиков)"};

        // Перебираем каждую сложность по очереди
        for (int d = 0; d < difficulties.length; d++) {
            int targetDifficulty = difficulties[d];

            // Пишем заголовок сложности (делаем его чуть крупнее и выделяем цветом)
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(new Color(222, 12, 85));
            g2d.drawString(titles[d], 80, startY);
            startY += lineHeight;

            // Фильтруем рекорды, оставляя только нужную сложность, и берем первые 5 (чтобы всё влезло на экран)
            java.util.List<ScoreRecord> filteredScores = new ArrayList<>();
            for (ScoreRecord record : allScores) {
                if (record.getLevelDifficulty() == targetDifficulty) {
                    filteredScores.add(record);
                }
            }

            // Сортируем (на всякий случай, хотя ScoreManager уже сортирует их при добавлении)
            Collections.sort(filteredScores);

            // Ограничиваем вывод топ-5 рекордами на одну сложность, чтобы окно не резалось снизу
            int limit = Math.min(5, filteredScores.size());

            g2d.setFont(new Font("Arial", Font.PLAIN, 12));

            if (limit == 0) {
                g2d.setColor(Color.GRAY);
                g2d.drawString("   Нет рекордов", 80, startY);
                startY += lineHeight;
            } else {
                for (int i = 0; i < limit; i++) {
                    ScoreRecord record = filteredScores.get(i);

                    // Выделяем текущий только что поставленный рекорд зеленым цветом
                    if (showVictory && currentRecord != null && !foundCurrent &&
                            record.getTimeSeconds() == currentRecord.getTimeSeconds() &&
                            record.getMoves() == currentRecord.getMoves() &&
                            record.getLevelDifficulty() == currentRecord.getLevelDifficulty()) {
                        g2d.setColor(new Color(100, 255, 100));
                        foundCurrent = true;
                    } else {
                        g2d.setColor(Color.WHITE);
                    }

                    String text = String.format("   %d. %s | %s | Ходов: %d",
                            i + 1, record.getPlayerName(), record.getFormattedTime(), record.getMoves());
                    g2d.drawString(text, 80, startY);
                    startY += lineHeight;
                }
            }

            // Добавляем пустой отступ перед следующей секцией сложности
            startY += sectionGap;
        }
    }
    
    public void showVictoryWindow(long timeSeconds, int moves, int difficulty) {
        showVictory = true;
        gamePanel.pause();
        
        // Сохраняем рекорд
        currentRecord = new ScoreRecord(timeSeconds, moves, difficulty);
        scoreManager.addScore(timeSeconds, moves, difficulty);
        
        uiManager.getWindows().clear();
        
        scoresWindow = uiManager.createWindow(50, 20);
        scoresWindow.setTitle(String.format("Победа! Время: %s, Ходы: %d",
                timeSeconds, moves));
        scoresWindow.setShowCross(true);
        
        // Кнопки
        try {
            BufferedImage buttonSprite = spriteManager.loadImage("resources/images/ui/button.png");
            scoresWindow.addButton("Новый уровень", buttonSprite, this::onNewLevel);
            scoresWindow.addButton("Закрыть", buttonSprite, this::hideVictoryWindow);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки спрайта кнопки: " + e.getMessage());
        }
        
        // Крестик закрывает окно
        scoresWindow.setOnClose(this::hideVictoryWindow);
        
        repaint();
    }

    private void onNewLevel() {
        hideVictoryWindow();
        int currentDifficulty = gamePanel.getCurrentDifficulty();
        gamePanel.generateNewLevel(currentDifficulty);
        gamePanel.repaint();
    }
    
    private void hideVictoryWindow() {
        showVictory = false;
        currentRecord = null;
        gamePanel.resume();
        uiManager.getWindows().clear();
        repaint();
    }

    public boolean isShowScores() {
        return showScores;
    }

    public boolean isShowVictory() {
        return showVictory;
    }
}
