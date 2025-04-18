import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.io.*;
import java.nio.file.Files;

public class AdaptiveHuffmanJUnitTest {
    
    private File inputFile;
    private File compressedFile;
    private File decompressedFile;
    
    @Before
    public void setUp() throws IOException {
        // Create fresh temp files for each test
        inputFile = File.createTempFile("test_input", ".txt");
        compressedFile = File.createTempFile("test_compressed", ".bin");
        decompressedFile = File.createTempFile("test_decompressed", ".txt");
    }
    
    @After
    public void tearDown() {
        // Clean up files after each test
        inputFile.delete();
        compressedFile.delete();
        decompressedFile.delete();
    }
    
    @Test
    public void testEncodeDecodeSimpleString() throws IOException {
        // Arrange
        String testString = "ABRACADABRA";
        Files.write(inputFile.toPath(), testString.getBytes());
        
        // Act
        AdaptiveHuffman.compress(inputFile.getAbsolutePath(), compressedFile.getAbsolutePath());
        AdaptiveHuffman.decompress(compressedFile.getAbsolutePath(), decompressedFile.getAbsolutePath());
        
        // Assert
        String result = new String(Files.readAllBytes(decompressedFile.toPath()));
        assertEquals("Decompressed output should match original", 
                   testString, result);
        
        // Additional metrics
        double ratio = (double)compressedFile.length()/inputFile.length();
        System.out.printf("Compression ratio: %.2f\n", ratio);
    }
    
    @Test
    public void testEncodeDecodeRepeatingPatterns() throws IOException {
        // Arrange
        String testString = "AAABBBCCCAAABBBCCC";
        Files.write(inputFile.toPath(), testString.getBytes());
        
        // Act
        AdaptiveHuffman.compress(inputFile.getAbsolutePath(), compressedFile.getAbsolutePath());
        AdaptiveHuffman.decompress(compressedFile.getAbsolutePath(), decompressedFile.getAbsolutePath());
        
        // Assert
        String result = new String(Files.readAllBytes(decompressedFile.toPath()));
        assertEquals("Repeating patterns should decode correctly",
                   testString, result);
    }
    
    @Test
    public void testLargerTextFile() throws IOException {
        // Arrange
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            largeText.append("Line ").append(i)
                   .append(": The quick brown fox jumps over the lazy dog. ")
                   .append(i % 10).append(i % 5).append(i % 3).append("\n");
        }
        Files.write(inputFile.toPath(), largeText.toString().getBytes());
        
        // Act
        AdaptiveHuffman.compress(inputFile.getAbsolutePath(), compressedFile.getAbsolutePath());
        AdaptiveHuffman.decompress(compressedFile.getAbsolutePath(), decompressedFile.getAbsolutePath());
        
        // Assert
        String result = new String(Files.readAllBytes(decompressedFile.toPath()));
        assertEquals("Large text should decode perfectly",
                   largeText.toString(), result);
    }
    
    @Test
    public void testEncodeDecodeLectureExample() throws IOException {
        // Arrange
        String testString = "ABCCCAAAA";
        Files.write(inputFile.toPath(), testString.getBytes());
        
        // Act
        AdaptiveHuffman.compress(inputFile.getAbsolutePath(), compressedFile.getAbsolutePath());
        AdaptiveHuffman.decompress(compressedFile.getAbsolutePath(), decompressedFile.getAbsolutePath());
        
        // Assert
        String result = new String(Files.readAllBytes(decompressedFile.toPath()));
        assertEquals("Lecture example should decode correctly",
                   testString, result);
    }
    
    @Test(expected = IOException.class)
    public void testInvalidInputFile() throws IOException {
        AdaptiveHuffman.compress("nonexistent.txt", compressedFile.getAbsolutePath());
    }
    
    public static void main(String[] args) {
        // Optional: For running outside JUnit
        org.junit.runner.JUnitCore.main("AdaptiveHuffmanJUnitTest");
    }
}
