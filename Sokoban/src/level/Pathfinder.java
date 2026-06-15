package level;

import java.util.*;

public class Pathfinder {
    private static final double WALL_COST = 100;
    private static final double PATH_COST = 1;
    private static final double PLAYER_PATH_COST = -1;
    private static final double BOX_COST = 10000;
    
    private Node[][] nodes;
    private int startX, startY, endX, endY;
    private List<Node> open;
    private List<Node> closed;
    
    public Pathfinder(Node[][] grid, int startX, int startY, int endX, int endY) {
        this.nodes = grid;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.open = new ArrayList<>();
        this.closed = new ArrayList<>();
    }
    
    public PathResult findPath(boolean isBox) {
        open.clear();
        closed.clear();
        
        Node startNode = nodes[startX][startY];
        startNode.cost = 0;
        startNode.f = heuristic(startX, startY);
        open.add(startNode);
        
        while (!open.isEmpty()) {
            Node current = open.remove(0);
            
            if (current.x == endX && current.y == endY) {
                open.add(current);
                return reconstructPath(current);
            }
            
            current.closed = true;
            closed.add(current);
            
            checkNeighbor(current.x + 1, current.y, current, isBox);
            checkNeighbor(current.x - 1, current.y, current, isBox);
            checkNeighbor(current.x, current.y + 1, current, isBox);
            checkNeighbor(current.x, current.y - 1, current, isBox);
        }
        
        System.out.println("No path found");
        return new PathResult(new ArrayList<>(), Double.MAX_VALUE);
    }
    
    private void checkNeighbor(int x, int y, Node parent, boolean isBox) {
        if (!isInBounds(x, y)) return;
        
        Node neighbor = nodes[x][y];
        
        if (!neighbor.closed && !neighbor.checked) {
            neighbor.cost = calculateCost(neighbor, parent, isBox);
            neighbor.f = neighbor.cost + heuristic(x, y);
            neighbor.parent = parent;
            neighbor.checked = true;
            addToSortedList(open, neighbor);
        } else if (!neighbor.closed) {
            double cost = calculateCost(neighbor, parent, isBox);
            if (cost < neighbor.cost && parent.parent != null) {
                neighbor.cost = cost;
                neighbor.f = neighbor.cost + heuristic(x, y);
                neighbor.parent = parent;
            }
        }
    }
    
