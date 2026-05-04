# Игровой цикл и архитектура потока

## Тип архитектуры: Event-Driven

В отличие от классических игр, использующих бесконечный цикл отрисовки (60 FPS), эта игра построена на **событийной модели** Java Swing.

Весь код выполняется в специальном потоке **EDT (Event Dispatch Thread)**

### Обработка собятия

Игра находится в состоянии "сна" и ждёт действий пользователя. Когда игрок нажимает команду `KeyAdapter` в классе `SokobanGame`:

```java
addKeyListener(new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {
        // Логика обработки нажатий
    }
});
```

### Обновление состояния (Update)

Метод `gamePanel.movePlayer(keyCode)` изменяет координаты объектов в памяти (Player, GameBox)

### Отрисовка (Render)

После изменения данных вызывается метод `repaint()`.

Swing сам решает, когда перерисовать экран, и вызывает `paintComponent()`

Пользователь -> KeyPressed -> movePlayer() -> repaint() -> paintComponent()

### Управление временем

Поскольку постоянного цикла нет, время измеряется не "тиками" цикла, а системными часами:

```java
long levelStartTime = System.currentTimeMillis();

// Расчет прошедшего времени
public long getElapsedTimeSeconds() {
    return (System.currentTimeMillis() - levelStartTime) / 1000;
}
```

Это позволяет игре не нагружать процессор, когда игрок думает над ходом
