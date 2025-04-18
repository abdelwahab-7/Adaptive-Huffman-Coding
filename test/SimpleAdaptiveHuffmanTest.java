import java.io.*;

public class SimpleAdaptiveHuffmanTest {
    
    // Simple test with very small input
    public static void testSimpleHello() throws IOException {
        // Input test string
        String testString = "hello";
        
        // Create a temporary input file
        File inputFile = File.createTempFile("test_input", ".txt");
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write(testString);
        }
        
        // Create a temporary output file for compression
        File compressedFile = File.createTempFile("test_compressed", ".bin");
        
        // Create a temporary output file for decompression
        File decompressedFile = File.createTempFile("test_decompressed", ".txt");
        
        System.out.println("Input file: " + inputFile.getAbsolutePath());
        System.out.println("Compressed file: " + compressedFile.getAbsolutePath());
        System.out.println("Decompressed file: " + decompressedFile.getAbsolutePath());
        
        // Compress the input file
        System.out.println("Compressing...");
        AdaptiveHuffman.compress(inputFile.getAbsolutePath(), compressedFile.getAbsolutePath());
        
        // Decompress the compressed file
        System.out.println("Decompressing...");
        AdaptiveHuffman.decompress(compressedFile.getAbsolutePath(), decompressedFile.getAbsolutePath());
        
        // Read the decompressed file
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(decompressedFile))) {
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char) c);
            }
        }
        
        // Check that the decompressed string matches the original
        System.out.println("Original: " + testString);
        System.out.println("Decompressed: " + result.toString());
        
        if (testString.equals(result.toString())) {
            System.out.println("Test passed: Decompressed output matches original input");
        } else {
            System.out.println("Test failed: Expected \"" + testString + "\" but got \"" + result.toString() + "\"");
        }
        
        // Calculate compression ratio
        double originalSize = inputFile.length();
        double compressedSize = compressedFile.length();
        double compressionRatio = compressedSize / originalSize;
        
        System.out.println("Original size: " + originalSize + " bytes");
        System.out.println("Compressed size: " + compressedSize + " bytes");
        System.out.println("Compression ratio: " + compressionRatio);
        
        // Clean up temporary files
        inputFile.delete();
        compressedFile.delete();
        decompressedFile.delete();
    }

    public static void main(String[] args) {
        try {
            System.out.println("Running Adaptive Huffman Coding test with small input...");
            testSimpleHello();
            System.out.println("Test completed.");
        } catch (IOException e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
}