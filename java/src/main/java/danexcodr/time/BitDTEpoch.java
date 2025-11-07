package danexcodr.time;

import java.time.*;
import java.time.temporal.ChronoField;

/**
 * BitDTEpoch provides flexible epoch-based utilities for the BitDT encoding system.
 * Supports multiple encoding bases (2-36) for maximum flexibility, plus smart auto-detection.
 * 
 * <p>Key features:
 * <ul>
 * <li>Custom base encoding (2-36) for simple epoch timestamps</li>
 * <li>Smart auto-mode that chooses the optimal encoding</li>
 * <li>Full BitDT beast mode for timezone-aware dates</li>
 * <li>Automatic format detection during decoding</li>
 * </ul>
 * 
 * @author danexcodr
 * @version 1.1
 * @see BitDT
 */
public class BitDTEpoch {
    
    // Mode constants
    public static final int MODE_AUTO = 0;      // Smart detection
    public static final int MODE_BASE36 = 36;   // Their original approach
    public static final int MODE_FULL_BITDT = -1; // Always use full BitDT
    
    private static final String BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    /**
     * Simple epoch-to-string conversion with auto mode.
     * 
     * @param epochMillis the epoch time in milliseconds
     * @return compact string representation (auto-detected best approach)
     */
    public static String toBitDT(long epochMillis) {
        return toBitDT(epochMillis, null, MODE_AUTO);
    }
    
    /**
     * Epoch-to-string conversion with custom base.
     * 
     * @param epochMillis the epoch time in milliseconds
     * @param base the numerical base (2-36), or MODE_AUTO for smart detection
     * @return compact string representation
     */
    public static String toBitDT(long epochMillis, int base) {
        return toBitDT(epochMillis, null, base);
    }
    
    /**
     * Full-featured epoch conversion with timezone and base selection.
     * 
     * @param epochMillis the epoch time in milliseconds
     * @param timezone the timezone string (e.g., "+08", "-05:30") or null for UTC
     * @param base the numerical base (2-36), MODE_AUTO, or MODE_FULL_BITDT
     * @return compact string representation
     */
    public static String toBitDT(long epochMillis, String timezone, int base) {
        try {
            // Handle special modes
            if (base == MODE_FULL_BITDT) {
                return toFullBitDT(epochMillis, timezone);
            }
            
            if (base == MODE_AUTO) {
                return toSmartBitDT(epochMillis, timezone);
            }
            
            // User-specified base encoding (2-36)
            if (base >= 2 && base <= 36) {
                return encodeBase(epochMillis, base);
            }
            
            // Invalid base, fallback to auto mode
            return toSmartBitDT(epochMillis, timezone);
            
        } catch (Exception e) {
            return BitDT.createEmpty().encode();
        }
    }
    
