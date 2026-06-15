package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.awt.image.BufferedImage;
import entities.*;
import graphics.SpriteManager;
import level.LevelCode;
import level.LevelGenerator;

public class GamePanel extends JPanel {
    private static final int TILE_SIZE = 64;
    private static final int ROWS = 10;
    private static final int COLS = 10;

    TileType[][] grid;
    Player player;
    List<GameBox> boxes;
    List<Point> targets;
    SpriteManager sprites;

    Stack<GameState> history;

    int lastDx = 0, lastDy = 1;

    private int[][] currentLevel;

    private boolean useGenerator = true;
    
    private long levelStartTime;
    private int moveCount;
    private int currentDifficulty;
    private boolean paused = false;
    private long pausedTime = 0;
    private boolean levelCompleted = false;
    
    public GamePanel() {
        sprites = new SpriteManager();
        history = new Stack<>();
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
        initLevel();
        saveState(); // Сохраняем начальное состояние
    }

    private void initLevel() {
        grid = new TileType[ROWS][COLS];
        boxes = new ArrayList<>();
        targets = new ArrayList<>();

        int[][] level;
        
        if (useGenerator) {
            LevelGenerator generator = new LevelGenerator(COLS, ROWS, 2);
            level = generator.generateLevel();
            currentDifficulty = 2;
        } else {
            level = new int[][] {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 3, 0, 2, 0, 0, 0, 0, 1},
                {1, 0, 0, 4, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 3, 0, 2, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
            currentDifficulty = 1;
        }

        currentLevel = level;
        loadLevelFromArray(level);

        sprites.setPlayerDirection(0, 1);
        lastDx = 0;
        lastDy = 1;
        
        // Запускаем таймер
        levelStartTime = System.currentTimeMillis();
        moveCount = 0;
        levelCompleted = false;
    }
    
    public void resetLevel() {
        grid = new TileType[ROWS][COLS];
        boxes = new ArrayList<>();
        targets = new ArrayList<>();
        
        loadLevelFromArray(currentLevel);
        
        sprites.setPlayerDirection(0, 1);
        lastDx = 0;
        lastDy = 1;
        history.clear();
        saveState();
        
        // Перезапускаем таймер
        levelStartTime = System.currentTimeMillis();
        moveCount = 0;
        levelCompleted = false;
        
        repaint();
    }

    private void loadLevelFromArray(int[][] level) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                switch (level[row][col]) {
                    case LevelCode.WALL:
                        grid[row][col] = TileType.WALL;
                        break;
                    case LevelCode.TARGET:
                        grid[row][col] = TileType.TARGET;
                        targets.add(new Point(col, row));
                        break;
                    case LevelCode.BOX:
                        grid[row][col] = TileType.FLOOR;
                        GameBox newBox = new GameBox(col, row);
                        newBox.onTarget = targets.contains(new Point(col, row));
                        boxes.add(newBox);
                        break;
                    case LevelCode.PLAYER:
                        grid[row][col] = TileType.FLOOR;
                        player = new Player(col, row);
                        break;
                    default:
                        grid[row][col] = TileType.FLOOR;
                        break;
                }
            }
        }
    }

    private void saveState() {
        history.push(new GameState(player.getX(), player.getY(), lastDx, lastDy, boxes));

        if (history.size() > 50) {
            history.remove(0);
        }
    }

    public void undo() {
        if (history.size() > 1) {
            history.pop();
            GameState previousState = history.peek();
            previousState.restore(this);
            repaint();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Отрисовка уровня
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;

                if (sprites.getFloorSprite() != null) {
                    g2d.drawImage(sprites.getFloorSprite(), x, y, TILE_SIZE, TILE_SIZE, null);
                }

                if (grid[row][col] == TileType.WALL && sprites.getWallSprite() != null) {
                    g2d.drawImage(sprites.getWallSprite(), x, y, TILE_SIZE, TILE_SIZE, null);
                }

                if (grid[row][col] == TileType.TARGET && sprites.getTargetSprite() != null) {
                    g2d.drawImage(sprites.getTargetSprite(), x, y, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }

        // Рисуем ящики
        for (GameBox box : boxes) {
            int x = box.x * TILE_SIZE;
            int y = box.y * TILE_SIZE;

            BufferedImage boxSprite;
            if (box.onTarget && sprites.getBoxOnTargetSprite() != null) {
                boxSprite = sprites.getBoxOnTargetSprite();
            } else {
                boxSprite = sprites.getBoxSprite();
            }

            if (boxSprite != null) {
                g2d.drawImage(boxSprite, x, y, TILE_SIZE, TILE_SIZE, null);
            }

            if (box.onTarget) {
                g2d.setColor(new Color(255, 255, 0, 100));
                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }

        // Рисуем игрока
        if (sprites.getPlayerSprite() != null) {
            int playerX = player.getX() * TILE_SIZE;
            int playerY = player.getY() * TILE_SIZE;
            g2d.drawImage(sprites.getPlayerSprite(), playerX, playerY,
                    TILE_SIZE, TILE_SIZE, null);
        }

        // рисуем таймер
        long time = getElapsedTimeSeconds();
        String timerText = String.format("Время: %02d:%02d  |  Ходов: %d", time / 60, time % 60, moveCount);

        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRect(10, 10, 180, 25);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(timerText, 18, 27);
    }

    public boolean movePlayer(int keyCode) {
        if (levelCompleted || paused) {
            return false;
        }

        int dx = 0, dy = 0;

        switch (keyCode) {
            case KeyEvent.VK_UP: dy = -1; break;
            case KeyEvent.VK_DOWN: dy = 1; break;
            case KeyEvent.VK_LEFT: dx = -1; break;
            case KeyEvent.VK_RIGHT: dx = 1; break;
            default: return false;
        }

        // Сохраняем направление движения
        lastDx = dx;
        lastDy = dy;

        // Обновляем спрайт игрока
        sprites.setPlayerDirection(dx, dy);

        int newX = player.getX() + dx;
        int newY = player.getY() + dy;

        // Проверка на стену
        if (isWall(newX, newY)) {
            return false;
        }

        boolean moved = false;

        // Проверка на ящик
        GameBox boxAtNewPos = getBoxAt(newX, newY);
        if (boxAtNewPos != null) {
            int boxNewX = newX + dx;
            int boxNewY = newY + dy;

            // Можно ли толкнуть ящик?
            if (!isWall(boxNewX, boxNewY) && getBoxAt(boxNewX, boxNewY) == null) {
                // Сохраняем состояние ПЕРЕД движением
                saveState();

                moveBox(boxAtNewPos, boxNewX, boxNewY);
                player.move(dx, dy);
                moveCount++;
                moved = true;
            }
        } else {
            // Сохраняем состояние ПЕРЕД движением
            saveState();

            // Простое перемещение
            player.move(dx, dy);
            moveCount++;
            moved = true;
        }

        return moved;
    }

    private boolean isWall(int x, int y) {
        return x < 0 || x >= COLS || y < 0 || y >= ROWS ||
                grid[y][x] == TileType.WALL;
    }

    private GameBox getBoxAt(int x, int y) {
        for (GameBox box : boxes) {
            if (box.x == x && box.y == y) {
                return box;
            }
        }
        return null;
    }

    private boolean isOnTarget(int x, int y) {
        return targets.contains(new Point(x, y));
    }

    private void moveBox(GameBox box, int newX, int newY) {
        box.setPosition(newX, newY);
        box.onTarget = isOnTarget(newX, newY);
    }

    public boolean isGameWon() {
        for (GameBox box : boxes) {
            if (!box.onTarget) {
                return false;
            }
        }
        return true;
    }

    public void enableGenerator(boolean enable) {
        useGenerator = enable;
    }
    
    public void generateNewLevel(int numBoxes) {
        useGenerator = true;
        LevelGenerator generator = new LevelGenerator(COLS, ROWS, numBoxes);
        int[][] generatedLevel = generator.generateLevel();
        
        // Очищаем текущий уровень
        grid = new TileType[ROWS][COLS];
        boxes = new ArrayList<>();
        targets = new ArrayList<>();
        
        // Сохраняем сгенерированный уровень
        currentLevel = generatedLevel;
        
        // Загружаем сгенерированный уровень
        loadLevelFromArray(generatedLevel);
        
        sprites.setPlayerDirection(0, 1);
        lastDx = 0;
        lastDy = 1;
        history.clear();
        saveState();
        
        // Запускаем таймер для нового уровня
        levelStartTime = System.currentTimeMillis();
        moveCount = 0;
        currentDifficulty = numBoxes;
        levelCompleted = false;
        
        repaint();
    }
    
    public long getElapsedTimeSeconds() {
        if (paused) {
            return pausedTime / 1000;
        }
        return (System.currentTimeMillis() - levelStartTime) / 1000;
    }
    
    public void pause() {
        if (!paused) {
            paused = true;
            pausedTime = System.currentTimeMillis() - levelStartTime;
        }
    }
    
    public void resume() {
        if (paused) {
            paused = false;
            levelStartTime = System.currentTimeMillis() - pausedTime;
        }
    }
    
    public int getMoveCount() {
        return moveCount;
    }
    
    public int getCurrentDifficulty() {
        return currentDifficulty;
    }
    
    // Методы доступа для GameState
    public Player getPlayer() {
        return player;
    }
    
    public List<GameBox> getBoxes() {
        return boxes;
    }
    
    public SpriteManager getSprites() {
        return sprites;
    }
    
    public void setPlayerPosition(int x, int y) {
        player.setPosition(x, y);
    }
    
    public void setPlayerDirection(int dx, int dy) {
        lastDx = dx;
        lastDy = dy;
        sprites.setPlayerDirection(dx, dy);
    }

    public void setLevelCompleted(boolean completed) {
        this.levelCompleted = completed;
    }
}