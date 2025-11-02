package danexcodr.time;

import java.util.*;

/**
 * Comprehensive tester for BitDTEpoch functionality
 * Validates all modes, bases, and edge cases
 * 
 * @author danexcodr
 * @version 1.0
 */
public class BitDTEpochTest {
    
    public static void main(String[] args) {
        System.out.println("=== BitDTEpoch Comprehensive Test ===\n");
        
        testNowFunction();
        testBasicFunctionality();
        testBaseEncodings();
        testTimezoneSupport();
        testAutoDetection();
        testEdgeCases();
        testPerformanceComparison();
        testBackwardCompatibility();
        
        System.out.println("=== All Tests Completed ===");
    }
    
    // Simple test to verify now() is working
public static void testNowFunction() {
    long start = System.currentTimeMillis();
    String nowEncoded = BitDTEpoch.now();
    long decoded = BitDTEpoch.fromBitDT(nowEncoded);
    long end = System.currentTimeMillis();
    
    System.out.println("Now() Test:");
    System.out.println("  Encoded: " + nowEncoded);
    System.out.println("  Decoded: " + decoded);
    System.out.println("  Current: " + start);
    System.out.println("  Difference: " + (decoded - start) + " ms");
    
    // Allow for small timing differences (up to 100ms)
    if (Math.abs(decoded - start) < 100) {
        System.out.println("  ✅ Now() function working correctly");
    } else {
        System.out.println("  ❌ Now() function has issues");
    }
}
    
    /**
     * Tests basic encoding/decoding functionality
     */
    public static void testBasicFunctionality() {
        System.out.println("1. BASIC FUNCTIONALITY TEST");
        System.out.println("===========================");
        
        long now = System.currentTimeMillis();
        long testTime = 1718323456789L; // Fixed timestamp for consistent testing
        
        // Test default auto mode
        String autoEncoded = BitDTEpoch.toBitDT(testTime);
        long autoDecoded = BitDTEpoch.fromBitDT(autoEncoded);
        System.out.printf("Auto mode: %s -> %d : %s%n", 
            autoEncoded, autoDecoded, (testTime == autoDecoded ? "✅ PASS" : "❌ FAIL"));
        
        // Test full BitDT mode
        String fullEncoded = BitDTEpoch.toBitDT(testTime, null, BitDTEpoch.MODE_FULL_BITDT);
        long fullDecoded = BitDTEpoch.fromBitDT(fullEncoded);
        System.out.printf("Full mode: %s -> %d : %s%n", 
            fullEncoded, fullDecoded, (testTime == fullDecoded ? "✅ PASS" : "❌ FAIL"));
        
        // Test current time
        String nowEncoded = BitDTEpoch.now();
        long nowDecoded = BitDTEpoch.fromBitDT(nowEncoded);
        System.out.printf("Now(): %s -> %d : %s%n", 
            nowEncoded, nowDecoded, (Math.abs(now - nowDecoded) < 1000 ? "✅ PASS" : "❌ FAIL"));
        
        System.out.println();
    }
    
    /**
     * Tests all base encodings from 2 to 36
     */
    public static void testBaseEncodings() {
        System.out.println("2. BASE ENCODING TEST");
        System.out.println("=====================");
        
        long testTime = 1718323456789L;
        
        // Test common bases
        // In testBaseEncodings() - only test supported bases
int[] testBases = {2, 8, 10, 16, 32, 36}; // Remove 58, 62

        for (int base : testBases) {
            try {
                String encoded = BitDTEpoch.toBitDT(testTime, base);
                long decoded = BitDTEpoch.fromBitDT(encoded, base);
                boolean pass = testTime == decoded;
                
                System.out.printf("Base %2d: %-20s -> %d : %s%n", 
                    base, encoded, decoded, (pass ? "✅ PASS" : "❌ FAIL"));
                    
                if (!pass) {
                    System.out.printf("  Expected: %d, Got: %d%n", testTime, decoded);
                }
            } catch (Exception e) {
                System.out.printf("Base %2d: ❌ ERROR - %s%n", base, e.getMessage());
            }
        }
        
        System.out.println();
    }
    
