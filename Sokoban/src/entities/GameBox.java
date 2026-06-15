package entities;

public class GameBox {
    public int x, y;
    public boolean onTarget;

    public GameBox(int x, int y) {
        this.x = x;
        this.y = y;
        this.onTarget = false;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
