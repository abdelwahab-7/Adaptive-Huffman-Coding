import java.io.*;
public class Encoder implements AutoCloseable {
    private HuffmanTree tree;
    private StringBuilder encodedOutput;
    private BitOutputStream output;
    
    public Encoder(String outputFileName) throws IOException {
        tree = new HuffmanTree();
        encodedOutput = new StringBuilder();
        output = new BitOutputStream(new FileOutputStream(outputFileName));
    }
    
    public Encoder(BitOutputStream outputStream) {
        tree = new HuffmanTree();
        encodedOutput = new StringBuilder();
        output = outputStream;
    }
    
    public void encodeSymbol(int symbol) throws IOException {
        if (tree.contains(symbol)) {
            String path = tree.getPathToNode(tree.getNode(symbol));
            writeStringAsPath(path);
        } else {
            String nytPath = tree.getPathToNYT();
            writeStringAsPath(nytPath);
            
            writeASCIIBits(symbol);
        }
        
        tree.update(symbol);
    }
    
    private void writeASCIIBits(int symbol) throws IOException {
        for (int i = 7; i >= 0; i--) {
            output.writeBit((symbol >> i) & 1);
        }
    }
    
    public void encodeString(String text) throws IOException {
        for (int i = 0; i < text.length(); i++) {
            encodeSymbol(text.charAt(i));
        }
    }
    
    private void writeStringAsPath(String path) throws IOException {
        for (int i = 0; i < path.length(); i++) {
            output.writeBit(path.charAt(i) == '1' ? 1 : 0);
        }
    }
    
    public String getEncodedBitString() {
        return encodedOutput.toString();
    }
    
    public void close() throws IOException {
        output.close();
    }
    
    public static class BitOutputStream {
        private OutputStream out;
        private int buffer;
        private int bitsInBuffer;
        
        public BitOutputStream(OutputStream out) {
            this.out = out;
            buffer = 0;
            bitsInBuffer = 0;
        }
        
        public void writeBit(int bit) throws IOException {
            buffer = (buffer << 1) | (bit & 1);
            bitsInBuffer++;
            
            if (bitsInBuffer == 8) {
                out.write(buffer);
                buffer = 0;
                bitsInBuffer = 0;
            }
        }
        
        public void close() throws IOException {
            if (bitsInBuffer > 0) {
                buffer = buffer << (8 - bitsInBuffer);
                out.write(buffer);
            }
            out.close();
        }
    }
}
