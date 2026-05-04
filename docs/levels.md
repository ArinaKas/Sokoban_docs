# Система уровней

Пакет `level` отвечает за представление и генерацию игровых уровней. Включает в себя как константы для кодирования уровня, так и сложный алгоритм процедурной генерации.

## LevelCode (Константы уровня)

**Файл**: `LevelCode.java`

Константы для представления элементов уровня в виде целочисленного массива `int[][]`.

### Константы

| Константа | Значение | Описание                  |
| --------- | -------- | ------------------------- |
| `FLOOR`   | 0        | Пол (проходимая клетка)   |
| `WALL`    | 1        | Стена (непроходимая)      |
| `TARGET`  | 2        | Целевая позиция для ящика |
| `BOX`     | 3        | Ящик                      |
| `PLAYER`  | 4        | Игрок                     |

### Использование

Уровень хранится как двумерный массив:

```java
int[][] level = {
    {1, 1, 1, 1, 1},
    {1, 4, 0, 3, 1},
    {1, 0, 0, 0, 1},
    {1, 0, 3, 2, 1},
    {1, 1, 1, 1, 1}
};
```

## LevelGenerator (Генератор уровней)

**Файл:** `LevelGenerator.java`

Класс для процедурной генерации гарантированно проходимых уровней

### Параметры генерации

```java
private int width;           // Ширина уровня (10)
private int height;          // Высота уровня (10)
private int numBoxes;        // Количество ящиков
private int retryCount = 0;  // Счетчик попыток
private static final int MAX_RETRIES = 10;
```

### Алгоритм генерации

Генератор использует обратный алгоритм решения (reverse solving):

1. **Инициализация:** Создается поле, полностью заполненное стенами

2. **Размещение целей:** Случайным образом размещаются целевые позиции (кнопки)

3. **Размещение ящиков:** Рядом с целями размещаются ящики

4. **Размещение игрока:** Игрок размещается в свободной позиции

5. **Построение путей:** Алгоритм "прорубает" стены, создавая проходимые пути

6. **Проверка решаемости:** Если уровень нерешаем, генерация повторяется

### Основные методы

#### `generateLevel()`

Основной метод генерации:

```java
public int[][] generateLevel() {
    createBorders();           // Создаем границы
    placeObjects();            // Размещаем объекты
    generatePaths();           // Генерируем пути

    // Если уровень невалидный, пробуем снова
    if (trash && retryCount < MAX_RETRIES) {
        retryCount++;
        initializeNodes();
        return generateLevel(); // Рекурсивный вызов
    }

    convertToGrid();           // Конвертируем в сетку
    return grid;
}
```

#### `generatePaths()`

Основной алгоритм. Использует "призрачные" ящики для поиска решения:

```java
private void generatePaths() {
    // Создаем копии ящиков для решения
    List<BoxData> ghostBoxes = copyBoxes();

    while (solveCounter > 0) {
        // 1. Вычисляем пути от всех ящиков к их целям
        List<PathResult> boxPaths = calculateBoxPaths(ghostBoxes);

        // 2. Вычисляем пути игрока ко всем ящикам
        Object[] playerPathsResult = calculatePlayerPaths(ghostBoxes, boxPaths);

        // 3. Выбираем лучший путь (минимальная стоимость)
        int bestPath = findBestPath(playerPathsResult);

        // 4. Удаляем стены на пути игрока
        for (Node node : playerPath.path) {
            node.wall = false;
        }

        // 5. Двигаем ящик к цели
        moveGhostBox(ghostBoxes.get(bestPath));

        // 6. Проверяем, достигнута ли цель
        if (boxOnTarget) {
            solveCounter--;
        }
    }
}
```

#### `isBlockaded()`

Проверяет, не заблокирован ли ящик другими ящиками:

```java
private boolean isBlockaded(int x, int y) {
    // Проверяет угловые позиции вокруг ящика
    // Если ящик окружен другими ящиками по диагонали - блокировка
    if (x + 1 < width && nodes[x + 1][y].hasBox) {
        if ((y + 1 < height && nodes[x + 1][y + 1].hasBox && nodes[x][y + 1].hasBox) ||
            (y - 1 >= 0 && nodes[x + 1][y - 1].hasBox && nodes[x][y - 1].hasBox)) {
            return true;
        }
    }
    // ... аналогично для других направлений
    return false;
}
```

#### `convertToGrid()`

Преобразует внутреннее представление (Node) в массив LevelCode:

```java
private void convertToGrid() {
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            if (nodes[x][y].wall) {
                grid[y][x] = LevelCode.WALL;
            } else if (isButton(x, y)) {
                grid[y][x] = LevelCode.TARGET;
            } else if (isBox(x, y)) {
                grid[y][x] = LevelCode.BOX;
            } else if (isPlayer(x, y)) {
                grid[y][x] = LevelCode.PLAYER;
            } else {
                grid[y][x] = LevelCode.FLOOR;
            }
        }
    }
}
```

#### `createSimpleLevel()`

Если генерация не удалась после MAX_RETRIES попыток, создается простой гарантированно проходимый уровень:

```java
private void createSimpleLevel() {
    // Создает простую комнату 6x6 в центре поля
    // Размещает 2 ящика и 2 цели
    // Игрок размещается рядом
}
```

## Вспомогательные классы

#### Node (внутренний класс)

Представляет клетку поля:

```java
class Node {
    int x, y;
    boolean wall = true;      // Стена
    boolean hasBox = false;   // Ящик
    boolean occupied = false; // Занята
    boolean used = false;     // Использована
}
```

#### BoxData (внутренний класс)

Данные о ящике:

```java
class BoxData {
    int x, y;
    ButtonData solveButton;  // Цель для этого ящика
    boolean placed = false;  // На цели ли

    void setPosition(int x, int y) { ... }
}
```

#### ButtonData (внутренний класс)

Данные о цели:

```java
class ButtonData {
    int x, y;
}
```

#### Pathfinder (внешний класс)

Алгоритм поиска пути (A\* или BFS):

```java
Pathfinder pathfinder = new Pathfinder(nodes, startX, startY, endX, endY);
PathResult result = pathfinder.findPath(true);
```

## Особенности генерации

1. Гарантированная решаемость: Уровень генерируется "с конца" - от решения к начальной позиции

2. Ограничение попыток: Максимум 10 попыток, затем fallback

3. Проверка блокировок: Ящики не могут быть размещены так, чтобы заблокировать друг друга

4. Оптимизация путей: Игрок и ящики двигаются по кратчайшим путям

## Взаимодействие с GamePanel

```java
// В GamePanel.java
LevelGenerator generator = new LevelGenerator(COLS, ROWS, 2);
int[][] level = generator.generateLevel();
loadLevelFromArray(level);
```

Генератор возвращает готовый массив, который загружается в игру через `loadLevelFromArray()`
