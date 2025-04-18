import java.io.*;

public class Decoder implements AutoCloseable {
    private HuffmanTree tree;
    private BitInputStream input;
    private StringBuilder decodedOutput;
    
    public Decoder(String inputFileName) throws IOException {
        tree = new HuffmanTree();
        input = new BitInputStream(new FileInputStream(inputFileName));
        decodedOutput = new StringBuilder();
    }
    
    public Decoder(BitInputStream inputStream) {
        tree = new HuffmanTree();
        input = inputStream;
        decodedOutput = new StringBuilder();
    }
    
    public int decodeSymbol() throws IOException {
        Node currentNode = tree.getRoot();
        
        while (!currentNode.isLeaf() && currentNode != tree.getNYT()) {
            int bit = input.readBit();
            if (bit == -1) return -1;
            
            if (bit == 0) {
                currentNode = currentNode.leftChild;
            } else {
                currentNode = currentNode.rightChild;
            }
        }
        
        int symbol;
        if (currentNode == tree.getNYT()) {
            symbol = readASCIIBits();
            if (symbol == -1) return -1; 
        } else {
            symbol = currentNode.symbol;
        }
        
        tree.update(symbol);
        return symbol;
    }
    
    private int readASCIIBits() throws IOException {
        int symbol = 0;
        for (int i = 0; i < 8; i++) {
            int bit = input.readBit();
            if (bit == -1) return -1; 
            symbol = (symbol << 1) | bit;
        }
        return symbol;
    }
    
    public String decode() throws IOException {
        int symbol;
        while ((symbol = decodeSymbol()) != -1) {
            decodedOutput.append((char) symbol);
        }
        return decodedOutput.toString();
    }
    
    public void close() throws IOException {
        input.close();
    }
    
    public static class BitInputStream {
        private InputStream in;
        private int buffer;
        private int bitsLeft;
        
        public BitInputStream(InputStream in) {
            this.in = in;
            buffer = 0;
            bitsLeft = 0;
        }
        
        public int readBit() throws IOException {
            if (bitsLeft == 0) {
                buffer = in.read();
                if (buffer == -1) return -1;
                bitsLeft = 8;
            }
            
            bitsLeft--;
            return (buffer >> bitsLeft) & 1;
        }
        
        public void close() throws IOException {
            in.close();
        }
    }
}


