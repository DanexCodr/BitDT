package danexcodr.time;

import java.util.*;

/**
 * Test class for BitDT functionality.
 * Provides comprehensive testing of encoding, decoding, and various operations
 * on BitDT instances.
 * 
 * @author danexcodr
 * @version 1.0
 */
public class BitDTTest {
    /**
     * Main method to run BitDT tests.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        testBasicEncodingDecoding();
        testEdgeCases();
        testSorting();
        testNumericalOperations();
        testThousandCounter();
        testYearEncoding();
        testTimezone();
        testEmptyAndNull();
        testCompression();
        testInvalidInputs();
        
        System.out.println("All tests completed.");
    }
    
    /**
     * Tests basic encoding and decoding functionality.
     */
    public static void testBasicEncodingDecoding() {
        System.out.println("=== Testing Basic Encoding/Decoding ===");
        
        BitDT original = BitDT.fromPrimitives(50000, 0, 1, 12, 30, 45, 123, null);
        String encoded = original.encode();
        BitDT decoded = BitDT.decode(encoded);
        
        System.out.println("Original: " + original.encode());
        System.out.println("Decoded: " + decoded.encode());
        System.out.println("Match: " + original.equals(decoded));
        System.out.println();
    }
    
    /**
     * Tests edge cases and boundary conditions.
     */
    public static void testEdgeCases() {
        System.out.println("=== Testing Edge Cases ===");
        
        BitDT minDate = BitDT.fromPrimitives(0, 0, 1, 0, 0, 0, 0, null);
        BitDT maxDate = BitDT.fromPrimitives(226980, 11, 31, 23, 59, 59, 999, null);
        
        System.out.println("Min date: " + minDate.encode());
        System.out.println("Max date: " + maxDate.encode());
        System.out.println();
    }
    
    /**
     * Tests sorting functionality.
     */
    public static void testSorting() {
        System.out.println("=== Testing Sorting ===");
        
        List<BitDT> dates = new ArrayList<BitDT>();
        dates.add(BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, null));
        dates.add(BitDT.fromPrimitives(50000, 3, 10, 9, 0, 0, 0, null));
        dates.add(BitDT.fromPrimitives(50001, 1, 1, 0, 0, 0, 0, null));
        
        List<BitDT> sorted = BitDT.sortByNumericalValue(dates);
        
        System.out.println("Original order:");
        for (BitDT dt : dates) {
            System.out.println(dt.encode());
        }
        
        System.out.println("Sorted order:");
        for (BitDT dt : sorted) {
            System.out.println(dt.encode());
        }
        System.out.println();
    }
    
    /**
     * Tests numerical operations and conversions.
     */
    public static void testNumericalOperations() {
        System.out.println("=== Testing Numerical Operations ===");
        
        BitDT dt = BitDT.fromPrimitives(50000, 6, 20, 15, 45, 30, 500, null);
        long numerical = dt.getNumericalValue();
        BitDT fromNumerical = BitDT.fromNumericalValue(numerical);
        
        System.out.println("Original: " + dt.encode());
        System.out.println("From numerical: " + fromNumerical.encode());
        System.out.println("Match: " + dt.equals(fromNumerical));
        System.out.println();
    }
    
    /**
     * Tests ThousandCounter encoding and decoding.
     */
    public static void testThousandCounter() {
        System.out.println("=== Testing ThousandCounter ===");
        
        for (int i = 0; i <= 999; i += 100) {
            String encoded = ThousandCounter.encodeMilliseconds(i);
            int decoded = ThousandCounter.decodeMilliseconds(encoded);
            System.out.println(i + " -> " + encoded + " -> " + decoded + " : " + (i == decoded));
        }
        System.out.println();
    }
    
    /**
     * Tests year encoding and decoding.
     */
    public static void testYearEncoding() {
        System.out.println("=== Testing Year Encoding ===");
        
        int[] testYears = {0, 50000, 100000, 150000, 200000, 226980};
        for (int year : testYears) {
            String encoded = BitDT.encodeYear(year);
            int decoded = BitDT.decodeYear(encoded);
            System.out.println(year + " -> " + encoded + " -> " + decoded + " : " + (year == decoded));
        }
        System.out.println();
    }
    
    /**
     * Tests timezone functionality.
     */
    public static void testTimezone() {
        System.out.println("=== Testing Timezone ===");
        
        BitDT withTz = BitDT.fromPrimitives(50000, 0, 1, 12, 0, 0, 0, "+05:30");
        String encoded = withTz.encode();
        BitDT decoded = BitDT.decode(encoded);
        
        System.out.println("With timezone: " + encoded);
        System.out.println("Decoded timezone: " + decoded.getTimezone());
        System.out.println();
    }
    
    /**
     * Tests empty and null handling.
     */
    public static void testEmptyAndNull() {
        System.out.println("=== Testing Empty and Null ===");
        
        BitDT empty = BitDT.createEmpty();
        BitDT fromNull = BitDT.decode(null);
        BitDT fromEmptyString = BitDT.decode("");
        
        System.out.println("Empty: " + empty.encode());
        System.out.println("From null: " + fromNull.encode());
        System.out.println("From empty string: " + fromEmptyString.encode());
        System.out.println("All empty: " + (empty.isEmpty() && fromNull.isEmpty() && fromEmptyString.isEmpty()));
        System.out.println();
    }
    
    /**
     * Tests zero compression functionality.
     */
    public static void testCompression() {
        System.out.println("=== Testing Compression ===");
        
        BitDT allZeros = BitDT.fromPrimitives(0, 0, 0, 0, 0, 0, 0, null);
        BitDT manyZeros = BitDT.fromPrimitives(0, 0, 1, 0, 0, 0, 0, null);
        
        System.out.println("All zeros: " + allZeros.encode());
        System.out.println("Many zeros: " + manyZeros.encode());
        System.out.println();
    }
    
    /**
     * Tests invalid input handling.
     */
    public static void testInvalidInputs() {
        System.out.println("=== Testing Invalid Inputs ===");
        
        try {
            BitDT invalid = BitDT.fromPrimitives(-1, 0, 1, 0, 0, 0, 0, null);
            System.out.println("Should not reach here");
        } catch (Exception e) {
            System.out.println("Correctly caught invalid year: " + e.getMessage());
        }
        
        try {
            BitDT invalid = BitDT.fromPrimitives(0, 12, 1, 0, 0, 0, 0, null);
            System.out.println("Should not reach here");
        } catch (Exception e) {
            System.out.println("Correctly caught invalid month: " + e.getMessage());
        }
        
        BitDT invalidDecoded = BitDT.decode("INVALID");
        System.out.println("Invalid decoding gives empty: " + invalidDecoded.isEmpty());
        System.out.println();
    }
}