    /**
     * Tests timezone support
     */
    public static void testTimezoneSupport() {
        System.out.println("3. TIMEZONE SUPPORT TEST");
        System.out.println("========================");
        
        long testTime = 1718323456789L; // June 13, 2024 14:30:56 UTC
        
        // In testTimezoneSupport() - use proper timezone formats
String[] timezones = {null, "UTC", "+00", "+08", "-05", "+0530"}; // Use ±HHMM format instead of ±HH:MM
        
        
        for (String tz : timezones) {
            try {
                String encoded = BitDTEpoch.toBitDT(testTime, tz, BitDTEpoch.MODE_FULL_BITDT);
                long decoded = BitDTEpoch.fromBitDT(encoded);
                
                // With timezones, we expect the same UTC epoch time back
                boolean pass = testTime == decoded;
                String tzDisplay = (tz == null ? "null" : tz);
                
                System.out.printf("TZ %-6s: %-18s -> %d : %s%n", 
                    tzDisplay, encoded, decoded, (pass ? "✅ PASS" : "❌ FAIL"));
                    
            } catch (Exception e) {
                System.out.printf("TZ %s: ❌ ERROR - %s%n", tz, e.getMessage());
            }
        }
        
        System.out.println();
    }
    
    /**
     * Tests auto-detection capabilities
     */
    public static void testAutoDetection() {
        System.out.println("4. AUTO-DETECTION TEST");
        System.out.println("======================");
        
        long testTime = 1718323456789L;
        
        // Create encodings in different bases
        String base36 = BitDTEpoch.toBitDT(testTime, 36);  // "KX3VW7A"
        String base32 = BitDTEpoch.toBitDT(testTime, 32);  // "1V5Q7Q7R"
        String base16 = BitDTEpoch.toBitDT(testTime, 16);  // "18F45A3A6B5"
        String fullBitDT = BitDTEpoch.toBitDT(testTime, null, BitDTEpoch.MODE_FULL_BITDT);
        
        // Test auto-detection
        System.out.printf("Base36 auto-detect: %s -> %d : %s%n", 
            base36, BitDTEpoch.fromBitDT(base36), 
            (testTime == BitDTEpoch.fromBitDT(base36) ? "✅ PASS" : "❌ FAIL"));
            
        System.out.printf("Base32 auto-detect: %s -> %d : %s%n", 
            base32, BitDTEpoch.fromBitDT(base32), 
            (testTime == BitDTEpoch.fromBitDT(base32) ? "✅ PASS" : "❌ FAIL"));
            
        System.out.printf("Base16 auto-detect: %s -> %d : %s%n", 
            base16, BitDTEpoch.fromBitDT(base16), 
            (testTime == BitDTEpoch.fromBitDT(base16) ? "✅ PASS" : "❌ FAIL"));
            
        System.out.printf("Full BitDT auto-detect: %s -> %d : %s%n", 
            fullBitDT, BitDTEpoch.fromBitDT(fullBitDT), 
            (testTime == BitDTEpoch.fromBitDT(fullBitDT) ? "✅ PASS" : "❌ FAIL"));
        
        System.out.println();
    }
    
    /**
     * Tests edge cases and error handling
     */
    public static void testEdgeCases() {
        System.out.println("5. EDGE CASES TEST");
        System.out.println("==================");
        
        // Test invalid inputs
        System.out.printf("Null input decode: %d : %s%n", 
            BitDTEpoch.fromBitDT(null), 
            (BitDTEpoch.fromBitDT(null) == -1 ? "✅ PASS" : "❌ FAIL"));
            
        System.out.printf("Empty input decode: %d : %s%n", 
            BitDTEpoch.fromBitDT(""), 
            (BitDTEpoch.fromBitDT("") == -1 ? "✅ PASS" : "❌ FAIL"));
            
        System.out.printf("Invalid base: %s : %s%n", 
            BitDTEpoch.toBitDT(123456789L, 99), // Invalid base
            (BitDTEpoch.toBitDT(123456789L, 99).length() > 0 ? "✅ PASS" : "❌ FAIL"));
        
        // Test very old and future dates
        long ancient = -62135596800000L; // January 1, 0001 UTC
        long future = 253402300799000L;  // December 31, 9999 UTC
        
        System.out.printf("Ancient date: %s -> %d : %s%n", 
            BitDTEpoch.toBitDT(ancient, BitDTEpoch.MODE_FULL_BITDT),
            BitDTEpoch.fromBitDT(BitDTEpoch.toBitDT(ancient, BitDTEpoch.MODE_FULL_BITDT)),
            (ancient == BitDTEpoch.fromBitDT(BitDTEpoch.toBitDT(ancient, BitDTEpoch.MODE_FULL_BITDT)) ? "✅ PASS" : "❌ FAIL"));
            
        System.out.printf("Future date: %s -> %d : %s%n", 
            BitDTEpoch.toBitDT(future, BitDTEpoch.MODE_FULL_BITDT),
            BitDTEpoch.fromBitDT(BitDTEpoch.toBitDT(future, BitDTEpoch.MODE_FULL_BITDT)),
            (future == BitDTEpoch.fromBitDT(BitDTEpoch.toBitDT(future, BitDTEpoch.MODE_FULL_BITDT)) ? "✅ PASS" : "❌ FAIL"));
        
        System.out.println();
    }
    
