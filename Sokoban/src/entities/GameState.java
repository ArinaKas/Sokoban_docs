package entities;

import java.util.ArrayList;
import java.util.List;
import game.GamePanel;

public class GameState {
    public int playerX, playerY;
    public int playerDx, playerDy;
    public List<BoxState> boxStates;

    public GameState(int playerX, int playerY, int playerDx, int playerDy, List<GameBox> boxes) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerDx = playerDx;
        this.playerDy = playerDy;
        this.boxStates = new ArrayList<>();
        for (GameBox box : boxes) {
            this.boxStates.add(new BoxState(box.x, box.y, box.onTarget));
        }
    }

    public void restore(GamePanel panel) {
        panel.setPlayerPosition(playerX, playerY);
        if (playerDx != 0 || playerDy != 0) {
            panel.setPlayerDirection(playerDx, playerDy);
        }

        List<GameBox> boxes = panel.getBoxes();
        for (int i = 0; i < boxStates.size() && i < boxes.size(); i++) {
            BoxState state = boxStates.get(i);
            GameBox box = boxes.get(i);
            box.setPosition(state.x, state.y);
            box.onTarget = state.onTarget;
        }
    }
}