    private double calculateCost(Node node, Node parent, boolean isBox) {
        double tempCost = 0;
        
        if (node.occupied) {
            tempCost = parent.cost + BOX_COST;
        } else {
            if (isBox) {
                tempCost = node.wall ? parent.cost + WALL_COST : parent.cost + PATH_COST;
            } else {
                tempCost = node.wall ? parent.cost + WALL_COST : parent.cost + PLAYER_PATH_COST;
            }
        }
        
        // Если путь рассчитывается для ящика, нужно учесть путь игрока
        // Игрок должен обойти ящик при смене направления
        if (isBox && parent.parent != null) {
            double cost1 = 0;
            double cost2 = 0;
            
            if (node.x - 1 == parent.x && node.x - 2 != parent.parent.x) {
                // Случай 1: узел справа от родителя
                if (node.y - 1 == parent.parent.y) {
                    // Случай 1.1: узел справа-сверху от parent.parent
                    cost1 = nodeCost(node.x - 2, node.y) + nodeCost(node.x - 2, node.y - 1);
                    cost2 = nodeCost(node.x, node.y - 1) + nodeCost(node.x, node.y + 1) + 
                            nodeCost(node.x - 1, node.y + 1) + nodeCost(node.x - 2, node.y + 1) + 
                            nodeCost(node.x - 2, node.y);
                } else {
                    // Случай 1.2: узел справа-снизу от parent.parent
                    cost1 = nodeCost(node.x - 2, node.y) + nodeCost(node.x - 2, node.y + 1);
                    cost2 = nodeCost(node.x, node.y - 1) + nodeCost(node.x, node.y + 1) + 
                            nodeCost(node.x - 1, node.y - 1) + nodeCost(node.x - 2, node.y - 1) + 
                            nodeCost(node.x - 2, node.y);
                }
            } else if (node.x + 1 == parent.x && node.x + 2 != parent.parent.x) {
                // Случай 2: узел слева от родителя
                if (node.y - 1 == parent.parent.y) {
                    // Случай 2.1: узел слева-сверху от parent.parent
                    cost1 = nodeCost(node.x + 2, node.y) + nodeCost(node.x + 2, node.y - 1);
                    cost2 = nodeCost(node.x, node.y - 1) + nodeCost(node.x, node.y + 1) + 
                            nodeCost(node.x + 1, node.y + 1) + nodeCost(node.x + 2, node.y + 1) + 
                            nodeCost(node.x + 2, node.y);
                } else {
                    // Случай 2.2: узел слева-снизу от parent.parent
                    cost1 = nodeCost(node.x + 2, node.y) + nodeCost(node.x + 2, node.y + 1);
                    cost2 = nodeCost(node.x, node.y - 1) + nodeCost(node.x, node.y + 1) + 
                            nodeCost(node.x + 1, node.y - 1) + nodeCost(node.x + 2, node.y - 1) + 
                            nodeCost(node.x + 2, node.y);
                }
            } else if (node.y - 1 == parent.y && node.y - 2 != parent.parent.y) {
                // Случай 3: узел выше родителя
                if (node.x - 1 == parent.parent.x) {
                    // Случай 3.1: узел справа-сверху от parent.parent
                    cost1 = nodeCost(node.x, node.y - 2) + nodeCost(node.x - 1, node.y - 2);
                    cost2 = nodeCost(node.x - 1, node.y) + nodeCost(node.x + 1, node.y) + 
                            nodeCost(node.x + 1, node.y - 1) + nodeCost(node.x + 1, node.y - 2) + 
                            nodeCost(node.x, node.y - 2);
                } else {
                    // Случай 3.2: узел слева-сверху от parent.parent
                    cost1 = nodeCost(node.x, node.y - 2) + nodeCost(node.x + 1, node.y - 2);
                    cost2 = nodeCost(node.x - 1, node.y) + nodeCost(node.x + 1, node.y) + 
                            nodeCost(node.x - 1, node.y - 1) + nodeCost(node.x - 1, node.y - 2) + 
                            nodeCost(node.x, node.y - 2);
                }
            } else if (node.y + 1 == parent.y && node.y + 2 != parent.parent.y) {
                // Случай 4: узел ниже родителя
                if (node.x - 1 == parent.parent.x) {
                    // Случай 4.1: узел справа-снизу от parent.parent
                    cost1 = nodeCost(node.x, node.y + 2) + nodeCost(node.x - 1, node.y + 2);
                    cost2 = nodeCost(node.x - 1, node.y) + nodeCost(node.x + 1, node.y) + 
                            nodeCost(node.x + 1, node.y + 1) + nodeCost(node.x + 1, node.y + 2) + 
                            nodeCost(node.x, node.y + 2);
                } else {
                    // Случай 4.2: узел слева-снизу от parent.parent
                    cost1 = nodeCost(node.x, node.y + 2) + nodeCost(node.x + 1, node.y + 2);
                    cost2 = nodeCost(node.x - 1, node.y) + nodeCost(node.x + 1, node.y) + 
                            nodeCost(node.x - 1, node.y + 1) + nodeCost(node.x - 1, node.y + 2) + 
                            nodeCost(node.x, node.y + 2);
                }
            }
            tempCost += Math.min(cost1, cost2);
        } else if (isBox) {
            // Добавляем стоимость позиции за ящиком
            if (node.x - 1 == parent.x) {
                tempCost += nodeCost(node.x - 2, node.y);
            } else if (node.x + 1 == parent.x) {
                tempCost += nodeCost(node.x + 2, node.y);
            } else if (node.y - 1 == parent.y) {
                tempCost += nodeCost(node.x, node.y - 2);
            } else if (node.y + 1 == parent.y) {
                tempCost += nodeCost(node.x, node.y + 2);
            }
        }
        
        // Для оптимизации предпочитаем использованные узлы
        if (node.used) {
            tempCost -= 5;
        }
        
        return tempCost;
    }
    
    private double nodeCost(int x, int y) {
        if (!isInBounds(x, y)) {
            return BOX_COST;
        }
        
        Node node = nodes[x][y];
        if (node.occupied) {
            return BOX_COST;
        } else {
            return node.wall ? WALL_COST : PLAYER_PATH_COST;
        }
    }
    
    private double heuristic(int x, int y) {
        return Math.abs(x - endX) + Math.abs(y - endY);
    }
    
    private PathResult reconstructPath(Node endNode) {
        List<Node> path = new ArrayList<>();
        Node current = endNode;
        
        while (current.parent != null) {
            path.add(0, current);
            current = current.parent;
        }
        
        resetNodes();
        return new PathResult(path, endNode.cost);
    }
    
    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < nodes.length && y >= 0 && y < nodes[0].length;
    }
    
    private void resetNodes() {
        for (Node node : open) {
            node.reset();
        }
        for (Node node : closed) {
            node.reset();
        }
    }
    
    private void addToSortedList(List<Node> list, Node node) {
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (node.f < list.get(i).f) {
                index = i;
                break;
            }
            index = i + 1;
        }
        list.add(index, node);
    }
    
    public static class PathResult {
        public List<Node> path;
        public double cost;
        
        public PathResult(List<Node> path, double cost) {
            this.path = path;
            this.cost = cost;
        }
    }
}
