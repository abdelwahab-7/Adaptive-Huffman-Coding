import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.geom.*;
import javax.swing.border.EmptyBorder;
import javax.swing.Timer; // Explicitly import Swing Timer
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.*;

public class SwingHuffmanVisualizer extends JFrame {
    private HuffmanTreePanel treePanel;
    private JTextField inputField;
    private JTextArea logArea;
    private JTextArea infoArea;
    private HuffmanTree tree;
    private Map<Character, String> encodingMap;
    private StringBuilder processedSymbols;
    private StringBuilder encodedBits;
    private StringBuilder decodedSymbols;
    
    private JButton encodeStepButton;
    private JButton encodeAllButton;
    private JButton decodeStepButton;
    private JButton decodeAllButton;
    private JButton resetButton;
    private JToggleButton modeToggleButton;
    private JLabel statusLabel;
    private JSlider animationSpeedSlider;
    private JPanel inputButtonsPanel;
    
    // Mode settings
    private boolean isEncodingMode = true;
    
    // Animation queue
    private final ConcurrentLinkedQueue<AnimationStep> animationQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean animationRunning = new AtomicBoolean(false);
    private Timer animationTimer;
    private long animationStepDelay = 500; // ms
    
    private String currentInput = "";
    private int currentPosition = 0;
    private ArrayList<Integer> encodedBitsList = new ArrayList<>();
    private int currentBitPosition = 0;
    
    // Virtual bit streams for encode/decode simulation
    private MockBitInputStream mockInputStream;
    private MockBitOutputStream mockOutputStream;
    
    // Color map for nodes of the same weight (weights 0-10)
    private static final Color[] WEIGHT_COLORS = {
        new Color(230, 230, 250),  
        new Color(173, 216, 230),  
        new Color(144, 238, 144),  
        new Color(255, 218, 185),  
        new Color(255, 182, 193),  
        new Color(240, 230, 140),  
        new Color(221, 160, 221),  
        new Color(176, 224, 230),  
        new Color(152, 251, 152),  
        new Color(255, 160, 122),  
        new Color(135, 206, 235), 
    };
    
    private static final Color DEFAULT_NODE_COLOR = new Color(211, 211, 211);  // Light gray for weights > 10
    private static final Color NYT_NODE_COLOR = new Color(255, 215, 0);        // Gold for NYT
    private static final Color HIGHLIGHT_COLOR = new Color(255, 165, 0);       // Orange
    private static final Color SWAP_COLOR = new Color(255, 105, 180);          // Hot pink
    private static final Color BACKGROUND_COLOR = new Color(248, 248, 248);    // Off-white
    private static final Color DECODE_COLOR = new Color(100, 149, 237);        // Cornflower blue
    
