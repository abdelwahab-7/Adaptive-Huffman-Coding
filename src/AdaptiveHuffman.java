import java.io.*;

public class AdaptiveHuffman {
    public static void compress(String inputFileName, String outputFileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
             Encoder encoder = new Encoder(outputFileName)) {
            
            int c;
            while ((c = reader.read()) != -1) {
                encoder.encodeSymbol(c);
            }
        }
    }
    
    public static void decompress(String inputFileName, String outputFileName) throws IOException {
        try (Decoder decoder = new Decoder(inputFileName);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            
            int symbol;
            while ((symbol = decoder. decodeSymbol()) != -1) {
                writer.write((char) symbol);
            }
        }
    }
    
    public static double calculateCompressionRatio(String originalFile, String compressedFile) throws IOException {
        File original = new File(originalFile);
        File compressed = new File(compressedFile);
        
        if (!original.exists() || !compressed.exists()) {
            throw new FileNotFoundException("One or both files not found");
        }
        
        long originalSize = original.length();
        long compressedSize = compressed.length();
        
        return (double) compressedSize / originalSize;
    }
    
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java AdaptiveHuffman [compress|decompress|analyze] inputFile outputFile");
            return;
        }
        
        String operation = args[0].toLowerCase();
        String inputFile = args[1];
        String outputFile = args[2];
        
        try {
            if (operation.equals("compress")) {
                System.out.println("Compressing " + inputFile + " to " + outputFile);
                compress(inputFile, outputFile);
                System.out.println("Compression complete.");
                
                double ratio = calculateCompressionRatio(inputFile, outputFile);
                System.out.printf("Compression ratio: %.2f (%.2f%%)\n", ratio, ratio * 100);
                System.out.println("Original size: " + new File(inputFile).length() + " bytes");
                System.out.println("Compressed size: " + new File(outputFile).length() + " bytes");
                
            } else if (operation.equals("decompress")) {
                System.out.println("Decompressing " + inputFile + " to " + outputFile);
                decompress(inputFile, outputFile);
                System.out.println("Decompression complete.");
                
            } else if (operation.equals("analyze")) {
                double ratio = calculateCompressionRatio(inputFile, outputFile);
                System.out.printf("Compression ratio: %.2f (%.2f%%)\n", ratio, ratio * 100);
                System.out.println("Original size: " + new File(inputFile).length() + " bytes");
                System.out.println("Compressed size: " + new File(outputFile).length() + " bytes");
                
            } else {
                System.out.println("Unknown operation: " + operation);
                System.out.println("Usage: java AdaptiveHuffman [compress|decompress|analyze] inputFile outputFile");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
