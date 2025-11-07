package danexcodr.time;

/**
 * The ThousandCounter class provides encoding and decoding functionality
 * for millisecond values (0-999) into compact 2-character representations.
 * This is used for efficient storage and transmission of millisecond precision
 * in date-time values.
 * 
 * @author danexcodr
 * @version 1.0
 */
public class ThousandCounter {
    private static final String FIRST_CHAR = "BCDFGHJKLNPQRSTVWXYZbcdfghjklnpqrstvwxyz";
    private static final String SECOND_CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    /**
     * Encodes a millisecond value (0-999) into a 2-character string.
     * The encoding uses two character sets to represent the value in base-25 format.
     * 
     * @param millis the millisecond value to encode (0-999)
     * @return a 2-character string representing the encoded milliseconds
     * @throws IllegalArgumentException if millis is not between 0 and 999
     */
    public static String encodeMilliseconds(int millis) {
        if (millis < 0 || millis > 999) {
            throw new IllegalArgumentException("Milliseconds must be between 0 and 999");
        }
        
        int firstIndex = millis / 25;
        int secondIndex = millis % 25;
        
        if (firstIndex < 0 || firstIndex >= FIRST_CHAR.length() || 
            secondIndex < 0 || secondIndex >= SECOND_CHAR.length()) {
            throw new IllegalArgumentException("Invalid millisecond value: " + millis);
        }
        
        return "" + FIRST_CHAR.charAt(firstIndex) + SECOND_CHAR.charAt(secondIndex);
    }
    
    /**
     * Decodes a 2-character string back into a millisecond value.
     * 
     * @param code the 2-character string to decode
     * @return the decoded millisecond value (0-999), or -1 if the code is invalid
     */
    public static int decodeMilliseconds(String code) {
        if (code == null || code.length() != 2) return -1;
        
        int firstIndex = FIRST_CHAR.indexOf(code.charAt(0));
        int secondIndex = SECOND_CHAR.indexOf(code.charAt(1));
        
        if (firstIndex == -1 || secondIndex == -1) return -1;
        
        return firstIndex * 25 + secondIndex;
    }
}