    public SwingHuffmanVisualizer() {
        super("Adaptive Huffman Tree Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        
        // Initialize tree and data structures
        resetDataStructures();
        
        setupUI();
        
        // Animation timer with a fixed rate
        animationTimer = new Timer((int)animationStepDelay, e -> processNextAnimation());
        animationTimer.setRepeats(false);
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void resetDataStructures() {
        tree = new HuffmanTree();
        encodingMap = new HashMap<>();
        processedSymbols = new StringBuilder();
        encodedBits = new StringBuilder();
        decodedSymbols = new StringBuilder();
        encodedBitsList.clear();
        mockOutputStream = new MockBitOutputStream();
        mockInputStream = new MockBitInputStream();
    }
    
    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        treePanel = new HuffmanTreePanel();
        treePanel.setPreferredSize(new Dimension(800, 500));
        
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        controlPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JPanel topRowPanel = new JPanel(new BorderLayout());
        topRowPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel inputLabel = new JLabel("Input String: ");
        inputField = new JTextField(20);
        inputField.setText("ABCCCAAAA");  // Default input
        
        modeToggleButton = new JToggleButton("Decompression Mode");
        modeToggleButton.setSelected(false); // Default to encoding mode
        modeToggleButton.addActionListener(e -> toggleMode());
        
        topRowPanel.add(inputLabel, BorderLayout.WEST);
        topRowPanel.add(inputField, BorderLayout.CENTER);
        topRowPanel.add(modeToggleButton, BorderLayout.EAST);
        
        inputButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputButtonsPanel.setBackground(BACKGROUND_COLOR);
        
        encodeStepButton = new JButton("Encode Step");
        encodeAllButton = new JButton("Encode All");
        decodeStepButton = new JButton("Decode Step");
        decodeAllButton = new JButton("Decode All");
        resetButton = new JButton("Reset");
        
        inputButtonsPanel.add(encodeStepButton);
        inputButtonsPanel.add(encodeAllButton);
        inputButtonsPanel.add(decodeStepButton);
        inputButtonsPanel.add(decodeAllButton);
        inputButtonsPanel.add(resetButton);
        
        decodeStepButton.setVisible(false);
        decodeAllButton.setVisible(false);
        
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        speedPanel.setBackground(BACKGROUND_COLOR);
        JLabel speedLabel = new JLabel("Animation Speed: ");
        animationSpeedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        animationSpeedSlider.setMajorTickSpacing(1);
        animationSpeedSlider.setPaintTicks(true);
        animationSpeedSlider.setPaintLabels(true);
        animationSpeedSlider.setBackground(BACKGROUND_COLOR);
        animationSpeedSlider.addChangeListener(e -> {
            int value = animationSpeedSlider.getValue();
            animationStepDelay = 1000 / value;  // 1000ms to 100ms range
        });
        
        speedPanel.add(speedLabel);
        speedPanel.add(animationSpeedSlider);
        
        inputPanel.add(topRowPanel, BorderLayout.NORTH);
        inputPanel.add(inputButtonsPanel, BorderLayout.CENTER);
        inputPanel.add(speedPanel, BorderLayout.SOUTH);
        
        statusLabel = new JLabel("Ready to encode. Enter a string or use the default. After encoding, use the 'Decompression Mode' button in the top-right to switch to decoding mode.");
        statusLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(BACKGROUND_COLOR);
        infoPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        infoArea = new JTextArea(8, 30);
        infoArea.setEditable(false);
        infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane infoScrollPane = new JScrollPane(infoArea);
        
        JLabel infoLabel = new JLabel("Information:");
        infoPanel.add(infoLabel, BorderLayout.NORTH);
        infoPanel.add(infoScrollPane, BorderLayout.CENTER);
        
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(BACKGROUND_COLOR);
        logPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        
        JLabel logLabel = new JLabel("Log:");
        logPanel.add(logLabel, BorderLayout.NORTH);
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        
        bottomPanel.add(infoPanel);
        bottomPanel.add(logPanel);
        
        controlPanel.add(inputPanel, BorderLayout.NORTH);
        controlPanel.add(statusLabel, BorderLayout.CENTER);
        controlPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        mainPanel.add(treePanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        setupListeners();
    }
    
    private void toggleMode() {
        isEncodingMode = !modeToggleButton.isSelected();
        
        if (isEncodingMode) {
            modeToggleButton.setText("Decompression Mode");
            statusLabel.setText("Ready to encode. Enter a string or use the default.");
        } else {
            modeToggleButton.setText("Compression Mode");
            
            if (encodedBitsList.size() > 0) {
                mockInputStream.setBits(encodedBitsList);
                statusLabel.setText("Ready to decode " + encodedBitsList.size() + " bits.");
            } else {
                statusLabel.setText("Encode data first to enable decoding visualization.");
            }
        }
        
        updateButtonVisibility();
        updateInfoDisplay();
        
        inputButtonsPanel.revalidate();
        inputButtonsPanel.repaint();
    }
    
    private void updateButtonVisibility() {
        encodeStepButton.setVisible(isEncodingMode);
        encodeAllButton.setVisible(isEncodingMode);
        decodeStepButton.setVisible(!isEncodingMode);
        decodeAllButton.setVisible(!isEncodingMode);
    }
    
    private void setupListeners() {
        encodeStepButton.addActionListener(e -> encodeNextStep());
        encodeAllButton.addActionListener(e -> encodeAllSteps());
        decodeStepButton.addActionListener(e -> decodeNextStep());
        decodeAllButton.addActionListener(e -> decodeAllSteps());
        resetButton.addActionListener(e -> resetTree());
        
        inputField.addActionListener(e -> {
            // Reset and prepare for encoding/decoding when Enter is pressed
            resetTree();
            currentInput = inputField.getText();
            if (!currentInput.isEmpty()) {
                if (isEncodingMode) {
                    statusLabel.setText("Ready to encode: " + currentInput);
                }
            }
        });
    }
    
    
    private void encodeNextStep() {
        if (currentPosition >= currentInput.length()) {
            statusLabel.setText("Encoding complete. Reset or switch to decompression mode.");
            return;
        }
        
        char c = currentInput.charAt(currentPosition);
        
        log("Processing character: '" + c + "' (position " + (currentPosition + 1) + " of " + currentInput.length() + ")");
        
        boolean isNewSymbol = !tree.contains(c);
        String encoding = simulateEncoding(c);
        
        List<Node> path = new ArrayList<>();
        if (isNewSymbol) {
            // Highlight path to NYT
            Node current = tree.getNYT();
            while (current != null) {
                path.add(0, current);
                current = current.parent;
            }
            
            enqueueAnimation(new AnimationStep(
                AnimationType.HIGHLIGHT_PATH, 
                path,
                "Encoding new symbol '" + c + "' - following path to NYT"
            ));
            
            log("New symbol: encoding with NYT path (" + tree.getPathToNYT() + ") + ASCII");
        } else {
            Node symbolNode = tree.getNode(c);
            Node current = symbolNode;
            while (current != null) {
                path.add(0, current); 
                current = current.parent;
            }
            
            enqueueAnimation(new AnimationStep(
                AnimationType.HIGHLIGHT_PATH, 
                path,
                "Encoding existing symbol '" + c + "' - following path to symbol node"
            ));
            
            log("Existing symbol: encoding with path " + tree.getPathToNode(symbolNode));
        }
        
        enqueueAnimation(new AnimationStep(
            AnimationType.UPDATE_TREE,
            c,
            "Updating tree after processing '" + c + "'"
        ));
        
        encodingMap.put(c, encoding);
        
        processedSymbols.append(c);
        
        try {
            if (tree.contains(c)) {
                String path_str = tree.getPathToNode(tree.getNode(c));
                for (int i = 0; i < path_str.length(); i++) {
                    int bit = path_str.charAt(i) == '1' ? 1 : 0;
                    mockOutputStream.writeBit(bit);
                    encodedBits.append(bit);
                    encodedBitsList.add(bit);
                }
            } else {
                String nytPath = tree.getPathToNYT();
                for (int i = 0; i < nytPath.length(); i++) {
                    int bit = nytPath.charAt(i) == '1' ? 1 : 0;
                    mockOutputStream.writeBit(bit);
                    encodedBits.append(bit);
                    encodedBitsList.add(bit);
                }
                
                for (int i = 7; i >= 0; i--) {
                    int bit = (c >> i) & 1;
                    mockOutputStream.writeBit(bit);
                    encodedBits.append(bit);
                    encodedBitsList.add(bit);
                }
            }
        } catch (IOException ex) {
            log("Error encoding: " + ex.getMessage());
        }
        
        if (isNewSymbol) {
            statusLabel.setText("Added new character: '" + c + "' with NYT + ASCII encoding");
        } else {
            statusLabel.setText("Updated existing character: '" + c + "' with path encoding");
        }
        
        log("Encoding: " + encoding);
        
        startAnimations();
        
        currentPosition++;
        
        updateInfoDisplay();
    }
    
    private void encodeAllSteps() {
        if (currentPosition >= currentInput.length()) {
            statusLabel.setText("Encoding complete. Reset or switch to decompression mode.");
            return;
        }
        
        Timer allStepsTimer = new Timer(50, null);
        allStepsTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPosition < currentInput.length() && !animationRunning.get()) {
                    encodeNextStep();
                } else if (currentPosition >= currentInput.length()) {
                    allStepsTimer.stop();
                    statusLabel.setText("Encoding complete. You can now switch to decompression mode to visualize decoding.");
                }
            }
        });
        allStepsTimer.start();
    }
    
    private String simulateEncoding(char c) {
        if (tree.contains(c)) {
            return tree.getPathToNode(tree.getNode(c));
        } else {
            String nytPath = tree.getPathToNYT();
            StringBuilder bits = new StringBuilder(nytPath);
            bits.append(" + ASCII(");
            
            for (int i = 7; i >= 0; i--) {
                bits.append((c >> i) & 1);
            }
            bits.append(")");
            
            return bits.toString();
        }
    }
    
    // DECODING METHODS
    
    private void decodeNextStep() {
        if (currentBitPosition >= encodedBitsList.size()) {
            statusLabel.setText("Decoding complete. Reset to start again.");
            return;
        }
        
        try {
            mockInputStream.resetPosition(currentBitPosition);
            
            Node currentNode = tree.getRoot();
            List<Node> path = new ArrayList<>();
            path.add(currentNode);
            
            while (!currentNode.isLeaf() && currentNode != tree.getNYT()) {
                if (currentBitPosition >= encodedBitsList.size()) {
                    statusLabel.setText("End of bit stream reached.");
                    return;
                }
                
                int bit = mockInputStream.readBit();
                currentBitPosition++;
                
                if (bit == 0) {
                    currentNode = currentNode.leftChild;
                } else {
                    currentNode = currentNode.rightChild;
                }
                
                path.add(currentNode);
            }
            
            enqueueAnimation(new AnimationStep(
                AnimationType.HIGHLIGHT_PATH,
                new ArrayList<>(path),
                "Decoding - following bit pattern through tree"
            ));
            
            int symbol;
            if (currentNode == tree.getNYT()) {
                StringBuilder asciiBits = new StringBuilder();
                for (int i = 0; i < 8; i++) {
                    if (currentBitPosition >= encodedBitsList.size()) {
                        statusLabel.setText("End of bit stream reached during ASCII read.");
                        return;
                    }
                    
                    int bit = mockInputStream.readBit();
                    currentBitPosition++;
                    asciiBits.append(bit);
                }
                
                symbol = 0;
                for (int i = 0; i < 8; i++) {
                    symbol = (symbol << 1) | (asciiBits.charAt(i) == '1' ? 1 : 0);
                }
                
                log("Decoded via NYT + ASCII bits (" + asciiBits + ") as: '" + (char)symbol + "'");
            } else {
                symbol = currentNode.symbol;
                log("Decoded via tree path as: '" + (char)symbol + "'");
            }
            
            enqueueAnimation(new AnimationStep(
                AnimationType.UPDATE_TREE,
                (char)symbol,
                "Updating tree after decoding '" + (char)symbol + "'"
            ));
            
            decodedSymbols.append((char)symbol);
            
            tree.update(symbol);
            
            statusLabel.setText("Decoded symbol: '" + (char)symbol + "' (Bit position: " + currentBitPosition + " of " + encodedBitsList.size() + ")");
            
            startAnimations();
            
            updateInfoDisplay();
            
        } catch (IOException ex) {
            log("Error decoding: " + ex.getMessage());
        }
    }
    
    private void decodeAllSteps() {
        if (currentBitPosition >= encodedBitsList.size()) {
            statusLabel.setText("Decoding complete. Reset to start again.");
            return;
        }
        
        // Schedule all remaining steps with short delays between them
        Timer allStepsTimer = new Timer(50, null);
        allStepsTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentBitPosition < encodedBitsList.size() && !animationRunning.get()) {
                    decodeNextStep();
                } else if (currentBitPosition >= encodedBitsList.size() || mockInputStream.isEndOfStream()) {
                    allStepsTimer.stop();
                    statusLabel.setText("Decoding complete.");
                }
            }
        });
        allStepsTimer.start();
    }
    
    private void resetTree() {
        // Stop any animations
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        animationQueue.clear();
        animationRunning.set(false);
        
        // Reset data structures
        resetDataStructures();
        
        // Reset UI
        currentInput = inputField.getText();
        currentPosition = 0;
        currentBitPosition = 0;
        
        if (isEncodingMode) {
            statusLabel.setText("Tree reset. Ready to encode: " + currentInput);
        } else {
            statusLabel.setText("Tree reset. Switch to encoding mode to generate data for decoding.");
            modeToggleButton.setSelected(false);
            isEncodingMode = true;
            updateButtonVisibility();
        }
        
        logArea.setText("");
        updateInfoDisplay();
        
        // Redraw the tree
        treePanel.repaint();
    }
    
    private void log(String message) {
        logArea.append(message + "\n");
        // Scroll to the bottom
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void updateInfoDisplay() {
        StringBuilder info = new StringBuilder();
        
        if (isEncodingMode) {
            // Encoding mode info
            info.append("ENCODING MODE\n\n");
            info.append("Processed: ").append(processedSymbols).append("\n\n");
            
            if (encodedBits.length() > 0) {
                info.append("Encoded Bits: ").append(formatBitString(encodedBits.toString())).append("\n\n");
            }
            
            info.append("Symbol Encodings:\n");
            
            for (char c : encodingMap.keySet()) {
                info.append("'").append(c).append("': ").append(encodingMap.get(c)).append("\n");
            }
            
            if (encodingMap.isEmpty()) {
                info.append("No symbols encoded yet.\n");
            }
            
            // Calculate compression stats
            if (processedSymbols.length() > 0) {
                int originalBits = processedSymbols.length() * 8; // 8 bits per ASCII char
                int encodedBits = calculateApproximateEncodedBits();
                double ratio = (double) encodedBits / originalBits;
                
                info.append("\nCompression Stats:\n");
                info.append("Original: ").append(originalBits).append(" bits\n");
                info.append("Encoded: ~").append(encodedBits).append(" bits\n");
                info.append("Ratio: ").append(String.format("%.2f", ratio)).append("\n");
            }
        } else {
            // Decoding mode info
            info.append("DECODING MODE\n\n");
            
            if (encodedBitsList.size() > 0) {
                info.append("Total Bits: ").append(encodedBitsList.size()).append("\n");
                info.append("Current Position: ").append(currentBitPosition).append("\n\n");
                
                // Show the bit stream with highlighting for processed bits
                info.append("Bit Stream:\n");
                StringBuilder bitStream = new StringBuilder();
                for (int i = 0; i < encodedBitsList.size(); i++) {
                    if (i == currentBitPosition) {
                        bitStream.append("[");
                    }
                    bitStream.append(encodedBitsList.get(i));
                    if (i == currentBitPosition - 1) {
                        bitStream.append("]");
                    }
                    
                    // Add space every 8 bits for readability
                    if ((i + 1) % 8 == 0) {
                        bitStream.append(" ");
                    }
                }
                info.append(bitStream).append("\n\n");
            }
            
            info.append("Decoded Output: ").append(decodedSymbols).append("\n\n");
            
            // Compare original and decoded
            if (processedSymbols.length() > 0 && decodedSymbols.length() > 0) {
                info.append("Verification:\n");
                info.append("Original: ").append(processedSymbols).append("\n");
                info.append("Decoded:  ").append(decodedSymbols).append("\n");
                
                boolean matches = processedSymbols.toString().equals(decodedSymbols.toString());
                info.append("Match: ").append(matches ? "YES" : "NO (error in decoding)").append("\n");
            }
        }
        
        // Add information about the adaptive Huffman algorithm features
        info.append("\nAdaptive Huffman Features:\n");
        info.append("1. Dynamic tree building - tree updates as symbols are processed\n");
        info.append("2. Node swapping - maintains the sibling property:\n");
        info.append("   • Nodes are ordered by weight\n");
        info.append("   • For nodes of same weight, highest-ordered node is swapped\n");
        info.append("   • Highlights show encoding/decoding paths\n");
        info.append("   • Pink highlights show nodes being swapped\n");
        info.append("3. FGK Algorithm - ensures optimal tree structure for adaptive coding\n");
        
        infoArea.setText(info.toString());
    }
    
    private String formatBitString(String bits) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < bits.length(); i++) {
            formatted.append(bits.charAt(i));
            if ((i + 1) % 8 == 0 && i < bits.length() - 1) {
                formatted.append(" ");
            }
        }
        return formatted.toString();
    }
    
    private int calculateApproximateEncodedBits() {
        int totalBits = 0;
        Map<Character, Integer> frequencies = new HashMap<>();
        
        // Count character frequencies
        for (char c : processedSymbols.toString().toCharArray()) {
            frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
        }
        
        // For each unique character
        for (char c : frequencies.keySet()) {
            // Get encoding
            String encodingInfo = encodingMap.get(c);
            
            if (encodingInfo.contains(" + ASCII")) {
                // First occurrence uses NYT path + ASCII
                int pathBits = encodingInfo.indexOf(" +") > 0 ? 
                               encodingInfo.substring(0, encodingInfo.indexOf(" +")).length() : 0;
                
                // First occurrence
                totalBits += pathBits + 8;
                
                // Subsequent occurrences use current path
                int subsequentOccurrences = frequencies.get(c) - 1;
                if (subsequentOccurrences > 0) {
                    totalBits += subsequentOccurrences * tree.getPathToNode(tree.getNode(c)).length();
                }
            } else {
                // All occurrences use a path
                totalBits += frequencies.get(c) * encodingInfo.length();
            }
        }
        
        return totalBits;
    }
    
    // Animation handling
    
    private void enqueueAnimation(AnimationStep step) {
        animationQueue.add(step);
    }
    
    private void startAnimations() {
        if (!animationRunning.getAndSet(true) && !animationQueue.isEmpty()) {
            processNextAnimation();
        }
    }
    
    private void processNextAnimation() {
        AnimationStep step = animationQueue.poll();
        if (step == null) {
            animationRunning.set(false);
            return;
        }
        
        log("Animation: " + step.description);
        
        switch (step.type) {
            case HIGHLIGHT_PATH:
                treePanel.highlightPath((List<Node>)step.data, !isEncodingMode);
                break;
                
            case UPDATE_TREE:
                char c = (Character)step.data;
                
                // Update tree and capture swaps
                tree.update(c);
                
                // Check if any nodes were swapped during the update
                List<Node> swappedNodes = tree.getNodeSwaps();
                if (!swappedNodes.isEmpty()) {
                    // Highlight the swapped nodes
                    treePanel.highlightSwap(new ArrayList<>(swappedNodes));
                    log("Nodes swapped to maintain the sibling property");
                }
                
                treePanel.refreshTree();
                break;
                
            case HIGHLIGHT_SWAPS:
                treePanel.highlightSwap((List<Node>)step.data);
                break;
        }
        
        // Schedule next animation
        if (!animationQueue.isEmpty()) {
            animationTimer.setInitialDelay((int)animationStepDelay);
            animationTimer.restart();
        } else {
            animationRunning.set(false);
        }
    }
    
    // Animation classes
    
    private enum AnimationType {
        HIGHLIGHT_PATH,
        UPDATE_TREE,
        HIGHLIGHT_SWAPS
    }
    
    private static class AnimationStep {
        final AnimationType type;
        final Object data;
        final String description;
        
        AnimationStep(AnimationType type, Object data, String description) {
            this.type = type;
            this.data = data;
            this.description = description;
        }
    }
    
    // Mock bit stream classes for visualization
    
    private class MockBitOutputStream {
        private List<Integer> bits = new ArrayList<>();
        
        public void writeBit(int bit) throws IOException {
            bits.add(bit);
        }
        
        public List<Integer> getBits() {
            return bits;
        }
    }
    
    private class MockBitInputStream {
        private List<Integer> bits = new ArrayList<>();
        private int position = 0;
        private boolean endOfStream = false;
        
        public void setBits(List<Integer> bits) {
            this.bits = new ArrayList<>(bits);
            this.position = 0;
            this.endOfStream = false;
        }
        
        public int readBit() throws IOException {
            if (position >= bits.size()) {
                endOfStream = true;
                return -1; // End of stream
            }
            return bits.get(position++);
        }
        
        public void resetPosition(int newPosition) {
            this.position = newPosition;
            this.endOfStream = false;
        }
        
        public boolean isEndOfStream() {
            return endOfStream;
        }
    }
    
    // Tree visualization panel
    
    private class HuffmanTreePanel extends JPanel {
        private double nodeRadius = 25;
        private double horizontalSpacing = 50;
        private double verticalSpacing = 70;
        
        private List<Node> highlightedPath = null;
        private boolean isDecodePath = false;
        private List<Node> swappedNodes = null;
        private boolean showSwapArrow = false;
        private Map<Node, Point2D> lastNodePositions = new HashMap<>();
        
        public HuffmanTreePanel() {
            setBackground(BACKGROUND_COLOR);
        }
        
        public void highlightPath(List<Node> path, boolean isDecodePath) {
            this.highlightedPath = path;
            this.isDecodePath = isDecodePath;
            repaint();
            
            // Clear highlight after a delay
            Timer t = new Timer(1000, e -> {
                highlightedPath = null;
                repaint();
            });
            t.setRepeats(false);
            t.start();
        }
        
        public void highlightSwap(List<Node> nodes) {
            if (nodes.size() >= 2) {
                this.swappedNodes = nodes;
                this.showSwapArrow = true;
                
                // Save current positions to draw the arrow correctly
                lastNodePositions = calculateNodePositions();
                
                repaint();
                
                // Show the arrow for a bit longer
                Timer arrowTimer = new Timer(1500, e -> {
                    showSwapArrow = false;
                    repaint();
                });
                arrowTimer.setRepeats(false);
                arrowTimer.start();
                
                // Clear swap highlight after a delay
                Timer t = new Timer(3000, e -> {
                    swappedNodes = null;
                    repaint();
                });
                t.setRepeats(false);
                t.start();
            }
        }
        
        public void refreshTree() {
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Enable anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw the tree
            if (tree.getRoot() != null) {
                // Calculate tree layout
                Map<Node, Point2D> nodePositions = calculateNodePositions();
                
                // Draw swap arrows if needed
                if (showSwapArrow && swappedNodes != null && swappedNodes.size() >= 2) {
                    drawSwapArrow(g2d, nodePositions);
                }
                
                // Draw connections first (so they're behind nodes)
                for (Map.Entry<Node, Point2D> entry : nodePositions.entrySet()) {
                    Node node = entry.getKey();
                    Point2D nodePos = entry.getValue();
                    
                    if (node.leftChild != null) {
                        Point2D childPos = nodePositions.get(node.leftChild);
                        drawConnection(g2d, nodePos, childPos, "0", highlightedPath != null && 
                                      highlightedPath.contains(node) && highlightedPath.contains(node.leftChild),
                                      isDecodePath);
                    }
                    
                    if (node.rightChild != null) {
                        Point2D childPos = nodePositions.get(node.rightChild);
                        drawConnection(g2d, nodePos, childPos, "1", highlightedPath != null && 
                                      highlightedPath.contains(node) && highlightedPath.contains(node.rightChild),
                                      isDecodePath);
                    }
                }
                
                // Then draw the nodes
                for (Map.Entry<Node, Point2D> entry : nodePositions.entrySet()) {
                    Node node = entry.getKey();
                    Point2D pos = entry.getValue();
                    
                    boolean isHighlighted = highlightedPath != null && highlightedPath.contains(node);
                    boolean isSwapped = swappedNodes != null && swappedNodes.contains(node);
                    
                    drawNode(g2d, node, pos.getX(), pos.getY(), isHighlighted, isSwapped, isDecodePath);
                }
            } else {
                // Draw empty tree message
                g2d.setColor(Color.GRAY);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
                g2d.drawString("Empty Tree", getWidth() / 2 - 60, getHeight() / 2);
            }
        }
        
        // Draw the swap arrow between two nodes
        private void drawSwapArrow(Graphics2D g2d, Map<Node, Point2D> currentPositions) {
            if (swappedNodes == null || swappedNodes.size() < 2) return;
            
            Node node1 = swappedNodes.get(0);
            Node node2 = swappedNodes.get(1);
            
            // Get positions from the saved positions map (pre-swap positions)
            Point2D pos1 = lastNodePositions.get(node1);
            Point2D pos2 = lastNodePositions.get(node2);
            
            // If we don't have saved positions, use current positions
            if (pos1 == null) pos1 = currentPositions.get(node1);
            if (pos2 == null) pos2 = currentPositions.get(node2);
            
            if (pos1 == null || pos2 == null) return;
            
            // Draw the arrow with thicker line
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Thicker solid line
            
            double x1 = pos1.getX();
            double y1 = pos1.getY();
            double x2 = pos2.getX();
            double y2 = pos2.getY();
            
            // Calculate the angle between the points
            double angle = Math.atan2(y2 - y1, x2 - x1);
            double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
            double arrowSize = 15; // Larger arrowhead
            
            // Adjust line endpoints to leave room for arrowheads
            double adjustedX1 = x1 + arrowSize * Math.cos(angle);
            double adjustedY1 = y1 + arrowSize * Math.sin(angle);
            double adjustedX2 = x2 - arrowSize * Math.cos(angle);
            double adjustedY2 = y2 - arrowSize * Math.sin(angle);
            
            // Draw the main line
            g2d.drawLine((int)adjustedX1, (int)adjustedY1, (int)adjustedX2, (int)adjustedY2);
            
            // Draw the <-> style arrowheads
            drawBidirectionalArrowHead(g2d, x1, y1, x2, y2, arrowSize);
            drawBidirectionalArrowHead(g2d, x2, y2, x1, y1, arrowSize);
            
            // Reset stroke
            g2d.setStroke(new BasicStroke(1.0f));
        }
        
        // Draw a <-> style arrowhead
        private void drawBidirectionalArrowHead(Graphics2D g2d, double x1, double y1, double x2, double y2, double arrowSize) {
            double angle = Math.atan2(y2 - y1, x2 - x1);
            
            // Draw perpendicular lines at the end of the arrow
            double perpAngle1 = angle + Math.PI/2; // 90 degrees clockwise
            double perpAngle2 = angle - Math.PI/2; // 90 degrees counter-clockwise
            
            // Calculate arrowhead points
            double tipX = x1 + arrowSize * Math.cos(angle);
            double tipY = y1 + arrowSize * Math.sin(angle);
            
            double wing1X = tipX + arrowSize * 0.5 * Math.cos(perpAngle1);
            double wing1Y = tipY + arrowSize * 0.5 * Math.sin(perpAngle1);
            double wing2X = tipX + arrowSize * 0.5 * Math.cos(perpAngle2);
            double wing2Y = tipY + arrowSize * 0.5 * Math.sin(perpAngle2);
            
            // Draw a filled triangle for the arrowhead
            int[] xPoints = {(int)tipX, (int)wing1X, (int)wing2X};
            int[] yPoints = {(int)tipY, (int)wing1Y, (int)wing2Y};
            
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
        
        private Map<Node, Point2D> calculateNodePositions() {
            Map<Node, Point2D> positions = new HashMap<>();
            
            // Start with the root at the center top
            double rootX = getWidth() / 2;
            double rootY = 50;
            
            // Perform a breadth-first traversal to position all nodes
            if (tree.getRoot() != null) {
                Map<Node, Integer> nodeWidths = calculateSubtreeWidths(tree.getRoot());
                positionNode(tree.getRoot(), rootX, rootY, getWidth() / 2, positions, nodeWidths);
            }
            
            return positions;
        }
        
        private Map<Node, Integer> calculateSubtreeWidths(Node node) {
            Map<Node, Integer> widths = new HashMap<>();
            calculateSubtreeWidthRecursive(node, widths);
            return widths;
        }
        
        private int calculateSubtreeWidthRecursive(Node node, Map<Node, Integer> widths) {
            if (node == null) {
                return 0;
            }
            
            if (node.isLeaf()) {
                widths.put(node, 1);
                return 1;
            }
            
            int leftWidth = calculateSubtreeWidthRecursive(node.leftChild, widths);
            int rightWidth = calculateSubtreeWidthRecursive(node.rightChild, widths);
            
            int width = Math.max(1, leftWidth + rightWidth);
            widths.put(node, width);
            return width;
        }
        
        private void positionNode(Node node, double x, double y, double horizontalSpace, 
                                 Map<Node, Point2D> positions, Map<Node, Integer> nodeWidths) {
            if (node == null) {
                return;
            }
            
            positions.put(node, new Point2D.Double(x, y));
            
            if (node.leftChild != null || node.rightChild != null) {
                double childY = y + verticalSpacing;
                
                // Adjust spacing for balanced tree
                double leftSpace = 0;
                double rightSpace = 0;
                
                int totalWidth = nodeWidths.getOrDefault(node, 1);
                int leftWidth = nodeWidths.getOrDefault(node.leftChild, 0);
                int rightWidth = nodeWidths.getOrDefault(node.rightChild, 0);
                
                double spacingFactor = horizontalSpace / totalWidth;
                
                if (node.leftChild != null) {
                    double leftX = x - (spacingFactor * rightWidth);
                    positionNode(node.leftChild, leftX, childY, horizontalSpace / 2, positions, nodeWidths);
                }
                
                if (node.rightChild != null) {
                    double rightX = x + (spacingFactor * leftWidth);
                    positionNode(node.rightChild, rightX, childY, horizontalSpace / 2, positions, nodeWidths);
                }
            }
        }
        
        private void drawNode(Graphics2D g2d, Node node, double x, double y, boolean highlight, boolean swap, boolean isDecodePath) {
            Color nodeColor;
            
            if (highlight) {
                if (isDecodePath) {
                    nodeColor = DECODE_COLOR;
                } else {
                    nodeColor = HIGHLIGHT_COLOR;
                }
            } else if (swap) {
                nodeColor = SWAP_COLOR;
            } else if (node == tree.getNYT()) {
                // NYT always gets the gold color
                nodeColor = NYT_NODE_COLOR;
            } else {
                // Color based on weight
                if (node.weight < WEIGHT_COLORS.length) {
                    nodeColor = WEIGHT_COLORS[node.weight];
                } else {
                    nodeColor = DEFAULT_NODE_COLOR;
                }
            }
            
            // Draw node circle
            g2d.setColor(nodeColor);
            g2d.fill(new Ellipse2D.Double(x - nodeRadius, y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius));
            
            g2d.setColor(Color.BLACK);
            g2d.draw(new Ellipse2D.Double(x - nodeRadius, y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius));
            
            // Add red circle for swapped nodes
            if (swap) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(3.0f));
                g2d.draw(new Ellipse2D.Double(x - nodeRadius - 5, y - nodeRadius - 5, 
                                            2 * (nodeRadius + 5), 2 * (nodeRadius + 5)));
                g2d.setStroke(new BasicStroke(1.0f));
            }
            
            // Draw node text - smaller and more compact
            g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
            String text;
            
            if (node == tree.getNYT()) {
                text = "NYT";
            } else if (node.isLeaf()) {
                // For leaf nodes, show the symbol and small weight
                text = "'" + (char)node.symbol + "'";
                // Draw the weight in smaller font below
                String weightText = "w:" + node.weight;
                String orderText = "#" + node.orderNumber;
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                
                g2d.setColor(Color.BLACK);
                g2d.drawString(text, (float)(x - textWidth / 2), (float)(y));
                
                // Draw weight and order in smaller font
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 8));
                fm = g2d.getFontMetrics();
                int weightWidth = fm.stringWidth(weightText);
                int orderWidth = fm.stringWidth(orderText);
                
                g2d.drawString(weightText, (float)(x - weightWidth / 2), (float)(y + 10));
                g2d.drawString(orderText, (float)(x - orderWidth / 2), (float)(y + 20));
                
                return; // Skip the general text drawing below
            } else {
                // For internal nodes, just show the weight
                text = "w:" + node.weight;
                // Draw the order in smaller font below
                String orderText = "#" + node.orderNumber;
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                
                g2d.setColor(Color.BLACK);
                g2d.drawString(text, (float)(x - textWidth / 2), (float)(y));
                
                // Draw order in smaller font
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 8));
                fm = g2d.getFontMetrics();
                int orderWidth = fm.stringWidth(orderText);
                
                g2d.drawString(orderText, (float)(x - orderWidth / 2), (float)(y + 10));
                
                return; // Skip the general text drawing below
            }
            
            // This code only runs for NYT
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            
            g2d.setColor(Color.BLACK);
            g2d.drawString(text, (float)(x - textWidth / 2), (float)(y + textHeight / 4));
        }
        
        private void drawConnection(Graphics2D g2d, Point2D parent, Point2D child, String edgeLabel, boolean highlight, boolean isDecodePath) {
            double angle = Math.atan2(child.getY() - parent.getY(), child.getX() - parent.getX());
            double parentX = parent.getX() + nodeRadius * Math.cos(angle);
            double parentY = parent.getY() + nodeRadius * Math.sin(angle);
            double childX = child.getX() - nodeRadius * Math.cos(angle);
            double childY = child.getY() - nodeRadius * Math.sin(angle);
            
            // Draw the line
            if (highlight) {
                if (isDecodePath) {
                    g2d.setColor(DECODE_COLOR);
                } else {
                    g2d.setColor(HIGHLIGHT_COLOR);
                }
                g2d.setStroke(new BasicStroke(3));
            } else {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
            }
            
            g2d.draw(new Line2D.Double(parentX, parentY, childX, childY));
            
            // Draw the edge label
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            FontMetrics fm = g2d.getFontMetrics();
            
            // Position label at midpoint
            double midX = (parentX + childX) / 2;
            double midY = (parentY + childY) / 2;
            
            // Position just off the line
            double orthX = (child.getY() - parent.getY()) / 10;  // Perpendicular direction
            double orthY = -(child.getX() - parent.getX()) / 10;
            
            g2d.setColor(Color.BLUE);
            g2d.drawString(edgeLabel, (float)(midX + orthX), (float)(midY + orthY));
            
            // Reset stroke
            g2d.setStroke(new BasicStroke(1));
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingHuffmanVisualizer());
    }
}