    /**
     * Smart decoding that automatically detects the encoding format.
     * 
     * @param bitdt the encoded string to decode
     * @return epoch time in milliseconds, or -1 if decoding fails
     */
    public static long fromBitDT(String bitdt) {
        if (bitdt == null || bitdt.isEmpty()) {
            return -1;
        }
        
        try {
            // Try to detect if it's base-encoded first (faster)
            if (isBaseEncoded(bitdt)) {
                return decodeBaseAuto(bitdt);
            }
            
            // Otherwise, try full BitDT decoding
            return fromFullBitDT(bitdt);
            
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * Explicit decoding with known base.
     * 
     * @param bitdt the base-encoded string
     * @param base the numerical base used for encoding (2-36)
     * @return epoch time in milliseconds, or -1 if decoding fails
     */
    public static long fromBitDT(String bitdt, int base) {
        if (bitdt == null || bitdt.isEmpty()) {
            return -1;
        }
        
        try {
            if (base >= 2 && base <= 36) {
                return Long.parseLong(bitdt.toLowerCase(), base);
            } else {
                return fromFullBitDT(bitdt);
            }
        } catch (Exception e) {
            return -1;
        }
    }
    
    // ===== PRIVATE IMPLEMENTATION METHODS =====
    
private static String toSmartBitDT(long epochMillis, String timezone) {
    // Use base encoding for UTC or null timezone, full BitDT for specific timezones
    if (timezone == null || "UTC".equals(timezone) || "+00".equals(timezone) || 
        "Z".equals(timezone) || "+0000".equals(timezone)) {
        return encodeBase(epochMillis, MODE_BASE36);
    } else {
        return toFullBitDT(epochMillis, timezone);
    }
}
    
    private static String toFullBitDT(long epochMillis, String timezone) {
        Instant instant = Instant.ofEpochMilli(epochMillis);
        
        // Handle timezone conversion
        ZonedDateTime zdt;
        if (timezone != null && !timezone.equals("UTC")) {
            ZoneOffset offset = parseZoneOffset(timezone);
            zdt = instant.atZone(offset);
        } else {
            zdt = instant.atZone(ZoneOffset.UTC);
        }
        
        // Use the full BitDT beast!
        return BitDT.fromPrimitives(
            BitDT.fromAbsoluteYear(zdt.getYear()),
            zdt.getMonthValue() - 1,
            zdt.getDayOfMonth(),
            zdt.getHour(), 
            zdt.getMinute(),
            zdt.getSecond(),
            zdt.get(ChronoField.MILLI_OF_SECOND),
            timezone
        ).encode();
    }

private static long fromFullBitDT(String bitdt) {
    try {
        BitDT dt = BitDT.decode(bitdt);
        
        if (dt.isEmpty()) {
            return -1;
        }
        
        // Validate that we have a reasonable date
        int year = dt.getYear();
        if (year == -1) {
            // Time-only format, use current date for year
            year = BitDT.fromAbsoluteYear(Instant.now().atZone(ZoneOffset.UTC).getYear());
        }
        
        // Build ZonedDateTime from components with proper timezone handling
        ZoneOffset offset = ZoneOffset.UTC;
        String timezone = dt.getTimezone();
        if (timezone != null) {
            offset = parseZoneOffset(timezone);
        }
        
        ZonedDateTime zdt = ZonedDateTime.of(
            BitDT.toAbsoluteYear(year),
            dt.getMonth() != -1 ? dt.getMonth() + 1 : 1,
            dt.getDay() != -1 ? dt.getDay() : 1,
            dt.getHour() != -1 ? dt.getHour() : 0,
            dt.getMinute() != -1 ? dt.getMinute() : 0, 
            dt.getSecond() != -1 ? dt.getSecond() : 0,
            dt.getMillis() != -1 ? dt.getMillis() * 1_000_000 : 0,
            offset
        );
        
        return zdt.toInstant().toEpochMilli();
    } catch (Exception e) {
        System.err.println("Failed to decode Full BitDT: " + bitdt + " - " + e.getMessage());
        return -1;
    }
}
    
    private static long decodeBaseAuto(String encoded) {
    // Analyze the string to guess the most likely base
    int likelyBase = guessMostLikelyBase(encoded);
    
    // Try the most likely base first
    try {
        return Long.parseLong(encoded.toLowerCase(), likelyBase);
    } catch (NumberFormatException e) {
        // Fall through to other bases
    }
    
    // Then try other bases in order
    for (int base : new int[]{36, 32, 16, 10}) {
        if (base == likelyBase) continue; // Already tried
        try {
            return Long.parseLong(encoded.toLowerCase(), base);
        } catch (NumberFormatException e) {
            continue;
        }
    }
    
    return fromFullBitDT(encoded);
}

private static int guessMostLikelyBase(String encoded) {
    boolean hasBase36Only = false;
    boolean hasBase32Only = false; 
    boolean hasHexOnly = true;
    
    for (int i = 0; i < encoded.length(); i++) {
        char c = encoded.charAt(i);
        
        if (c >= 'G' && c <= 'Z') {
            // Character in Base36 but NOT in Base32
            if (c >= 'W' && c <= 'Z') {
                hasBase36Only = true;  // W,X,Y,Z are Base36 ONLY
            } else {
                // G-V could be Base32 OR Base36
                hasBase32Only = true;  // At least has Base32 chars
            }
            hasHexOnly = false;
        } else if (c >= 'A' && c <= 'F') {
            // Still potentially hex
        } else if (c >= '0' && c <= '9') {
            // Valid for all bases
        } else {
            hasHexOnly = false;
        }
    }
    
    if (hasHexOnly) return 16;
    if (hasBase36Only) return 36;  // Contains W,X,Y,Z - must be Base36
    if (hasBase32Only) return 32;  // Contains G-V but not W-Z - likely Base32
    return 36; // Default to most common case
}

    private static boolean isBaseEncoded(String str) {
        // Heuristic: if string contains only base36 characters and is 6-12 chars, likely base-encoded
        if (str.length() < 6 || str.length() > 12) {
            return false;
        }
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (BASE36_CHARS.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }
    
    private static ZoneOffset parseZoneOffset(String timezone) {
        if (timezone == null || timezone.equals("UTC")) {
            return ZoneOffset.UTC;
        }
        
        try {
            // Handle ±HH format
            if (timezone.length() == 3) {
                int hours = Integer.parseInt(timezone.substring(1, 3));
                int totalSeconds = hours * 3600;
                if (timezone.charAt(0) == '-') {
                    totalSeconds = -totalSeconds;
                }
                return ZoneOffset.ofTotalSeconds(totalSeconds);
            }
            
            // Handle ±HH:MM format - FIXED PARSING
            if (timezone.length() == 6 && timezone.charAt(3) == ':') {
                int hours = Integer.parseInt(timezone.substring(1, 3));
                int minutes = Integer.parseInt(timezone.substring(4, 6));
                int totalSeconds = hours * 3600 + minutes * 60;
                if (timezone.charAt(0) == '-') {
                    totalSeconds = -totalSeconds;
                }
                return ZoneOffset.ofTotalSeconds(totalSeconds);
            }
            
            // Handle ±HHMM format (no colon)
            if (timezone.length() == 5) {
                int hours = Integer.parseInt(timezone.substring(1, 3));
                int minutes = Integer.parseInt(timezone.substring(3, 5));
                int totalSeconds = hours * 3600 + minutes * 60;
                if (timezone.charAt(0) == '-') {
                    totalSeconds = -totalSeconds;
                }
                return ZoneOffset.ofTotalSeconds(totalSeconds);
            }
            
            return ZoneOffset.UTC;
        } catch (Exception e) {
            return ZoneOffset.UTC;
        }
    }

/**
 * Gets current time as BitDT string in auto mode (UTC).
 */
public static String now() {
    long currentTime = System.currentTimeMillis();
    return toBitDT(currentTime, "UTC", MODE_AUTO);
}

/**
 * Gets current time as BitDT string with specified base (UTC).
 */
public static String now(int base) {
    long currentTime = System.currentTimeMillis();
    return toBitDT(currentTime, "UTC", base);
}

/**
 * Gets current time as BitDT string with timezone and base.
 */
public static String now(String timezone, int base) {
    long currentTime = System.currentTimeMillis();
    return toBitDT(currentTime, timezone, base);
}

/**
 * Debug method to check current time handling.
 */
public static void debugNow() {
    long currentMillis = System.currentTimeMillis();
    String base36 = encodeBase(currentMillis, 36);
    String auto = now();
    
    System.out.println("Debug Now():");
    System.out.println("  System.currentTimeMillis(): " + currentMillis);
    System.out.println("  Base36 encoded: " + base36);
    System.out.println("  now() result: " + auto);
    System.out.println("  now() decoded: " + fromBitDT(auto));
    
    // Verify they match
    if (fromBitDT(auto) == currentMillis) {
        System.out.println("  ✅ now() working correctly");
    } else {
        System.out.println("  ❌ now() has issues");
        System.out.println("  Expected: " + currentMillis);
        System.out.println("  Got: " + fromBitDT(auto));
    }
}

// Also add validation to ensure we're not getting cached values
private static String encodeBase(long value, int base) {
    // Clear any potential caching that might cause issues
    return Long.toString(value, base).toUpperCase();
}
    
    /**
     * Benchmark helper to compare different bases for a timestamp.
     */
    public static void benchmark(long epochMillis) {
        System.out.println("Benchmark for: " + epochMillis);
        // Only test supported bases
        for (int base : new int[]{2, 10, 16, 32, 36}) {
            try {
                String result = toBitDT(epochMillis, base);
                System.out.printf("Base %2d: %-20s (%d chars)%n", base, result, result.length());
            } catch (Exception e) {
                System.out.printf("Base %2d: ERROR - %s%n", base, e.getMessage());
            }
        }
        
        String full = toBitDT(epochMillis, null, MODE_FULL_BITDT);
        System.out.printf("Full BitDT: %s (%d chars)%n", full, full.length());
        
        String auto = toBitDT(epochMillis);
        System.out.printf("Auto mode:  %s (%d chars)%n", auto, auto.length());
    }
}