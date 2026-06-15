package level;

public class BoxData {
    public int x, y;
    public boolean placed = false;
    public ButtonData solveButton;
    
    public BoxData(int x, int y, ButtonData button) {
        this.x = x;
        this.y = y;
        this.solveButton = button;
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
