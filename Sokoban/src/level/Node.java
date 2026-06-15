package level;

public class Node {
    public int x, y;
    public boolean wall = true;
    public boolean occupied = false;
    public boolean hasBox = false;
    public boolean used = false;
    public boolean checked = false;
    public boolean closed = false;
    
    public Node parent = null;
    public double cost = 0;
    public double f = 0; // f = cost + heuristic
    
    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void reset() {
        parent = null;
        cost = 0;
        f = 0;
        checked = false;
        closed = false;
    }
}
