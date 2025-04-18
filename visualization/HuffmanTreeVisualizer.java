import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HuffmanTreeVisualizer extends Application {
    private HuffmanTree tree;
    private Canvas canvas;
    private GraphicsContext gc;
    private TextField inputField;
    private TextArea encodingInfoArea;
    private Label statusLabel;
    
    private final int NODE_RADIUS = 20;
    private final int HORIZONTAL_SPACING = 40;
    private final int VERTICAL_SPACING = 60;
    
    // Store encoding details for visualization
    private Map<Character, String> encodingMap;
    private StringBuilder processedSymbols;
    
    @Override
    public void start(Stage primaryStage) {
        tree = new HuffmanTree();
        encodingMap = new HashMap<>();
        processedSymbols = new StringBuilder();
        
        // Create UI components
        BorderPane root = new BorderPane();
        canvas = new Canvas(800, 400);
        gc = canvas.getGraphicsContext2D();
        
        inputField = new TextField();
        inputField.setPromptText("Enter a character to add");
        
        Button addButton = new Button("Add Character");
        addButton.setOnAction(e -> addCharacter());
        
        Button resetButton = new Button("Reset Tree");
        resetButton.setOnAction(e -> resetTree());
        
        statusLabel = new Label("Adaptive Huffman Tree Visualization");
        
        encodingInfoArea = new TextArea();
        encodingInfoArea.setEditable(false);
        encodingInfoArea.setPrefHeight(200);
        encodingInfoArea.setWrapText(true);
        
        HBox controls = new HBox(10, new Label("Input:"), inputField, addButton, resetButton);
        VBox bottom = new VBox(10, controls, statusLabel, new Label("Encoding Information:"), encodingInfoArea);
        
        root.setCenter(canvas);
        root.setBottom(bottom);
        
        Scene scene = new Scene(root, 800, 650);
        primaryStage.setTitle("Adaptive Huffman Tree Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initial drawing
        drawTree();
        updateEncodingInfo();
    }
    
    private void addCharacter() {
        String input = inputField.getText();
        if (input.isEmpty()) {
            statusLabel.setText("Please enter a character");
            return;
        }
        
        char c = input.charAt(0);
        
        // Simulate encoding process
        boolean isNewSymbol = !tree.contains(c);
        String encoding = simulateEncoding(c);
        
        // Update the encoding map
        encodingMap.put(c, encoding);
        
        // Update the tree
        tree.update(c);
        
        // Add to processed symbols
        processedSymbols.append(c);
        
        // Update status
        if (isNewSymbol) {
            statusLabel.setText("Added new character: '" + c + "' with NYT + ASCII encoding");
        } else {
            statusLabel.setText("Updated existing character: '" + c + "' with path encoding");
        }
        
        inputField.clear();
        
        // Update visualization
        drawTree();
        updateEncodingInfo();
    }
    
    private String simulateEncoding(char c) {
        // Simulate the encoding process without actually writing to a file
        if (tree.contains(c)) {
            // Symbol already in tree, return its path
            return tree.getPathToNode(tree.getNode(c));
        } else {
            // New symbol, return NYT path + ASCII (simplified)
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
    
    private void resetTree() {
        tree = new HuffmanTree();
        encodingMap.clear();
        processedSymbols.setLength(0);
        statusLabel.setText("Tree reset");
        drawTree();
        updateEncodingInfo();
    }
    
    private void updateEncodingInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Processed String: ").append(processedSymbols).append("\n\n");
        info.append("Symbol Encodings:\n");
        
        for (char c : encodingMap.keySet()) {
            info.append("'").append(c).append("': ").append(encodingMap.get(c)).append("\n");
        }
        
        if (encodingMap.isEmpty()) {
            info.append("No symbols encoded yet.\n");
        }
        
        info.append("\nEncoding Process:\n");
        info.append("1. For symbols already in the tree, encode using the path from root to the symbol's node.\n");
        info.append("2. For new symbols, encode using the path to NYT node followed by the 8-bit ASCII representation.\n");
        info.append("3. After each symbol, update the tree to reflect new frequency information.\n");
        
        encodingInfoArea.setText(info.toString());
    }
    
    private void drawTree() {
        // Clear canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw the tree
        if (tree.getRoot() != null) {
            drawNode(tree.getRoot(), canvas.getWidth() / 2, 50, canvas.getWidth() / 4);
        }
    }
    
    private void drawNode(Node node, double x, double y, double horizontalOffset) {
        if (node == null) return;
        
        // Draw the node
        if (node == tree.getNYT()) {
            gc.setFill(Color.YELLOW);
        } else if (node.isLeaf()) {
            gc.setFill(Color.LIGHTGREEN);
        } else {
            gc.setFill(Color.LIGHTBLUE);
        }
        
        gc.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - NODE_RADIUS, y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
        
        // Draw node information
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(12));
        
        if (node == tree.getNYT()) {
            gc.fillText("NYT", x - 15, y + 5);
        } else if (node.isLeaf()) {
            String symbolStr = node.symbol < 32 || node.symbol > 126 ? 
                               String.format("\\u%04X", node.symbol) : String.valueOf((char)node.symbol);
            gc.fillText(symbolStr + ":" + node.weight, x - 15, y + 5);
        } else {
            gc.fillText(String.valueOf(node.weight), x - 10, y + 5);
        }
        
        // Draw connections to children
        if (node.leftChild != null) {
            double childX = x - horizontalOffset;
            double childY = y + VERTICAL_SPACING;
            
            gc.strokeLine(x, y + NODE_RADIUS, childX, childY - NODE_RADIUS);
            gc.fillText("0", (x + childX) / 2 - 10, (y + childY) / 2);
            
            drawNode(node.leftChild, childX, childY, horizontalOffset / 2);
        }
        
        if (node.rightChild != null) {
            double childX = x + horizontalOffset;
            double childY = y + VERTICAL_SPACING;
            
            gc.strokeLine(x, y + NODE_RADIUS, childX, childY - NODE_RADIUS);
            gc.fillText("1", (x + childX) / 2 + 5, (y + childY) / 2);
            
            drawNode(node.rightChild, childX, childY, horizontalOffset / 2);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
