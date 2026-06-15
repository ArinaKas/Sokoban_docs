package graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class SpriteManager {
    private BufferedImage playerForward; // смотрит вперед (вниз)
    private BufferedImage playerBack;    // смотрит назад (вверх)
    private BufferedImage playerRight;   // смотрит вправо
    private BufferedImage playerLeft;    // смотрит влево
    private BufferedImage wallSprite;
    private BufferedImage boxSprite;
    private BufferedImage boxOnTargetSprite; // ящик на цели
    private BufferedImage targetSprite;
    private BufferedImage floorSprite;

    // Текущий спрайт игрока (для анимации)
    private BufferedImage currentPlayerSprite;

    public SpriteManager() {
        loadSprites();
        // По умолчанию игрок смотрит вперед
        currentPlayerSprite = playerForward;
    }

    private void loadSprites() {
        try {
            // Загрузка спрайтов игрока с разными направлениями
            playerForward = loadImage("resources/images/player1.png");
            playerBack = loadImage("resources/images/player2.png");
            playerRight = loadImage("resources/images/player3.png");
            playerLeft = loadImage("resources/images/player4.png");

            // Загрузка остальных спрайтов
            wallSprite = loadImage("resources/images/wall.png");
            boxSprite = loadImage("resources/images/box.png");
            boxOnTargetSprite = loadImage("resources/images/box_ontarget.png"); // опционально
            targetSprite = loadImage("resources/images/target.png");
            floorSprite = loadImage("resources/images/floor.png");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Не удалось загрузить спрайты!");
            // Если не загрузились, используем первый доступный спрайт
            if (playerForward == null && playerBack == null) {
                System.err.println("Убедитесь, что файлы спрайтов находятся в resources/images/");
            }
        }
    }

    public BufferedImage loadImage(String path) throws IOException {
        URL resource = getClass().getClassLoader().getResource(path);
        if (resource == null) {
            throw new IOException("Ресурс не найден: " + path);
        }
        return ImageIO.read(resource);
    }

    // Метод для смены спрайта игрока в зависимости от направления движения
    public void setPlayerDirection(int dx, int dy) {
        if (dy == -1) { // вверх
            currentPlayerSprite = playerBack;
        } else if (dy == 1) { // вниз
            currentPlayerSprite = playerForward;
        } else if (dx == -1) { // влево
            currentPlayerSprite = playerLeft;
        } else if (dx == 1) { // вправо
            currentPlayerSprite = playerRight;
        }
    }

    // Геттеры для спрайтов
    public BufferedImage getPlayerSprite() {
        return currentPlayerSprite != null ? currentPlayerSprite : playerForward;
    }

    public BufferedImage getWallSprite() { return wallSprite; }
    public BufferedImage getBoxSprite() { return boxSprite; }
    public BufferedImage getBoxOnTargetSprite() { return boxOnTargetSprite; }
    public BufferedImage getTargetSprite() { return targetSprite; }
    public BufferedImage getFloorSprite() { return floorSprite; }
}