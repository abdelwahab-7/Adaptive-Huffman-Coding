import java.io.*;

public class AdaptiveHuffmanTest {
    
    public static void testEncodeDecodeSimpleString() throws IOException {
        String testString = "ABRCRDDARBAR";
        
        File inputFile = File.createTempFile("test_input", ".txt");
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write(testString);
        }
        
        File compressedFile = File.createTempFile("test_compressed", ".bin");
        
        File decompressedFile = File.createTempFile("test_decompressed", ".txt");
        
        System.out.println("Compressing...");
        AdaptiveHuffman.compress(inputFile.getAbsolutePath(), compressedFile.getAbsolutePath());
        
        System.out.println("Decompressing...");
        AdaptiveHuffman.decompress(compressedFile.getAbsolutePath(), decompressedFile.getAbsolutePath());
        
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(decompressedFile))) {
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char) c);
            }
        }
        //StringBuilder resultt = new StringBuilder(result);
        //resultt.deleteCharAt(resultt.length() - 1);
        //result = resultt;
        System.out.println("Original: " + testString);
        System.out.println("Decompressed: " + result.toString());
        
        if (testString.equals(result.toString())) {
            System.out.println("Test passed: Decompressed output matches original input");
        } else {
            System.out.println("Test failed: Expected \"" + testString + "\" but got \"" + result.toString() + "\"");
        }
        
        double originalSize = inputFile.length();
        double compressedSize = compressedFile.length();
        double compressionRatio = compressedSize / originalSize;
        
        System.out.println("Test case: Simple string");
        System.out.println("Original size: " + originalSize + " bytes");
        System.out.println("Compressed size: " + compressedSize + " bytes");
        System.out.println("Compression ratio: " + compressionRatio);
        
        inputFile.delete();
        compressedFile.delete();
        decompressedFile.delete();
    }
    
    public static void testEncodeDecodeRepeatingPatterns() throws IOException {
        String testString = "AAABBBCCCAAABBBCCC";
        
        File inputFile = File.createTempFile("test_input_patterns", ".txt");
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write(testString);
        }
        
        File compressedFile = File.createTempFile("test_compressed_patterns", ".bin");
        
        File decompressedFile = File.createTempFile("test_decompressed_patterns", ".txt");
        
        System.out.println("Compressing...");
        AdaptiveHuffman.compress(inputFile.getAbsolutePath(), compressedFile.getAbsolutePath());
        
        System.out.println("Decompressing...");
        AdaptiveHuffman.decompress(compressedFile.getAbsolutePath(), decompressedFile.getAbsolutePath());
        
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(decompressedFile))) {
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char) c);
            }
        }
        
        System.out.println("Original: " + testString);
        System.out.println("Decompressed: " + result.toString());
        
        if (testString.equals(result.toString())) {
            System.out.println("Test passed: Decompressed output matches original input");
        } else {
            System.out.println("Test failed: Expected \"" + testString + "\" but got \"" + result.toString() + "\"");
        }
        
        double originalSize = inputFile.length();
        double compressedSize = compressedFile.length();
        double compressionRatio = compressedSize / originalSize;
        
        System.out.println("Test case: Repeating patterns");
        System.out.println("Original size: " + originalSize + " bytes");
        System.out.println("Compressed size: " + compressedSize + " bytes");
        System.out.println("Compression ratio: " + compressionRatio);
        
        inputFile.delete();
        compressedFile.delete();
        decompressedFile.delete();
    }
    
    public static void testLargerTextFile() throws IOException {
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 20; i++) { 
            largeText.append("Line ").append(i).append(": The quick brown fox jumps over the lazy dog. ");
            largeText.append(i % 10).append(i % 5).append(i % 3).append("\n");
        }
        
        File inputFile = File.createTempFile("test_input_large", ".txt");
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write(largeText.toString());
        }
        
        File compressedFile = File.createTempFile("test_compressed_large", ".bin");
        
        File decompressedFile = File.createTempFile("test_decompressed_large", ".txt");
        
        System.out.println("Compressing...");
        AdaptiveHuffman.compress(inputFile.getAbsolutePath(), compressedFile.getAbsolutePath());
        
        System.out.println("Decompressing...");
        AdaptiveHuffman.decompress(compressedFile.getAbsolutePath(), decompressedFile.getAbsolutePath());
        
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(decompressedFile))) {
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char) c);
            }
        }
        
        boolean matches = largeText.toString().equals(result.toString());
        System.out.println("Content match: " + (matches ? "Yes" : "No"));
        
        if (matches) {
            System.out.println("Test passed: Decompressed output matches original input");
        } else {
            System.out.println("Test failed: Decompressed content doesn't match original");
        }
        
        double originalSize = inputFile.length();
        double compressedSize = compressedFile.length();
        double compressionRatio = compressedSize / originalSize;
        
        System.out.println("Test case: Larger text file");
        System.out.println("Original size: " + originalSize + " bytes");
        System.out.println("Compressed size: " + compressedSize + " bytes");
        System.out.println("Compression ratio: " + compressionRatio);
        
        inputFile.delete();
        compressedFile.delete();
        decompressedFile.delete();
    }

    public static void testEncodeDecodeLectureExample() throws IOException {
        String testString = "ABCCCAAAA";
        
        File inputFile = File.createTempFile("test_input", ".txt");
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write(testString);
        }
        
        File compressedFile = File.createTempFile("test_compressed", ".bin");
        
        File decompressedFile = File.createTempFile("test_decompressed", ".txt");
        
        System.out.println("Compressing...");
        AdaptiveHuffman.compress(inputFile.getAbsolutePath(), compressedFile.getAbsolutePath());
        
        System.out.println("Decompressing...");
        AdaptiveHuffman.decompress(compressedFile.getAbsolutePath(), decompressedFile.getAbsolutePath());
        
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(decompressedFile))) {
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char) c);
            }
        }
        
        System.out.println("Original: " + testString);
        System.out.println("Decompressed: " + result.toString());
        
        if (testString.equals(result.toString())) {
            System.out.println("Test passed: Decompressed output matches original input");
        } else {
            System.out.println("Test failed: Expected \"" + testString + "\" but got \"" + result.toString() + "\"");
        }
        
        double originalSize = inputFile.length();
        double compressedSize = compressedFile.length();
        double compressionRatio = compressedSize / originalSize;
        
        System.out.println("Test case: Simple string");
        System.out.println("Original size: " + originalSize + " bytes");
        System.out.println("Compressed size: " + compressedSize + " bytes");
        System.out.println("Compression ratio: " + compressionRatio);
        
        inputFile.delete();
        compressedFile.delete();
        decompressedFile.delete();
    }
    
    
    public static void main(String[] args) {
        try {
            System.out.println("Running Adaptive Huffman Coding tests...\n");
            
            System.out.println("\n=== Test 1: Simple String ===");
            testEncodeDecodeSimpleString();
            
            System.out.println("\n=== Test 2: Repeating Patterns ===");
            testEncodeDecodeRepeatingPatterns();
            
            System.out.println("\n=== Test 3: Lecture Text File ===");
            testEncodeDecodeLectureExample();

            System.out.println("\n=== Test 4: Larger Text File ===");
            testLargerTextFile();

            
            System.out.println("\nAll tests completed.");
        } catch (IOException e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
