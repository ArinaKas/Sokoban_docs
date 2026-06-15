package entities;

public class BoxState {
    public int x, y;
    public boolean onTarget;

    public BoxState(int x, int y, boolean onTarget) {
        this.x = x;
        this.y = y;
        this.onTarget = onTarget;
    }
}
