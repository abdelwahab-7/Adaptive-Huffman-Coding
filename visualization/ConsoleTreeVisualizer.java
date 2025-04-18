import java.util.HashMap;
import java.util.Map;

public class ConsoleTreeVisualizer {
    private HuffmanTree tree;
    private Map<Character, String> encodingMap;
    private StringBuilder processedSymbols;
    
    public ConsoleTreeVisualizer() {
        tree = new HuffmanTree();
        encodingMap = new HashMap<>();
        processedSymbols = new StringBuilder();
    }
    
    public void startDemo() {
        System.out.println("Adaptive Huffman Tree Console Visualizer");
        System.out.println("----------------------------------------");
        
        // Example: Encoding the string "ABRACADABRA"
        String demoString = "ABRACADABRA";
        System.out.println("Demonstrating adaptive Huffman coding with the string: " + demoString);
        
        for (int i = 0; i < demoString.length(); i++) {
            char c = demoString.charAt(i);
            System.out.println("\n=== Processing character: '" + c + "' (position " + (i+1) + ") ===");
            
            addCharacter(c);
            printTree();
            System.out.println();
        }
        
        printFinalEncodingInfo();
    }
    
    private void addCharacter(char c) {
        boolean isNewSymbol = !tree.contains(c);
        String encoding = simulateEncoding(c);
        
        // Update the encoding map
        encodingMap.put(c, encoding);
        
        // Update the tree
        tree.update(c);
        
        // Add to processed symbols
        processedSymbols.append(c);
        
        // Print status
        if (isNewSymbol) {
            System.out.println("New symbol: '" + c + "' encoded with NYT path + ASCII");
        } else {
            System.out.println("Existing symbol: '" + c + "' encoded with its path in the tree");
        }
        
        System.out.println("Encoding: " + encoding);
    }
    
    private String simulateEncoding(char c) {
        if (tree.contains(c)) {
            // Symbol already in tree, return its path
            return tree.getPathToNode(tree.getNode(c));
        } else {
            // New symbol, return NYT path + ASCII
            String nytPath = tree.getPathToNYT();
            StringBuilder bits = new StringBuilder(nytPath);
            bits.append(" + ASCII(");
            
            // ASCII representation in binary
            for (int i = 7; i >= 0; i--) {
                bits.append((c >> i) & 1);
            }
            bits.append(")");
            
            return bits.toString();
        }
    }
    
    private void printFinalEncodingInfo() {
        System.out.println("\n=== Final Encoding Summary ===");
        System.out.println("Processed String: " + processedSymbols);
        System.out.println("\nSymbol Encodings:");
        
        for (char c : encodingMap.keySet()) {
            System.out.println("'" + c + "': " + encodingMap.get(c));
        }
        
        System.out.println("\nAdaptive Huffman Coding Process:");
        System.out.println("1. For symbols already in the tree, encode using the path from root to the symbol's node.");
        System.out.println("2. For new symbols, encode using the path to NYT node followed by the 8-bit ASCII representation.");
        System.out.println("3. After each symbol, update the tree to reflect new frequency information.");
        
        // Calculate compression (approximate, not actual bits)
        int originalBits = processedSymbols.length() * 8; // 8 bits per character in ASCII
        int encodedBits = calculateApproximateEncodedBits();
        
        System.out.println("\nApproximate Compression:");
        System.out.println("Original: " + originalBits + " bits (" + processedSymbols.length() + " bytes)");
        System.out.println("Encoded: ~" + encodedBits + " bits (~" + (encodedBits / 8.0) + " bytes)");
        System.out.println("Ratio: " + String.format("%.2f", (double)encodedBits / originalBits));
    }
    
    private int calculateApproximateEncodedBits() {
        int totalBits = 0;
        Map<Character, Integer> frequencies = new HashMap<>();
        
        // Count character frequencies
        for (char c : processedSymbols.toString().toCharArray()) {
            frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
        }
        
        // For each unique character, first occurrence uses NYT + ASCII encoding
        for (char c : frequencies.keySet()) {
            // First occurrence: NYT path + 8 bits
            String firstEncoding = encodingMap.get(c);
            int pathBits = 0;
            
            if (firstEncoding.contains(" + ASCII")) {
                // Extract just the path part
                pathBits = firstEncoding.indexOf(" +") > 0 ? 
                           firstEncoding.substring(0, firstEncoding.indexOf(" +")).length() : 0;
                totalBits += pathBits + 8; // path + 8 bits for ASCII
            } else {
                pathBits = firstEncoding.length();
                totalBits += pathBits;
            }
            
            // Subsequent occurrences: just the path (which may change over time)
            int subsequentOccurrences = frequencies.get(c) - 1;
            if (subsequentOccurrences > 0) {
                // This is a simplification - in reality, the path length can change
                // For demonstration purposes, we'll just use the final path length
                totalBits += subsequentOccurrences * tree.getPathToNode(tree.getNode(c)).length();
            }
        }
        
        return totalBits;
    }
    
    private void printTree() {
        System.out.println("\nCurrent Huffman Tree:");
        if (tree.getRoot() == tree.getNYT()) {
            System.out.println("NYT (empty tree)");
            return;
        }
        
        printNode(tree.getRoot(), "", true);
    }
    
    private void printNode(Node node, String prefix, boolean isTail) {
        String nodeInfo;
        
        if (node == tree.getNYT()) {
            nodeInfo = "NYT";
        } else if (node.isLeaf()) {
            nodeInfo = "'" + (char)node.symbol + "' (w:" + node.weight + ")";
        } else {
            nodeInfo = "(w:" + node.weight + ")";
        }
        
        System.out.println(prefix + (isTail ? "└── " : "├── ") + nodeInfo);
        
        if (node.leftChild != null && node.rightChild != null) {
            printNode(node.leftChild, prefix + (isTail ? "    " : "│   "), false);
            printNode(node.rightChild, prefix + (isTail ? "    " : "│   "), true);
        } else if (node.leftChild != null) {
            printNode(node.leftChild, prefix + (isTail ? "    " : "│   "), true);
        } else if (node.rightChild != null) {
            printNode(node.rightChild, prefix + (isTail ? "    " : "│   "), true);
        }
    }
    
    public static void main(String[] args) {
        ConsoleTreeVisualizer visualizer = new ConsoleTreeVisualizer();
        visualizer.startDemo();
    }
}