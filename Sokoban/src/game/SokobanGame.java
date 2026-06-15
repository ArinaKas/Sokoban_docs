package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import score.ScoreManager;

public class SokobanGame extends JFrame {
    private GamePanel gamePanel;
    private GameContainer gameContainer;
    private JLabel statusLabel;
    private ScoreManager scoreManager;
    private graphics.SpriteManager spriteManager;
    private Timer refreshTimer; // Таймер для обновления экрана и текста в реальном времени

    public SokobanGame() {
        setTitle("Сокобан");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        scoreManager = new ScoreManager();
        spriteManager = new graphics.SpriteManager();
        gameContainer = new GameContainer(gamePanel, scoreManager, spriteManager);

        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Запуск таймера обновления (срабатывает 10 раз в секунду)
        refreshTimer = new Timer(100, e -> updateGameStatus());
        refreshTimer.start();

        // Регистрация обработчика клавиш
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }
        });

        add(gameContainer, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setFocusable(true);

        // Первичный вызов текста при старте
        updateGameStatus();
    }

    // Метод динамического обновления нижней надписи
    private void updateGameStatus() {
        long time = gamePanel.getElapsedTimeSeconds();
        String formattedTime = String.format("%02d:%02d", time / 60, time % 60);
        int moves = gamePanel.getMoveCount();

        if (gameContainer.isShowVictory()) {
            statusLabel.setText("Победа! C - очистить рекорды");
        } else if (gameContainer.isShowScores()) {
            statusLabel.setText(String.format("S - возобновить игру, Время: %s, Ходы: %d, C - очистить рекорды",
                    formattedTime, moves));
        } else {
            statusLabel.setText("Стрелки - движение, Z - отменить, R - в начало, Ctrl+R - перегенерировать");
        }

        // Заставляем контейнер постоянно перерисовываться, чтобы таймер шел на экране вверху
        gameContainer.repaint();
    }

    // Обработка нажатий клавиш
    private void handleKeyPressed(KeyEvent e) {
        // Открытие таблицы рекордов (S)
        if (e.getKeyCode() == KeyEvent.VK_S) {
            handleToggleScores();
            return;
        }

        // Очистка рекордов (C)
        if (e.getKeyCode() == KeyEvent.VK_C) {
            handleClearScores();
            return;
        }

        // Перегенерация с выбором сложности (Ctrl+R)
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_R) {
            handleLevelGenerationDifficulty();
            return;
        }

        // Обычный перезапуск уровня (R)
        if (e.getKeyCode() == KeyEvent.VK_R) {
            handleResetLevel();
            return;
        }

        // Отмена хода (Z)
        if (e.getKeyCode() == KeyEvent.VK_Z) {
            handleUndo();
            return;
        }

        // Движение игрока (стрелки)
        handlePlayerMovement(e);
    }

    // Переключение таблицы рекордов
    private void handleToggleScores() {
        if (gameContainer.isShowVictory()) {
            return;
        }
        gameContainer.showScoresWindow();
        updateGameStatus();
    }

    // Перезапуск уровня
    private void handleResetLevel() {
        gamePanel.resetLevel();
        gameContainer.repaint();
        updateGameStatus();
    }

    // Отмена хода
    private void handleUndo() {
        if (gamePanel.isGameWon()) {
            return;
        }
        gamePanel.undo();
        gameContainer.repaint();
        updateGameStatus();
    }

    // Движение игрока и проверка победы
    private void handlePlayerMovement(KeyEvent e) {
        if (gamePanel.movePlayer(e.getKeyCode())) {
            gameContainer.repaint();
            checkAndShowVictory();
            updateGameStatus();
        }
    }

    // Проверка условия победы и отображение окна победы
    private void checkAndShowVictory() {
        if (gamePanel.isGameWon() && !gameContainer.isShowVictory()) {
            long timeSeconds = gamePanel.getElapsedTimeSeconds();
            int moves = gamePanel.getMoveCount();
            int difficulty = gamePanel.getCurrentDifficulty();

            gamePanel.setLevelCompleted(true);
            gameContainer.showVictoryWindow(timeSeconds, moves, difficulty);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SokobanGame().setVisible(true);
        });
    }

    private void handleClearScores() {
        // Проверяем, открыто ли сейчас какое-то окно рекордов/победы
        if (!gameContainer.isShowScores() && !gameContainer.isShowVictory()) {
            return;
        }

        int response = JOptionPane.showConfirmDialog(
                SokobanGame.this,
                "Вы уверены, что хотите удалить все рекорды?",
                "Очистить рекорды",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            scoreManager.clearScores();
            gameContainer.repaint();
        }
    }

    private void handleLevelGenerationDifficulty() {
        String[] options = {"Легкая (2 ящика)", "Средняя (4 ящика)", "Сложная (6 ящиков)"};

        int choice = JOptionPane.showOptionDialog(
                SokobanGame.this,
                "Выберите сложность для нового уровня:",
                "Новый уровень",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        // Если пользователь нажал Cancel
        if (choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        // Конвертируем выбор в количество коробок
        int numBoxes = 2;
        switch (choice) {
            case 0: numBoxes = 2; break;
            case 1: numBoxes = 4; break;
            case 2: numBoxes = 6; break;
        }

        // Применяем изменения к игре
        gamePanel.generateNewLevel(numBoxes);
        gameContainer.repaint();
        updateGameStatus();
    }
}