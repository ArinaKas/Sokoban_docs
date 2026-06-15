package level;

import java.awt.*;
import java.util.*;

public class LevelGenerator {
    private static final Random random = new Random();
    
    private int width;
    private int height;
    private int numBoxes;
    private Node[][] nodes;
    private java.util.List<BoxData> boxes;
    private java.util.List<ButtonData> buttons;
    private int playerX, playerY;
    private int playerStartX, playerStartY;
    private int solveCounter;
    private boolean trash = false;
    private int[][] grid;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 10;
    
    public LevelGenerator(int width, int height, int numBoxes) {
        this.width = width;
        this.height = height;
        this.numBoxes = numBoxes;
        this.solveCounter = numBoxes;
        this.nodes = new Node[width][height];
        this.boxes = new ArrayList<>();
        this.buttons = new ArrayList<>();
        this.grid = new int[height][width];
        
        initializeNodes();
    }
    
    private void initializeNodes() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nodes[x][y] = new Node(x, y);
                nodes[x][y].wall = true;
            }
        }
    }
    
    public int[][] generateLevel() {
        // Создаем границы
        createBorders();
        
        // Размещаем объекты
        placeObjects();
        
        // Генерируем пути (основной алгоритм)
        generatePaths();
        
        // Если уровень невалидный, пробуем снова
        if (trash && retryCount < MAX_RETRIES) {
            System.out.println("Level generation failed, retrying... (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
            retryCount++;
            initializeNodes();
            boxes.clear();
            buttons.clear();
            trash = false;
            solveCounter = numBoxes;
            return generateLevel();
        } else if (trash) {
            // Если не получилось после MAX_RETRIES попыток, используем простой уровень
            System.out.println("Using fallback simple level");
            createSimpleLevel();
        }
        
        // Конвертируем в сетку
        convertToGrid();
        
        return grid;
    }
    
    private void createBorders() {
        for (int x = 0; x < width; x++) {
            nodes[x][0].wall = true;
            nodes[x][height - 1].wall = true;
        }
        for (int y = 0; y < height; y++) {
            nodes[0][y].wall = true;
            nodes[width - 1][y].wall = true;
        }
    }
    
    private void placeObjects() {
        java.util.List<Node> allowedSpots = defineAllowedSpots();
        
        // Размещаем кнопки (цели)
        for (int i = 0; i < numBoxes; i++) {
            Point pos = randomSpot(allowedSpots);
            if (pos != null) {
                buttons.add(new ButtonData(pos.x, pos.y));
            }
        }
        
        // Размещаем ящики
        for (int i = 0; i < numBoxes && i < buttons.size(); i++) {
            Point pos = randomSpot(allowedSpots);
            if (pos != null) {
                boxes.add(new BoxData(pos.x, pos.y, buttons.get(i)));
                nodes[pos.x][pos.y].hasBox = true;
            }
        }
        
        // Размещаем игрока
        Point pos = randomSpot(allowedSpots);
        if (pos == null && !buttons.isEmpty()) {
            pos = new Point(buttons.get(0).x, buttons.get(0).y);
        }
        if (pos != null) {
            setPlayerPos(pos.x, pos.y);
            playerStartX = playerX;
            playerStartY = playerY;
        }
    }
    
    private java.util.List<Node> defineAllowedSpots() {
        java.util.List<Node> spots = new ArrayList<>();
        for (int x = 2; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {
                spots.add(nodes[x][y]);
            }
        }
        return spots;
    }
    
    private Point randomSpot(java.util.List<Node> allowedSpots) {
        if (allowedSpots.isEmpty()) return null;
        
        int index = random.nextInt(allowedSpots.size());
        Node node = allowedSpots.get(index);
        allowedSpots.remove(index);
        
        int x = node.x;
        int y = node.y;
        nodes[x][y].wall = false;
        
        // Проверяем блокировку ящиками
        if (isBlockaded(x, y)) {
            return randomSpot(allowedSpots);
        }
        
        return new Point(x, y);
    }
    
    private boolean isBlockaded(int x, int y) {
        if (x + 1 < width && nodes[x + 1][y].hasBox) {
            if ((y + 1 < height && nodes[x + 1][y + 1].hasBox && nodes[x][y + 1].hasBox) ||
                (y - 1 >= 0 && nodes[x + 1][y - 1].hasBox && nodes[x][y - 1].hasBox)) {
                return true;
            }
        }
        if (x - 1 >= 0 && nodes[x - 1][y].hasBox) {
            if ((y - 1 >= 0 && nodes[x - 1][y - 1].hasBox && nodes[x][y - 1].hasBox) ||
                (y + 1 < height && nodes[x - 1][y + 1].hasBox && nodes[x][y + 1].hasBox)) {
                return true;
            }
        }
        return false;
    }
    
    private void setPlayerPos(int x, int y) {
        this.playerX = x;
        this.playerY = y;
    }
    
    // Основной алгоритм генерации путей
    private void generatePaths() {
        int steps = 0;
        
        // Создаем копии ящиков для решения
        java.util.List<BoxData> ghostBoxes = copyBoxes();
        
        // Двигаем призрачные ящики к целям
        while (solveCounter > 0) {
            // Вычисляем пути от всех ящиков к их целям
            java.util.List<Pathfinder.PathResult> boxPaths = calculateBoxPaths(ghostBoxes);
            
            // Вычисляем пути игрока ко всем ящикам и выбираем лучший
            Object[] playerPathsResult = calculatePlayerPaths(ghostBoxes, boxPaths);
            @SuppressWarnings("unchecked")
            java.util.List<Pathfinder.PathResult> playerPaths = (java.util.List<Pathfinder.PathResult>) playerPathsResult[0];
            int bestPath = (int) playerPathsResult[1];
            
            if (bestPath == -1) {
                trash = true;
                break;
            }
            
            Pathfinder.PathResult playerPath = playerPaths.get(bestPath);
            Pathfinder.PathResult boxPath = boxPaths.get(bestPath);
            
            // Удаляем стены на пути игрока
            for (Node node : playerPath.path) {
                node.wall = false;
                if (node.occupied) {
                    trash = true;
                }
            }
            
            // Двигаем ящик в направлении решения
            BoxData thisBox = ghostBoxes.get(bestPath);
            if (boxPath.path.isEmpty()) {
                trash = true;
                break;
            }
            
            Node currentNode = boxPath.path.get(0);
            int diffX = currentNode.x - thisBox.x;
            int diffY = currentNode.y - thisBox.y;
            int stop = 0;
            
            // Если путь ящика длиннее 1, толкаем до поворота
            if (boxPath.path.size() > 1) {
                for (int i = 1; i < boxPath.path.size(); i++) {
                    Node nextNode = boxPath.path.get(i);
                    if (diffX == nextNode.x - currentNode.x && diffY == nextNode.y - currentNode.y) {
                        currentNode = nextNode;
                    } else {
                        stop = i - 1;
                        break;
                    }
                }
            }
            
            // Удаляем стены на пути ящика
            for (int i = 0; i <= stop && i < boxPath.path.size(); i++) {
                boxPath.path.get(i).wall = false;
            }
            
            // Устанавливаем новые позиции игрока и ящика
            nodes[thisBox.x][thisBox.y].occupied = false;
            thisBox.setPosition(boxPath.path.get(stop).x, boxPath.path.get(stop).y);
            nodes[thisBox.x][thisBox.y].occupied = true;
            setPlayerPos(thisBox.x - diffX, thisBox.y - diffY);
            
            // Проверяем, на цели ли ящик
            if (thisBox.x == thisBox.solveButton.x && thisBox.y == thisBox.solveButton.y) {
                thisBox.placed = true;
                solveCounter--;
                ghostBoxes.remove(bestPath);
            }
            
            steps++;
            if (steps > 4000) {
                trash = true;
                break;
            }
        }
        
        // Возвращаем игрока на стартовую позицию
        setPlayerPos(playerStartX, playerStartY);
    }
    
    private java.util.List<BoxData> copyBoxes() {
        java.util.List<BoxData> newBoxes = new ArrayList<>();
        for (BoxData box : boxes) {
            newBoxes.add(new BoxData(box.x, box.y, box.solveButton));
            nodes[box.x][box.y].occupied = true;
            nodes[box.x][box.y].used = false;
        }
        return newBoxes;
    }
    
    private java.util.List<Pathfinder.PathResult> calculateBoxPaths(java.util.List<BoxData> ghostBoxes) {
        java.util.List<Pathfinder.PathResult> boxPaths = new ArrayList<>();
        for (BoxData box : ghostBoxes) {
            nodes[box.x][box.y].occupied = false;
            Pathfinder pathfinder = new Pathfinder(nodes, box.x, box.y, 
                                                   box.solveButton.x, box.solveButton.y);
            boxPaths.add(pathfinder.findPath(true));
            nodes[box.x][box.y].occupied = true;
        }
        return boxPaths;
    }
    
    private Object[] calculatePlayerPaths(java.util.List<BoxData> ghostBoxes, 
                                         java.util.List<Pathfinder.PathResult> boxPaths) {
        java.util.List<Pathfinder.PathResult> playerPaths = new ArrayList<>();
        int bestPath = -1;
        double lowestCost = Double.MAX_VALUE;
        
        for (int i = 0; i < ghostBoxes.size(); i++) {
            BoxData box = ghostBoxes.get(i);
            if (boxPaths.get(i).path.isEmpty()) continue;
            
            Node firstNode = boxPaths.get(i).path.get(0);
            int newX = box.x;
            int newY = box.y;
            
            // Определяем позицию игрока для толкания ящика
            if (firstNode.x == box.x + 1) {
                newX -= 1;
            } else if (firstNode.x == box.x - 1) {
                newX += 1;
            } else if (firstNode.y == box.y + 1) {
                newY -= 1;
            } else {
                newY += 1;
            }
            
            Pathfinder pathfinder = new Pathfinder(nodes, playerX, playerY, newX, newY);
            Pathfinder.PathResult result = pathfinder.findPath(false);
            playerPaths.add(result);
            
            if (result.cost < lowestCost) {
                lowestCost = result.cost;
                bestPath = i;
            }
        }
        
        return new Object[]{playerPaths, bestPath};
    }
    
    private void convertToGrid() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (nodes[x][y].wall) {
                    grid[y][x] = LevelCode.WALL;
                } else {
                    boolean isButton = false;
                    boolean isBox = false;
                    
                    for (ButtonData button : buttons) {
                        if (button.x == x && button.y == y) {
                            grid[y][x] = LevelCode.TARGET;
                            isButton = true;
                            break;
                        }
                    }
                    
                    if (!isButton) {
                        for (BoxData box : boxes) {
                            if (box.x == x && box.y == y) {
                                grid[y][x] = LevelCode.BOX;
                                isBox = true;
                                break;
                            }
                        }
                    }
                    
                    if (!isButton && !isBox) {
                        if (x == playerStartX && y == playerStartY) {
                            grid[y][x] = LevelCode.PLAYER;
                        } else {
                            grid[y][x] = LevelCode.FLOOR;
                        }
                    }
                }
            }
        }
    }
    
    public int[][] getGrid() {
        return grid;
    }
    
    // Создает простой гарантированно проходимый уровень
    private void createSimpleLevel() {
        // Очищаем все
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nodes[x][y].wall = true;
            }
        }
        
        boxes.clear();
        buttons.clear();
        
        // Создаем простую комнату
        for (int x = 2; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {
                nodes[x][y].wall = false;
            }
        }
        
        // Размещаем объекты в безопасных позициях
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Кнопки
        buttons.add(new ButtonData(centerX - 2, centerY));
        buttons.add(new ButtonData(centerX + 2, centerY));
        
        // Ящики
        boxes.add(new BoxData(centerX - 1, centerY, buttons.get(0)));
        boxes.add(new BoxData(centerX + 1, centerY, buttons.get(1)));
        
        // Игрок
        playerStartX = centerX;
        playerStartY = centerY + 2;
    }
}
