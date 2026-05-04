# Игровая логика

## Основные классы

### Main.java

**Назначение**: Точка входа в приложение.

```java
package game;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SokobanGame().setVisible(true);
        });
    }
}
```

**Ответственность**:

- Запуск приложения в Event Dispatch Thread (EDT)
- Создание и отображение основного окна

### SokobanGame.java

**Назначение**: Основное окно игры

**Наследование**: `JFrame`

**Основные поля**:

- `GamePanel` - игровая панель
- `GameContainer` - контейнер UI
- `ScoreManager` - менеджер рекордов
- `SpriteManager` - менеджер спрайтов

**Основные методы**:

`checkGameState()` - проаеряет условие победы и показывает окно с результатами

```java
private void checkGameState() {
    if (gamePanel.isGameWon()) {
        long timeSeconds = gamePanel.getElapsedTimeSeconds();
        int moves = gamePanel.getMoveCount();
        int difficulty = gamePanel.getCurrentDifficulty();

        gameContainer.showVictoryWindow(timeSeconds, moves, difficulty);
    }
}
```

**Обработка клавиш**:

- `S` - показать таблицу рекордов
- `Ctrl + R` - сгенерировать новый уровень
- `R` - сбросить текущий уровень
- `Z` - отменить ход
- `Стрелки` - движение игрока

### GameContainer.java

**Назначение**: контейнер для игры и UI элементов

**Наследование**: `JPanel`

**Основные поля**:

- `GamePanel` - ссылка на игровую панель
- `UIManager` - управление UI окнами
- `ScoreManager` - менеджер рекордов
- `showScores` - флаг отображения таблицы рекордов
- `showVictory` - флаг отображения окна победы

**Основные методы**:

`showScoresWindow()` - Показывает окно с таблицей рекордов:

- Останавливает игровой таймер
- Создает окно с кнопкой закрытия
- Отрисовывает топ-10 рекордов

`showVictoryWindow(long timeSeconds, int moves, int difficulty)` - Показывает окно победы:

- Сохраняет текущий рекорд
- Показывает статистику прохождения
- Предлагает кнопки "Новый уровень" и "Закрыть"

`paintComponent(Graphics g)` - Переопределенный метод отрисовки:

- Рисует игровую панель
- Если активно окно (рекорды/победа), рисует полупрозрачный оверлей
- Отрисовывает UI элементы

### GamePanel.java

**Назначение**: Основная игровая логика и отрисовка

**Наследование**: `JPanel`

**Константы**:

```java
private static final int TILE_SIZE = 64;  // Размер клетки
private static final int ROWS = 10;       // Количество строк
private static final int COLS = 10;       // Количество столбцов
```

**Основные поля**:

- `TileType[][] grid` - двумерный массив уровня
- `Player player` - объект игрока
- `List<GameBox> boxes` - список ящиков
- `List<Point> targets` - список целевых позиций
- `Stack<GameState> history` - стек состояний для отмены
- `SpriteManager sprites` - менеджер спрайтов
- `long levelStartTime` - время начала уровня
- `int moveCount` - счетчик ходов

**Основные методы**:

`initLevel()` - Инициализирует уровень:

- Создает сетку
- Загружает уровень из массива или генерирует
- Инициализирует игрока и ящики
- Запускает таймер

`movePlayer(int keyCode)` - Обрабатывает движение игрока:

- Определяет направление движения
- Проверяет, нет ли стены на пути
- Если на пути ящик - проверяет возможность его толкнуть
- Сохраняет текущее состояние в стек
- Перемещает игрока (и ящик, если нужно)
- Увеличивает счетчик ходов

`saveState()` - Сохраняет текущее состояние игры в стек:

```java
private void saveState() {
    history.push(new GameState(player.getX(), player.getY(),
                                lastDx, lastDy, boxes));
    if (history.size() > 50) {
        history.remove(0);  // Ограничение размера стека
    }
}
```

`undo()` - Отменяет последний ход:

- Извлекает предыдущее состояние из стека
- Восстанавливает позицию игрока и ящиков

`isGameWon()` - Проверяет условие победы:

```java
public boolean isGameWon() {
    for (GameBox box : boxes) {
        if (!box.onTarget) {
            return false;
        }
    }
    return true;
}
```

`generateNewLevel(int numBoxes)` - Генерирует новый уровень:

- Создает LevelGenerator с указанным количеством ящиков
- Загружает сгенерированный уровень
- Сбрасывает статистику

`paintComponent(Graphics g)` - Отрисовывает игровое поле:

- Рисует фон (пол)
- Рисует стены и цели
- Рисует ящики (с выделением если на цели)
- Рисует игрока

### Взаимодействие классов

SokobanGame

↓ создает

GameContainer

↓ содержит

GamePanel

↓ использует

- Player (сущность)
- GameBox (сущность)
- GameState (сущность)
- SpriteManager (графика)
- LevelGenerator (уровни)
