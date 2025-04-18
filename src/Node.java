public class Node {
    public int weight;      
    public int symbol;      
    public Node parent;     
    public Node leftChild;  
    public Node rightChild; 
    public int orderNumber; 

    public Node(int weight, int orderNumber) {
        this.weight = weight;
        this.symbol = -1;  
        this.orderNumber = orderNumber;
        this.parent = null;
        this.leftChild = null;
        this.rightChild = null;
    }

    public Node(int weight, int symbol, int orderNumber) {
        this.weight = weight;
        this.symbol = symbol;
        this.orderNumber = orderNumber;
        this.parent = null;
        this.leftChild = null;
        this.rightChild = null;
    }

    public boolean isLeaf() {
        return leftChild == null && rightChild == null;
    }

    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public String toString() {
        if (isLeaf()) {
            return "Leaf[symbol=" + (char)symbol + ", weight=" + weight + ", order=" + orderNumber + "]";
        } else {
            return "Node[weight=" + weight + ", order=" + orderNumber + "]";
        }
    }
}