    /**
     * Tests performance and size comparison
     */
    public static void testPerformanceComparison() {
        System.out.println("6. PERFORMANCE COMPARISON");
        System.out.println("=========================");
        
        long testTime = System.currentTimeMillis();
        int iterations = 10000;
        
        // Size comparison
        String base36 = BitDTEpoch.toBitDT(testTime, 36);
        String base62 = BitDTEpoch.toBitDT(testTime, 62);
        String full = BitDTEpoch.toBitDT(testTime, null, BitDTEpoch.MODE_FULL_BITDT);
        String auto = BitDTEpoch.toBitDT(testTime);
        
        System.out.println("Size Comparison:");
        System.out.printf("  Base36:  %s (%d chars)%n", base36, base36.length());
        System.out.printf("  Base62:  %s (%d chars)%n", base62, base62.length());
        System.out.printf("  Full:    %s (%d chars)%n", full, full.length());
        System.out.printf("  Auto:    %s (%d chars)%n", auto, auto.length());
        
        // Speed test - Base36
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            BitDTEpoch.toBitDT(testTime + i, 36);
        }
        long base36Time = System.nanoTime() - start;
        
        // Speed test - Full BitDT
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            BitDTEpoch.toBitDT(testTime + i, null, BitDTEpoch.MODE_FULL_BITDT);
        }
        long fullTime = System.nanoTime() - start;
        
        // Speed test - Auto
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            BitDTEpoch.toBitDT(testTime + i);
        }
        long autoTime = System.nanoTime() - start;
        
        System.out.println("Performance (lower is better):");
        System.out.printf("  Base36:  %,.3f ms%n", base36Time / 1_000_000.0);
        System.out.printf("  Full:    %,.3f ms%n", fullTime / 1_000_000.0);
        System.out.printf("  Auto:    %,.3f ms%n", autoTime / 1_000_000.0);
        System.out.printf("  Full/Base36 ratio: %.2fx%n", (double)fullTime / base36Time);
        
        System.out.println();
    }
    
    /**
     * Tests backward compatibility with the original user's approach
     */
    public static void testBackwardCompatibility() {
        System.out.println("7. BACKWARD COMPATIBILITY TEST");
        System.out.println("==============================");
        
        // Recreate the exact user's original implementation
        long testTime = 1718323456789L;
        
        // Their original code
        String theirOriginal = Long.toString(testTime, 36).toUpperCase();
        long theirDecoded = Long.parseLong(theirOriginal.toLowerCase(), 36);
        
        // Our compatible version
        String ourVersion = BitDTEpoch.toBitDT(testTime, 36);
        long ourDecoded = BitDTEpoch.fromBitDT(ourVersion, 36);
        
        System.out.printf("Their original: %s -> %d%n", theirOriginal, theirDecoded);
        System.out.printf("Our version:    %s -> %d%n", ourVersion, ourDecoded);
        System.out.printf("Compatibility: %s%n", 
            (theirOriginal.equals(ourVersion) && theirDecoded == ourDecoded ? "✅ PERFECT MATCH" : "❌ BROKEN"));
        
        // Test that their exact use case still works
        String theirStyle = BitDTEpoch.toBitDT(testTime, 36);
        long decoded = BitDTEpoch.fromBitDT(theirStyle);
        System.out.printf("Their use case: %s -> %d : %s%n", 
            theirStyle, decoded, (testTime == decoded ? "✅ WORKS" : "❌ BROKEN"));
        
        System.out.println();
        
        // Run the built-in benchmark
        System.out.println("8. BUILT-IN BENCHMARK");
        System.out.println("=====================");
        BitDTEpoch.benchmark(testTime);
    }
}