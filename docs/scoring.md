# Система рекордов

Пакет `score` отвечает за сохранение, загрузку и управление таблицей рекордов игроков. Использует сериализацию Java для хранения данных в файле.

## ScoreRecord (Запись рекорда)

**Файл**: `ScoreRecord.java`

Класс, представляющий одну запись о прохождении уровня.

### Реализуемые интерфейсы

- `Comparable<ScoreRecord>` - для сортировки рекордов
- `Serializable` - для сохранения в файл

### Поля

```java
private String playerName;           // Имя игрока (по умолчанию "User")
private long timeSeconds;            // Время в секундах
private int moves;                   // Количество ходов
private LocalDateTime completedAt;   // Дата и время завершения
private int levelDifficulty;         // Сложность (количество ящиков)
```

### Конструкторы

Базовый конструктор

```java
public ScoreRecord(long timeSeconds, int moves, int levelDifficulty) {
    this("User", timeSeconds, moves, levelDifficulty);
}
```

Полный конструктор

```java
public ScoreRecord(String playerName, long timeSeconds, int moves, int levelDifficulty) {
    this.playerName = playerName;
    this.timeSeconds = timeSeconds;
    this.moves = moves;
    this.levelDifficulty = levelDifficulty;
    this.completedAt = LocalDateTime.now(); // Автоматическая установка времени
}
```

### Основные методы

#### `getFormattedTime()`

Форматирует время в формате MM:SS:

```java
public String getFormattedTime() {
    long minutes = timeSeconds / 60;
    long seconds = timeSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
}
// Пример: 125 секунд → "02:05"
```

#### `getFormattedDate()`

Форматирует дату завершения:

```java
public String getFormattedDate() {
    return completedAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
}
// Пример: "05.04.2026 14:30"
```

#### `compareTo()`

Сравнивает рекорды для сортировки (лучшие первые):

```java
@Override
public int compareTo(ScoreRecord other) {
    // Сначала сравниваем по времени (меньше = лучше)
    if (this.timeSeconds != other.timeSeconds) {
        return Long.compare(this.timeSeconds, other.timeSeconds);
    }
    // Если время одинаковое, сравниваем по ходам (меньше = лучше)
    return Integer.compare(this.moves, other.moves);
}
```

**Приоритет сортировки:**

1. Время (основной критерий)

2. Количество ходов (вторичный критерий)

#### `toString()`

Строковое представление:

```java
@Override
public String toString() {
    return String.format("%s | Время: %s | Ходов: %d | Дата: %s | Сложность: %d",
            playerName, getFormattedTime(), moves, getFormattedDate(), levelDifficulty);
}
```

## ScoreManager (Менеджер рекордов)

**Файл:** `ScoreManager.java`

Управляет коллекцией рекордов: добавление, сортировка, сохранение и загрузка

### Константы и поля

```java
private static final String SCORES_FILE = "scores.dat";
private List<ScoreRecord> records;
```

**Файл сохранений:** `scores.dat` в корневой директории проекта

### Основные методы

#### Конструктор

```java
public ScoreManager() {
    records = new ArrayList<>();
    loadScores(); // Автоматическая загрузка при создании
}
```

#### `addScore()` - Добавление рекорда

```java
public void addScore(long timeSeconds, int moves, int levelDifficulty) {
    ScoreRecord record = new ScoreRecord(timeSeconds, moves, levelDifficulty);
    records.add(record);
    Collections.sort(records); // Сортировка (лучшие первые)
    saveScores();              // Автосохранение
}
```

**Что происходит:**

1. Создается новая запись

2. Добавляется в список

3. Список сортируется (используется compareTo())

4. Сохраняется на диск

#### `getTopScores(int limit)` - Получение лучших

```java
public List<ScoreRecord> getTopScores(int limit) {
    return records.subList(0, Math.min(limit, records.size()));
}
```

Возвращает топ-N рекордов (например, топ-10)

#### `getAllScores()` - Все рекорды

```java
public List<ScoreRecord> getAllScores() {
    return new ArrayList<>(records); // Возвращает копию
}
```

### Сохранение и загрузка

#### `saveScores()` - Сериализация

```java
private void saveScores() {
    try (ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(SCORES_FILE))) {
        oos.writeObject(records);
    } catch (IOException e) {
        System.err.println("Ошибка при сохранении рекордов: " + e.getMessage());
    }
}
```

**Используется:** Java Object Serialization

**Формат:** Бинарный файл `scores.dat`

#### `loadScores()` - Десериализация

```java
@SuppressWarnings("unchecked")
private void loadScores() {
    File file = new File(SCORES_FILE);
    if (!file.exists()) {
        records = new ArrayList<>();
        return;
    }

    try (ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(SCORES_FILE))) {
        records = (List<ScoreRecord>) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
        System.err.println("Ошибка при загрузке рекордов: " + e.getMessage());
        records = new ArrayList<>();
    }
}
```

**Обработка ошибок:**

- Если файл не существует - создается пустой список

- При ошибке чтения - создается пустой список (не crashing)

## Интеграция с игрой

#### В SokobanGame.java

```java
// Создание менеджера
scoreManager = new ScoreManager();

// При победе
private void checkGameState() {
    if (gamePanel.isGameWon()) {
        long timeSeconds = gamePanel.getElapsedTimeSeconds();
        int moves = gamePanel.getMoveCount();
        int difficulty = gamePanel.getCurrentDifficulty();

        gameContainer.showVictoryWindow(timeSeconds, moves, difficulty);
    }
}
```

#### В GameContainer.java

```java
public void showVictoryWindow(long timeSeconds, int moves, int difficulty) {
    // Сохраняем рекорд
    currentRecord = new ScoreRecord(timeSeconds, moves, difficulty);
    scoreManager.addScore(timeSeconds, moves, difficulty);

    // Показываем окно с таблицей
    drawScores(g2d);
}

private void drawScores(Graphics2D g2d) {
    List<ScoreRecord> topScores = scoreManager.getTopScores(10);

    for (int i = 0; i < topScores.size(); i++) {
        ScoreRecord record = topScores.get(i);

        // Выделяем текущий рекорд
        if (showVictory && currentRecord != null &&
            record.getTimeSeconds() == currentRecord.getTimeSeconds() &&
            record.getMoves() == currentRecord.getMoves()) {
            g2d.setColor(new Color(100, 255, 100)); // Зеленый
        } else {
            g2d.setColor(Color.WHITE);
        }

        String text = String.format("%d. %s | %s | Ходов: %d",
                i + 1, record.getPlayerName(),
                record.getFormattedTime(),
                record.getMoves());
        g2d.drawString(text, 80, startY + i * lineHeight);
    }
}
```

### Особенности реализации

1\. **Автосортировка**

Рекорды сортируются автоматически при каждом добавлении.

2\. **Persistent storage**

Рекорды сохраняются между запусками игры

3\. **Обработка ошибок**

При повреждении файла игра не падает, а создает новый пустой список

4\. **Подсветка текущего**

В таблице рекордов текущий результат подсвечивается зеленым цветом

5\. **Два критерия победы**

- Основное: минимальное время
- Вторичное: минимальное количество ходов
