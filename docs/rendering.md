# Система рендеринга и UI

### Архитектура отрисовки

Игра использует двухуровневую систему отрисовки:

1. **Игровое поле** - спрайты объектов игры
2. **UI слой** - окна, кнопки, таблицы рекордов

### Загружаемые спрайты

#### Игрок (4 направления):

- `player1.png` - смотрит вниз (вперед)
- `player2.png` - смотрит вверх (назад)
- `player3.png` - смотрит вправо
- `player4.png` - смотрит влево

#### Окружение:

- `wall.png` - стена
- `box.png` - ящик
- `box_ontarget.png` - ящик на цели (опционально)
- `target.png` - целевая позиция
- `floor.png` - пол

### Основные методы

#### `loadImage(String path)`

Загружает изображение из ресурсов:

```java
public BufferedImage loadImage(String path) throws IOException {
    URL resource = getClass().getClassLoader().getResource(path);
    if (resource == null) {
        throw new IOException("Ресурс не найден: " + path);
    }
    return ImageIO.read(resource);
}
```

#### `setPlayerDirection(int dx, int dy)`

Выбирает правильный спрайт игрока в зависимости от направления:

```java
if (dy == -1) { // вверх
    currentPlayerSprite = playerBack;
} else if (dy == 1) { // вниз
    currentPlayerSprite = playerForward;
} else if (dx == -1) { // влево
    currentPlayerSprite = playerLeft;
} else if (dx == 1) { // вправо
    currentPlayerSprite = playerRight;
}
```

**Особенности:**

- Singleton - подобный паттерн (один экземпляр на всю игру)

- Загрузка происходит один раз в конструкторе

- Fallback на null если спрайт не найден

## UIManager (Менеджер UI)

**Файл:** `ui/UIManager.java`

Управляет окнами интерфейса и их отрисовкой

**Поля:**

- `List<UIWindow> windows` - список активных окон

- `BufferedImage windowSprite` - спрайт фона окна

- `BufferedImage crossSprite` - спрайт крестика закрытия

- `BufferedImage buttonSprite` - спрайт кнопки

**Основные методы:**

#### `createWindow(int x, y)`

Создает новое окно и добавляет его в список:

```java
public UIWindow createWindow(int x, int y) {
    UIWindow window = new UIWindow(x, y, windowSprite, crossSprite);
    windows.add(window);
    return window;
}
```

#### `handleMouseClick(int px, int py)`

Обрабатывает клик мыши, передавая его верхнему окну:

```java
// Обрабатываем окна в обратном порядке (сверху вниз)
for (int i = windows.size() - 1; i >= 0; i--) {
    UIWindow window = windows.get(i);
    if (window.contains(px, py)) {
        window.handleMouseClick(px, py);
        break;
    }
}
```

## UIWindow (Окно интерфейса)

**Файл:** `ui/UIWindow.java`

Класс, представляющий модальное окно UI

**Параметры:**

- `width = 540` - ширина окна

- `height = 600` - высота окна

- `TITLE_HEIGHT = 50` - высота заголовка

- `CROSS_SIZE = 32` - размер крестика

- `BUTTON_WIDTH = 128` - ширина кнопки

- `PADDING = 10` - отступы

**Основные методы:**

#### `addButton(String label, BufferedImage sprite, Runnable onClick)`

Добавляет кнопку в нижнюю часть окна с автоматическим позиционированием:

```java
int buttonCount = buttons.size();
int totalButtonWidth = (buttonCount + 1) * BUTTON_WIDTH + buttonCount * PADDING;
int startX = x + width - totalButtonWidth - PADDING;
int buttonY = y + height - BUTTON_HEIGHT - PADDING;
int buttonX = startX + buttonCount * (BUTTON_WIDTH + PADDING);
```

#### `draw(Graphics2D g2d)`

**Отрисовывает:**

1. Фон окна (спрайт или fallback цвет)

2. Заголовок

3. Крестик закрытия (спрайт или вручную нарисованный)

4. Все кнопки

## UIButton (Кнопка UI)

**Файл:** `ui/UIButton.java`

Интерактивная кнопка с hover-эффектом

**Поля:**

- `int x, y` - позиция

- `int width = 128, height = 32` - размеры

- `String label` - текст кнопки

- `BufferedImage sprite` - спрайт фона

- `boolean hovered` - состояние наведения

- `Runnable onClick` - обработчик клика

#### Метод `draw(Graphics2D g2d)`

Отрисовывает кнопку:

1. Если есть спрайт - рисует его

2. Иначе - рисует цветной прямоугольник (синий, светлее при наведении)

3. Поверх - текст по центру

**Hover эффект:**

```java
g2d.setColor(hovered ? new Color(100, 150, 255) : new Color(70, 120, 200));
```

## Процесс отрисовки (GameContainer)

**Файл:** `game/GameContainer.java`

**Метод** `paintComponent(Graphics g)` реализует многослойную отрисовку:

**Слои отрисовки**

```java
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;

    // 1. Рисуем игровое поле
    gamePanel.paintComponent(g2d);

    // 2. Если активно UI - рисуем оверлей
    if (showScores || showVictory) {
        // Полупрозрачный черный фон
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 3. Рисуем UI окна
        uiManager.drawAll(g2d);

        // 4. Рисуем таблицу рекордов
        drawScores(g2d);
    }
}
```

**Оптимизация рендеринга**

Включено сглаживание:

```java
g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
```

### Особенности реализации

1\. Отсутствие постоянного цикла отрисовки

Игра перерисовывается только при событиях:

- Движение игрока
- Открытие/закрытие UI
- Наведение мыши на кнопки

2\. Fallback графика

Если спрайт не загружен, рисуется цветная замена:

- Кнопки - синие прямоугольники
- Окна - серые прямоугольники
- Крестик - линии

3\. Масштабирование

Все спрайты масштабируются до TILE_SIZE = 64px при отрисовке.
