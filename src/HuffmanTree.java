import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class HuffmanTree {
    private Node root;
    private Node NYT; 
    private int nextOrderNumber;
    private Map<Integer, Node> symbolToNode;
    private List<Node> nodeSwaps;
    
    public HuffmanTree() {
        nextOrderNumber = 512; 
        symbolToNode = new HashMap<>();
        nodeSwaps = new ArrayList<>();
        
        NYT = new Node(0, -1, nextOrderNumber--);
        root = NYT;
    }
    
    public Node getRoot() {
        return root;
    }
    
    public Node getNYT() {
        return NYT;
    }
    
    public List<Node> getNodeSwaps() {
        return nodeSwaps;
    }
    
    public void clearNodeSwaps() {
        nodeSwaps.clear();
    }
    
    public boolean contains(int symbol) {
        return symbolToNode.containsKey(symbol);
    }
    
    public Node getNode(int symbol) {
        return symbolToNode.get(symbol);
    }
    
    public void update(int symbol) {
        nodeSwaps.clear();
        
        if (contains(symbol)) {
            updateExistingSymbol(symbol);
        } else {
            addNewSymbol(symbol);
        }
    }
    
    private void addNewSymbol(int symbol) {
        Node oldNYT = NYT;
        
        Node internalNode = new Node(0, nextOrderNumber--);
        
        Node symbolNode = new Node(0, symbol, nextOrderNumber--);
        
        Node newNYT = new Node(0, -1, nextOrderNumber--);
        
        internalNode.leftChild = newNYT;
        internalNode.rightChild = symbolNode;
        newNYT.parent = internalNode;
        symbolNode.parent = internalNode;
        
        if (oldNYT == root) {
            root = internalNode;
            internalNode.parent = null;
        } else {
            if (oldNYT.parent.leftChild == oldNYT) {
                oldNYT.parent.leftChild = internalNode;
            } else {
                oldNYT.parent.rightChild = internalNode;
            }
            internalNode.parent = oldNYT.parent;
        }
        
        NYT = newNYT;
        
        symbolToNode.put(symbol, symbolNode);
        
        incrementWeight(symbolNode);
    }
    
    private void updateExistingSymbol(int symbol) {
        Node node = symbolToNode.get(symbol);
        
        incrementWeight(node);
    }
    
    private void incrementWeight(Node node) {
        Node highestNode = findHighestNodeWithSameWeight(node);
        
        if (highestNode != null && highestNode != node && !isAncestor(node, highestNode)) {
            nodeSwaps.add(node);
            nodeSwaps.add(highestNode);
            
            swapNodes(node, highestNode);
        }
        
        node.weight++;

        if (node.parent != null) {
            incrementWeight(node.parent);
        }
    }
    
    private boolean isAncestor(Node potential, Node node) {
        Node current = node.parent;
        while (current != null) {
            if (current == potential) return true;
            current = current.parent;
        }
        return false;
    }
    
    private Node findHighestNodeWithSameWeight(Node node) {
        Node result = null;
        int highestOrder = -1;
        
        Node current = findHighestOrderNode();
        while (current != null) {
            if (current.weight == node.weight && 
                current.orderNumber > node.orderNumber && 
                current != node && 
                current != root) {
                
                if (!areSiblings(current, node)) {
                    if (result == null || current.orderNumber > highestOrder) {
                        result = current;
                        highestOrder = current.orderNumber;
                    }
                }
            }
            
            current = findNextHighestOrderNode(current);
        }
        
        return result;
    }
    
    private boolean areSiblings(Node n1, Node n2) {
        return n1.parent != null && n1.parent == n2.parent;
    }
    
    private Node findHighestOrderNode() {
        if (root == null) return null;
        
        Node current = root;
        while (current.leftChild != null) {
            current = current.leftChild;
        }
        
        return current;
    }
    
    private Node findNextHighestOrderNode(Node node) {
        if (node == null) return null;
        
        if (node.rightChild != null) {
            Node current = node.rightChild;
            while (current.leftChild != null) {
                current = current.leftChild;
            }
            return current;
        }
        
        Node current = node;
        while (current.parent != null && current.parent.rightChild == current) {
            current = current.parent;
        }
        
        return current.parent;
    }
    
    private void swapNodes(Node a, Node b) {
        if (a.parent == b || b.parent == a) return;
        
        Node aParent = a.parent;
        Node bParent = b.parent;
        
        boolean aIsLeftChild = aParent != null && aParent.leftChild == a;
        boolean bIsLeftChild = bParent != null && bParent.leftChild == b;
        
        if (aParent != null) {
            if (aIsLeftChild) {
                aParent.leftChild = b;
            } else {
                aParent.rightChild = b;
            }
        }
        
        if (bParent != null) {
            if (bIsLeftChild) {
                bParent.leftChild = a;
            } else {
                bParent.rightChild = a;
            }
        }
        
        a.parent = bParent;
        b.parent = aParent;
        
        if (a == root) root = b;
        else if (b == root) root = a;
        
        int temp = a.orderNumber;
        a.orderNumber = b.orderNumber;
        b.orderNumber = temp;
    }
    
    public String getPathToNode(Node node) {
        StringBuilder path = new StringBuilder();
        Node current = node;
        
        while (current != root) {
            if (current.parent.leftChild == current) {
                path.append('0');
            } else {
                path.append('1');
            }
            current = current.parent;
        }
        
        return path.reverse().toString();
    }
    
    public String getPathToNYT() {
        return getPathToNode(NYT);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendNodeToString(root, sb, "", "");
        return sb.toString();
    }
    
    private void appendNodeToString(Node node, StringBuilder sb, String prefix, String childPrefix) {
        if (node == null) return;
        
        sb.append(prefix);
        
        if (node == NYT) {
            sb.append("NYT");
        } else if (node.isLeaf()) {
            sb.append("'").append((char)node.symbol).append("' (w:").append(node.weight).append(")");
        } else {
            sb.append("(w:").append(node.weight).append(")");
        }
        
        sb.append(" [").append(node.orderNumber).append("]");
        sb.append("\n");
        
        if (node.leftChild != null || node.rightChild != null) {
            appendNodeToString(node.leftChild, sb, childPrefix + "├── ", childPrefix + "│   ");
            appendNodeToString(node.rightChild, sb, childPrefix + "└── ", childPrefix + "    ");
        }
    }
}
