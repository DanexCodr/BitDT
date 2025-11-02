package danexcodr.time;

import java.util.*;

/**
 * Example class demonstrating various use cases of BitDT.
 * Shows practical applications and common patterns for using the BitDT library.
 * 
 * @author danexcodr
 * @version 1.0
 */
public class BitDTExample {
    /**
     * Main method demonstrating BitDT usage examples.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        exampleBasicUsage();
        exampleDateTypes();
        exampleTimezones();
        exampleSortingAndComparison();
        exampleStorageEfficiency();
        exampleErrorHandling();
        exampleIntegration();
    }
    
    /**
     * Demonstrates basic BitDT usage patterns.
     */
    public static void exampleBasicUsage() {
        System.out.println("=== Basic Usage ===");
        
        BitDT current = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 45, 123, null);
        System.out.println("Current datetime: " + current.encode());
        
        BitDT dateOnly = BitDT.fromPrimitives(50000, 5, 15, 0, 0, 0, 0, null);
        System.out.println("Date only: " + dateOnly.encode());
        
        BitDT timeOnly = BitDT.fromPrimitives(0, 0, 0, 14, 30, 45, 123, null);
        System.out.println("Time only: " + timeOnly.encode());
        System.out.println();
    }
    
    /**
     * Demonstrates different date type handling.
     */
    public static void exampleDateTypes() {
        System.out.println("=== Date Types ===");
        
        BitDT full = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 45, 123, null);
        BitDT dateOnly = BitDT.fromPrimitives(50000, 5, 15, 0, 0, 0, 0, null);
        BitDT timeOnly = BitDT.fromPrimitives(0, 0, 0, 14, 30, 45, 123, null);
        BitDT empty = BitDT.createEmpty();
        
        System.out.println("Full type: " + full.getDateType());
        System.out.println("Date only type: " + dateOnly.getDateType());
        System.out.println("Time only type: " + timeOnly.getDateType());
        System.out.println("Empty type: " + empty.getDateType());
        System.out.println();
    }
    
    /**
     * Demonstrates timezone handling.
     */
    public static void exampleTimezones() {
        System.out.println("=== Timezones ===");
        
        BitDT utc = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, "+00");
        BitDT est = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, "-05");
        BitDT ist = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, "+05:30");
        
        System.out.println("UTC: " + utc.encode());
        System.out.println("EST: " + est.encode());
        System.out.println("IST: " + ist.encode());
        System.out.println();
    }
    
    /**
     * Demonstrates sorting and comparison operations.
     */
    public static void exampleSortingAndComparison() {
        System.out.println("=== Sorting and Comparison ===");
        
        List<BitDT> events = new ArrayList<BitDT>();
        events.add(BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, null));
        events.add(BitDT.fromPrimitives(50000, 3, 10, 9, 0, 0, 0, null));
        events.add(BitDT.fromPrimitives(50001, 1, 1, 0, 0, 0, 0, null));
        events.add(BitDT.fromPrimitives(50000, 5, 15, 18, 45, 0, 0, null));
        
        List<BitDT> sorted = BitDT.sortByNumericalValue(events);
        
        System.out.println("Sorted events:");
        for (BitDT event : sorted) {
            System.out.println("  " + event.encode());
        }
        
        BitDT first = sorted.get(0);
        BitDT last = sorted.get(sorted.size() - 1);
        System.out.println("First before last: " + first.before(last));
        System.out.println();
    }
    
    /**
     * Demonstrates storage efficiency benefits.
     */
    public static void exampleStorageEfficiency() {
        System.out.println("=== Storage Efficiency ===");
        
        BitDT[] dates = new BitDT[5];
        dates[0] = BitDT.fromPrimitives(50000, 0, 1, 0, 0, 0, 0, null);
        dates[1] = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, null);
        dates[2] = BitDT.fromPrimitives(50000, 11, 30, 23, 59, 59, 999, null);
        dates[3] = BitDT.fromPrimitives(51000, 2, 28, 12, 0, 0, 0, "+08");
        dates[4] = BitDT.createEmpty();
        
        for (int i = 0; i < dates.length; i++) {
            String encoded = dates[i].encode();
            System.out.println("Date " + (i + 1) + ": " + encoded + " (length: " + encoded.length() + ")");
        }
        System.out.println();
    }
    
    /**
     * Demonstrates error handling patterns.
     */
    public static void exampleErrorHandling() {
        System.out.println("=== Error Handling ===");
        
        BitDT invalidDecoded = BitDT.decode("INVALID_STRING");
        System.out.println("Invalid decode result: " + invalidDecoded.encode());
        System.out.println("Is empty: " + invalidDecoded.isEmpty());
        
        try {
            BitDT invalidYear = BitDT.fromPrimitives(300000, 0, 1, 0, 0, 0, 0, null);
        } catch (Exception e) {
            System.out.println("Caught invalid year: " + e.getMessage());
        }
        
        try {
            String invalidMillis = ThousandCounter.encodeMilliseconds(1000);
        } catch (Exception e) {
            System.out.println("Caught invalid milliseconds: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Demonstrates integration with other systems.
     */
    public static void exampleIntegration() {
        System.out.println("=== Integration Examples ===");
        
        long[] numericalValues = new long[3];
        numericalValues[0] = BitDT.fromPrimitives(50000, 0, 1, 0, 0, 0, 0, null).getNumericalValue();
        numericalValues[1] = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, null).getNumericalValue();
        numericalValues[2] = BitDT.fromPrimitives(50001, 0, 1, 0, 0, 0, 0, null).getNumericalValue();
        
        System.out.println("Numerical values for storage:");
        for (long value : numericalValues) {
            System.out.println("  " + value);
        }
        
        List<BitDT> restored = BitDT.fromNumericalArray(numericalValues);
        System.out.println("Restored dates:");
        for (BitDT dt : restored) {
            System.out.println("  " + dt.encode());
        }
        System.out.println();
    }
}