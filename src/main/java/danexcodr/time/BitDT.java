package danexcodr.time;

import java.util.*;

/**
 * The BitDT class represents a compact, space-efficient date-time implementation
 * that supports encoding and decoding of date-time values with optional timezone information.
 * It uses bit packing and character encoding to achieve significant space savings
 * compared to traditional date-time representations.
 * 
 * <p>Key features:
 * <ul>
 * <li>Supports dates from 50,000 BCE to 176,980 CE</li>
 * <li>Millisecond precision</li>
 * <li>Optional timezone support</li>
 * <li>Zero compression for efficient encoding</li>
 * <li>Multiple date types (full, date-only, time-only, empty)</li>
 * </ul>
 * 
 * @author danexcodr
 * @version 1.0
 */
public class BitDT {
    private static final String MONTHS = "ABCDEFGHIJKL";
    private static final String DAYS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345";
    private static final String HOURS = "BCDEFGHJKLMNOPQRSTUVWXYZ";
    private static final String MINUTES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345678";
    private static final String YEAR_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789";
    
    private static final char TWO_ZEROS = '.';
    private static final char THREE_ZEROS = ':';
    private static final char FOUR_ZEROS = ';';
    private static final char FIVE_ZEROS = '?';
    private static final char SIX_ZEROS = '!';
    private static final char SEVEN_ZEROS = '&';
    
    private static final char UTC_PLUS = '+';
    private static final char UTC_MINUS = '-';
    private static final String TIMEZONE_DIGITS = "0123456789";
    
    private static final long YEAR_MASK = 0xFFFFF00000000000L;
    private static final long MONTH_MASK = 0x00000F0000000000L;
    private static final long DAY_MASK = 0x000000F800000000L;
    private static final long HOUR_MASK = 0x00000007C0000000L;
    private static final long MINUTE_MASK = 0x000000003F000000L;
    private static final long SECOND_MASK = 0x0000000000FC0000L;
    private static final long MILLIS_MASK = 0x000000000003FF00L;
    private static final long TYPE_MASK = 0xF000000000000000L;
    
    /**
     * Date type constant for empty/invalid timestamps.
     */
    public static final byte TYPE_EMPTY = 0;
    
    /**
     * Date type constant for full date-time values with all fields.
     */
    public static final byte TYPE_FULL = 1;
    
    /**
     * Date type constant for date-only values (time fields are zero).
     */
    public static final byte TYPE_DATE_ONLY = 2;
    
    /**
     * Date type constant for time-only values (date fields are zero).
     */
    public static final byte TYPE_TIME_ONLY = 3;
    private static final byte TYPE_DATE_HOUR = 4;
    
    private static final long EMPTY_VALUE = TYPE_EMPTY;
    
    private final long packedValue;
    private final byte timezoneOffset;
    private final byte dateType;
    
    private BitDT(long packedValue, byte timezoneOffset, byte dateType) {
        this.packedValue = packedValue;
        this.timezoneOffset = timezoneOffset;
        this.dateType = dateType;
    }
    
    /**
     * Creates a BitDT instance from individual date-time components.
     * 
     * @param year the relative year value
     * @param month the month (0-11, where 0=January)
     * @param day the day of month (1-31)
     * @param hour the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     * @param millis the milliseconds (0-999)
     * @return a new BitDT instance
     */
    public static BitDT fromPrimitives(int year, int month, int day, int hour, int minute, int second, int millis) {
        return fromPrimitives(year, month, day, hour, minute, second, millis, null);
    }
    
    /**
     * Creates a BitDT instance from individual date-time components with timezone.
     * 
     * @param year the relative year value
     * @param month the month (0-11, where 0=January)
     * @param day the day of month (1-31)
     * @param hour the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     * @param millis the milliseconds (0-999)
     * @param timezone the timezone string (e.g., "+08", "-05", "+0530")
     * @return a new BitDT instance
     */
    public static BitDT fromPrimitives(int year, int month, int day, int hour, int minute, int second, int millis, String timezone) {
        byte dateType = determineDateType(year, month, day, hour, minute, second, millis);
        
        long packed = ((long) year << 44) |
                     ((long) month << 40) |
                     ((long) day << 35) |
                     ((long) hour << 30) |
                     ((long) minute << 24) |
                     ((long) second << 18) |
                     ((long) millis << 8) |
                     dateType;
        byte tzOffset = parseTimezoneOffset(timezone);
        return new BitDT(packed, tzOffset, dateType);
    }
    
    private static byte determineDateType(int year, int month, int day, int hour, int minute, int second, int millis) {
        if (year == 0 && month == 0 && day == 0 && hour == 0 && minute == 0 && second == 0 && millis == 0) {
            return TYPE_EMPTY;
        } else if (hour == 0 && minute == 0 && second == 0 && millis == 0) {
            return TYPE_DATE_ONLY;
        } else if (year == 0 && month == 0 && day == 0) {
            return TYPE_TIME_ONLY;
        } else if (minute == 0 && second == 0 && millis == 0 && hour != 0) {
            return TYPE_DATE_HOUR;
        } else {
            return TYPE_FULL;
        }
    }
    
    /**
     * Creates a BitDT instance from packed internal representation.
     * 
     * @param packedValue the packed 64-bit value containing date-time fields
     * @param timezoneOffset the timezone offset in 15-minute increments
     * @param dateType the date type constant
     * @return a new BitDT instance
     */
    public static BitDT fromPackedValue(long packedValue, byte timezoneOffset, byte dateType) {
        return new BitDT(packedValue, timezoneOffset, dateType);
    }
    
    /**
     * Creates an empty/invalid BitDT instance.
     * 
     * @return an empty BitDT instance
     */
    public static BitDT createEmpty() {
        return new BitDT(EMPTY_VALUE, (byte)0, TYPE_EMPTY);
    }
    
    /**
     * Gets the relative year value.
     * 
     * @return the relative year, or -1 for time-only or empty instances
     */
    public int getYear() {
        if (dateType == TYPE_EMPTY || dateType == TYPE_TIME_ONLY) return -1;
        return (int) ((packedValue & YEAR_MASK) >>> 44);
    }
    
    /**
     * Gets the month value (0-11).
     * 
     * @return the month (0=January), or -1 for time-only or empty instances
     */
    public int getMonth() {
        if (dateType == TYPE_EMPTY || dateType == TYPE_TIME_ONLY) return -1;
        return (int) ((packedValue & MONTH_MASK) >>> 40);
    }
    
    /**
     * Gets the day of month (1-31).
     * 
     * @return the day of month, or -1 for time-only or empty instances
     */
    public int getDay() {
        if (dateType == TYPE_EMPTY || dateType == TYPE_TIME_ONLY) return -1;
        return (int) ((packedValue & DAY_MASK) >>> 35);
    }
    
    /**
     * Gets the hour value (0-23).
     * 
     * @return the hour, or -1 for date-only or empty instances
     */
    public int getHour() {
        if (dateType == TYPE_EMPTY || dateType == TYPE_DATE_ONLY) return -1;
        return (int) ((packedValue & HOUR_MASK) >>> 30);
    }
    
    /**
     * Gets the minute value (0-59).
     * 
     * @return the minute, or -1 for date-only or empty instances
     */
    public int getMinute() {
        if (dateType == TYPE_EMPTY || dateType == TYPE_DATE_ONLY) return -1;
        if (dateType == TYPE_DATE_HOUR) return 0;
        return (int) ((packedValue & MINUTE_MASK) >>> 24);
    }
    
    /**
     * Gets the second value (0-59).
     * 
     * @return the second, or -1 for date-only or empty instances
     */
    public int getSecond() {
        if (dateType == TYPE_EMPTY || dateType == TYPE_DATE_ONLY) return -1;
        if (dateType == TYPE_DATE_HOUR) return 0;
        return (int) ((packedValue & SECOND_MASK) >>> 18);
    }
    
    /**
     * Gets the millisecond value (0-999).
     * 
     * @return the milliseconds, or -1 for date-only or empty instances
     */
    public int getMillis() {
        if (dateType == TYPE_EMPTY || dateType == TYPE_DATE_ONLY) return -1;
        if (dateType == TYPE_DATE_HOUR) return 0;
        return (int) ((packedValue & MILLIS_MASK) >>> 8);
    }
    
    /**
     * Gets the timezone string representation.
     * 
     * @return the timezone string, or null if no timezone is set
     */
    public String getTimezone() {
        return formatTimezoneOffset(timezoneOffset);
    }
    
    /**
     * Gets the packed internal representation of the date-time.
     * 
     * @return the packed 64-bit value
     */
    public long getPackedValue() {
        return packedValue;
    }
    
    /**
     * Gets the timezone offset in 15-minute increments.
     * 
     * @return the timezone offset
     */
    public byte getTimezoneOffset() {
        return timezoneOffset;
    }
    
    /**
     * Gets the date type constant.
     * 
     * @return the date type (TYPE_EMPTY, TYPE_FULL, TYPE_DATE_ONLY, or TYPE_TIME_ONLY)
     */
    public byte getDateType() {
        return dateType;
    }
    
    private byte getType() {
        return (byte) ((packedValue & TYPE_MASK) >>> 60);
    }
    
    /**
     * Checks if this instance represents an empty/invalid timestamp.
     * 
     * @return true if this is an empty instance
     */
    public boolean isEmpty() {
        return dateType == TYPE_EMPTY;
    }
    
    private static byte parseTimezoneOffset(String timezone) {
        if (timezone == null || timezone.isEmpty()) {
            return 0;
        }
        
        if (timezone.equals("+") || timezone.equals("-")) {
            return 0;
        }
        
        try {
            int hours = 0;
            int minutes = 0;
            
            if (timezone.length() == 3) {
                hours = Integer.parseInt(timezone.substring(1, 3));
            } else if (timezone.length() == 5) {
                hours = Integer.parseInt(timezone.substring(1, 3));
                minutes = Integer.parseInt(timezone.substring(3, 5));
            }
            
            int totalMinutes = hours * 60 + minutes;
            if (timezone.charAt(0) == '-') {
                totalMinutes = -totalMinutes;
            }
            
            return (byte) (totalMinutes / 15);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private static String formatTimezoneOffset(byte offset) {
        if (offset == 0) {
            return null;
        }
        
        int totalMinutes = offset * 15;
        int hours = Math.abs(totalMinutes) / 60;
        int minutes = Math.abs(totalMinutes) % 60;
        
        if (minutes == 0) {
            return String.format("%s%02d", totalMinutes >= 0 ? "+" : "-", hours);
        } else {
            return String.format("%s%02d%02d", totalMinutes >= 0 ? "+" : "-", hours, minutes);
        }
    }
    
    /**
     * Encodes a relative year value into a 3-character string.
     * 
     * @param year the relative year value (0-226,980)
     * @return a 3-character encoded year string
     * @throws IllegalArgumentException if year is out of valid range
     */
    public static String encodeYear(int year) {
        if (year < 0 || year >= 226981) {
            throw new IllegalArgumentException("Year must be between 0 and 226,980");
        }
        char c1 = YEAR_CHARS.charAt(year / (61 * 61));
        char c2 = YEAR_CHARS.charAt((year / 61) % 61);
        char c3 = YEAR_CHARS.charAt(year % 61);
        return "" + c1 + c2 + c3;
    }
    
    /**
     * Decodes a 3-character year string back into a relative year value.
     * 
     * @param yearCode the 3-character encoded year string
     * @return the decoded relative year, or -1 if the code is invalid
     */
    public static int decodeYear(String yearCode) {
        if (yearCode == null || yearCode.length() != 3) return -1;
        int idx1 = YEAR_CHARS.indexOf(yearCode.charAt(0));
        int idx2 = YEAR_CHARS.indexOf(yearCode.charAt(1));
        int idx3 = YEAR_CHARS.indexOf(yearCode.charAt(2));
        if (idx1 == -1 || idx2 == -1 || idx3 == -1) return -1;
        return idx1 * 61 * 61 + idx2 * 61 + idx3;
    }
    
    /**
     * Converts a relative year to an absolute year (with BCE/CE notation).
     * 
     * @param relativeYear the relative year value
     * @return the absolute year (negative for BCE, positive for CE)
     */
    public static int toAbsoluteYear(int relativeYear) {
        return relativeYear - 50000;
    }
    
    /**
     * Converts an absolute year to a relative year value.
     * 
     * @param absoluteYear the absolute year (negative for BCE, positive for CE)
     * @return the relative year value
     */
    public static int fromAbsoluteYear(int absoluteYear) {
        return absoluteYear + 50000;
    }
    
    private static String compressZeros(String fields) {
        StringBuilder compressed = new StringBuilder();
        int zeroCount = 0;
        
        for (int i = 0; i < fields.length(); i++) {
            char c = fields.charAt(i);
            if (c == '0') {
                zeroCount++;
            } else {
                if (zeroCount > 0) {
                    String zeroRun = encodeZeroRun(zeroCount);
                    compressed.append(zeroRun);
                    zeroCount = 0;
                }
                compressed.append(c);
            }
        }
        
        if (zeroCount > 0) {
            String zeroRun = encodeZeroRun(zeroCount);
            compressed.append(zeroRun);
        }
        
        return compressed.toString();
    }
    
    private static String encodeZeroRun(int zeroCount) {
        switch (zeroCount) {
            case 1: return "0";
            case 2: return ".";
            case 3: return ":";
            case 4: return ";";
            case 5: return "?";
            case 6: return "!";
            case 7: return "&";
            default: 
                StringBuilder sb = new StringBuilder("&");
                for (int i = 0; i < zeroCount - 7; i++) {
                    sb.append("0");
                }
                return sb.toString();
        }
    }
    
    private static String expandZeros(String compressed) {
        StringBuilder expanded = new StringBuilder();
        
        for (int i = 0; i < compressed.length(); i++) {
            char c = compressed.charAt(i);
            switch (c) {
                case '0': expanded.append('0'); break;
                case '.': expanded.append("00"); break;
                case ':': expanded.append("000"); break;
                case ';': expanded.append("0000"); break;
                case '?': expanded.append("00000"); break;
                case '!': expanded.append("000000"); break;
                case '&': expanded.append("0000000"); break;
                default: expanded.append(c); break;
            }
        }
        
        return expanded.toString();
    }
    
    private static String parseTimezone(String BitDT) {
        if (BitDT == null || BitDT.length() < 2) {
            return null;
        }
        
        int lastIndex = BitDT.length() - 1;
        char lastChar = BitDT.charAt(lastIndex);
        
        if (lastChar == UTC_PLUS || lastChar == UTC_MINUS) {
            return String.valueOf(lastChar);
        }
        
        for (int i = BitDT.length() - 1; i >= 0; i--) {
            char c = BitDT.charAt(i);
            if (c == UTC_PLUS || c == UTC_MINUS) {
                String timezonePart = BitDT.substring(i);
                if (isValidTimezone(timezonePart)) {
                    return timezonePart;
                }
            }
        }
        
        return null;
    }
    
    private static boolean isValidTimezone(String timezone) {
        if (timezone == null || timezone.length() < 2) {
            return false;
        }
        
        char sign = timezone.charAt(0);
        if (sign != UTC_PLUS && sign != UTC_MINUS) {
            return false;
        }
        
        String digits = timezone.substring(1);
        if (digits.isEmpty()) {
            return true;
        }
        
        for (int i = 0; i < digits.length(); i++) {
            if (TIMEZONE_DIGITS.indexOf(digits.charAt(i)) == -1) {
                return false;
            }
        }
        
        return digits.length() == 2 || digits.length() == 4;
    }
    
    private static String extractDateTimePart(String BitDT) {
        if (BitDT == null) {
            return null;
        }
        
        String timezone = parseTimezone(BitDT);
        if (timezone != null) {
            return BitDT.substring(0, BitDT.length() - timezone.length());
        }
        
        return BitDT;
    }
    
    /**
     * Encodes this BitDT instance into a compact string representation.
     * 
     * @return the encoded string representation
     */
    public String encode() {
        if (isEmpty()) {
            return "&";
        }
        
        Integer year = getYear();
        Integer month = getMonth();
        Integer day = getDay();
        Integer hour = getHour();
        Integer minute = getMinute();
        Integer second = getSecond();
        Integer millis = getMillis();
        String timezone = getTimezone();
        
        if (dateType == TYPE_DATE_ONLY) {
            hour = -1;
            minute = -1;
            second = -1;
            millis = -1;
        } else if (dateType == TYPE_TIME_ONLY) {
            year = -1;
            month = -1;
            day = -1;
        } else if (dateType == TYPE_DATE_HOUR) {
            minute = -1;
            second = -1;
            millis = -1;
        }
        
        Integer finalYear = (year == -1) ? null : year;
        Integer finalMonth = (month == -1) ? null : month;
        Integer finalDay = (day == -1) ? null : day;
        Integer finalHour = (hour == -1) ? null : hour;
        Integer finalMinute = (minute == -1) ? null : minute;
        Integer finalSecond = (second == -1) ? null : second;
        Integer finalMillis = (millis == -1) ? null : millis;

        return encodeDateTime(finalYear, finalMonth, finalDay, finalHour, finalMinute, finalSecond, finalMillis, timezone);
    }
    
    /**
     * Encodes date-time components into a compact string representation.
     * 
     * @param year the year value (can be null)
     * @param month the month value (0-11, can be null)
     * @param day the day value (1-31, can be null)
     * @param hour the hour value (0-23, can be null)
     * @param minute the minute value (0-59, can be null)
     * @param second the second value (0-59, can be null)
     * @param millis the millisecond value (0-999, can be null)
     * @return the encoded string representation
     */
    public static String encodeDateTime(Integer year, Integer month, Integer day, Integer hour, Integer minute, Integer second, Integer millis) {
        return encodeDateTime(year, month, day, hour, minute, second, millis, null);
    }
    
    /**
     * Encodes date-time components with timezone into a compact string representation.
     * 
     * @param year the year value (can be null)
     * @param month the month value (0-11, can be null)
     * @param day the day value (1-31, can be null)
     * @param hour the hour value (0-23, can be null)
     * @param minute the minute value (0-59, can be null)
     * @param second the second value (0-59, can be null)
     * @param millis the millisecond value (0-999, can be null)
     * @param timezone the timezone string (can be null)
     * @return the encoded string representation
     */
    public static String encodeDateTime(Integer year, Integer month, Integer day, Integer hour, Integer minute, Integer second, Integer millis, String timezone) {
        try {
            String yearCode = "0";
            if (year != null && year >= 0) {
                yearCode = encodeYear(year);
            }
            
            char monthChar = '0';
            if (month != null && month >= 0) {
                if (month < 0 || month >= MONTHS.length()) {
                    throw new IllegalArgumentException("Month must be between 0 and " + (MONTHS.length() - 1));
                }
                monthChar = MONTHS.charAt(month);
            }
            
            char dayChar = '0';
            if (day != null && day >= 1) {
                if (day < 1 || day > DAYS.length()) {
                    throw new IllegalArgumentException("Day must be between 1 and " + DAYS.length());
                }
                dayChar = DAYS.charAt(day - 1);
            }
            
            char hourChar = '0';
            if (hour != null && hour >= 0) {
                if (hour < 0 || hour >= HOURS.length()) {
                    throw new IllegalArgumentException("Hour must be between 0 and " + (HOURS.length() - 1));
                }
                hourChar = HOURS.charAt(hour);
            }
            
            char minuteChar = '0';
            if (minute != null && minute >= 0) {
                if (minute < 0 || minute >= MINUTES.length()) {
                    throw new IllegalArgumentException("Minute must be between 0 and " + (MINUTES.length() - 1));
                }
                minuteChar = MINUTES.charAt(minute);
            }
            
            char secondChar = '0';
            if (second != null && second >= 0) {
                if (second < 0 || second >= MINUTES.length()) {
                    throw new IllegalArgumentException("Second must be between 0 and " + (MINUTES.length() - 1));
                }
                secondChar = MINUTES.charAt(second);
            }
            
            String millisCode = "0";
            if (millis != null && millis >= 0) {
                if (millis == 0) {
                    millisCode = "0";
                } else {
                    millisCode = ThousandCounter.encodeMilliseconds(millis);
                }
            }
            
            String uncompressed = yearCode + monthChar + dayChar + hourChar + minuteChar + secondChar + millisCode;
            String dateTimePart = compressZeros(uncompressed);
            
            if (timezone != null && !timezone.isEmpty()) {
                return dateTimePart + timezone;
            }
            
            return dateTimePart;
        } catch (Exception e) {
            return "&";
        }
    }
    
    /**
     * Decodes a compact string representation into a BitDT instance.
     * 
     * @param BitDT the encoded string to decode
     * @return a BitDT instance representing the decoded date-time
     */
    public static BitDT decode(String BitDT) {
        if (BitDT == null) {
            return createEmpty();
        }
        
        String timezoneStr = parseTimezone(BitDT);
        String dateTimePart = extractDateTimePart(BitDT);
        
        if (dateTimePart == null) {
            return createEmpty();
        }
        
        if (!dateTimePart.isEmpty() && dateTimePart.charAt(0) != '0' && dateTimePart.length() < 3) {
            return createEmpty();
        }
        
        if (dateTimePart.isEmpty()) {
            return createEmpty();
        }
        
        String expanded = expandZeros(dateTimePart);
        
        if (expanded.length() < 7) {
            return createEmpty();
        }
        
        for (int i = 0; i < expanded.length(); i++) {
            char c = expanded.charAt(i);
            if (!isValidEncodedChar(c)) {
                return createEmpty();
            }
        }
        
        int year = -1, month = -1, day = -1, hour = -1, minute = -1, second = -1, millis = -1;
        int pos = 0;
        
        if (pos < expanded.length()) {
            if (expanded.charAt(pos) == '0') {
                year = -1;
                pos += 1;
            } else if (pos + 2 < expanded.length()) {
                String yearCode = expanded.substring(pos, pos + 3);
                year = decodeYear(yearCode);
                if (year == -1) {
                    return createEmpty();
                }
                pos += 3;
            } else {
                return createEmpty();
            }
        }
        
        if (pos < expanded.length()) {
            if (expanded.charAt(pos) == '0') {
                month = -1;
            } else {
                month = MONTHS.indexOf(expanded.charAt(pos));
                if (month == -1) {
                    return createEmpty();
                }
            }
            pos += 1;
        }
        
        if (pos < expanded.length()) {
            if (expanded.charAt(pos) == '0') {
                day = -1;
            } else {
                day = DAYS.indexOf(expanded.charAt(pos)) + 1;
                if (day == 0) {
                    return createEmpty();
                }
            }
            pos += 1;
        }
        
        if (pos < expanded.length()) {
            if (expanded.charAt(pos) == '0') {
                hour = -1;
            } else {
                hour = HOURS.indexOf(expanded.charAt(pos));
                if (hour == -1) {
                    return createEmpty();
                }
            }
            pos += 1;
        }
        
        if (pos < expanded.length()) {
            if (expanded.charAt(pos) == '0') {
                minute = -1;
            } else {
                minute = MINUTES.indexOf(expanded.charAt(pos));
                if (minute == -1) {
                    return createEmpty();
                }
            }
            pos += 1;
        }
        
        if (pos < expanded.length()) {
            if (expanded.charAt(pos) == '0') {
                second = -1;
            } else {
                second = MINUTES.indexOf(expanded.charAt(pos));
                if (second == -1) {
                    return createEmpty();
                }
            }
            pos += 1;
        }
        
        if (pos < expanded.length()) {
            if (expanded.charAt(pos) == '0') {
                millis = 0;
                pos += 1;
            } else if (pos + 1 < expanded.length()) {
                String millisCode = expanded.substring(pos, pos + 2);
                millis = ThousandCounter.decodeMilliseconds(millisCode);
                if (millis == -1) {
                    return createEmpty();
                }
                pos += 2;
            } else {
                return createEmpty();
            }
        }
        
        if (year == -1 && month == -1 && day == -1 && hour == -1 && minute == -1 && second == -1 && millis == -1) {
            return createEmpty();
        }
        
        byte tzOffset = parseTimezoneOffset(timezoneStr);
        
        int finalYear = year != -1 ? year : 0;
        int finalMonth = month != -1 ? month : 0;
        int finalDay = day != -1 ? day : 0;
        int finalHour = hour != -1 ? hour : 0;
        int finalMinute = minute != -1 ? minute : 0;
        int finalSecond = second != -1 ? second : 0;
        int finalMillis = millis != -1 ? millis : 0;
        
        return fromPrimitives(finalYear, finalMonth, finalDay, finalHour, finalMinute, finalSecond, finalMillis, timezoneStr);
    }
    
    /**
     * Gets the numerical value for comparison and sorting.
     * 
     * @return the packed numerical value
     */
    public long getNumericalValue() {
        return packedValue;
    }
    
    /**
     * Compares this BitDT with another for ordering.
     * 
     * @param other the other BitDT to compare to
     * @return a negative integer, zero, or positive integer as this BitDT is less than, equal to, or greater than the specified BitDT
     */
    public int compareTo(BitDT other) {
        return Long.compare(this.packedValue, other.packedValue);
    }
    
    /**
     * Checks if this BitDT is before another BitDT.
     * 
     * @param other the other BitDT to compare to
     * @return true if this BitDT is before the other BitDT
     */
    public boolean before(BitDT other) {
        return this.packedValue < other.packedValue;
    }
    
    /**
     * Checks if this BitDT is after another BitDT.
     * 
     * @param other the other BitDT to compare to
     * @return true if this BitDT is after the other BitDT
     */
    public boolean after(BitDT other) {
        return this.packedValue > other.packedValue;
    }
    
    /**
     * Compares this BitDT with another for equality.
     * 
     * @param other the object to compare with
     * @return true if the objects are equal
     */
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BitDT)) return false;
        BitDT that = (BitDT) other;
        return this.packedValue == that.packedValue && this.timezoneOffset == that.timezoneOffset;
    }
    
    /**
     * Returns a hash code value for this BitDT.
     * 
     * @return a hash code value for this object
     */
    public int hashCode() {
        return (int) (packedValue ^ (packedValue >>> 32)) + timezoneOffset;
    }
    
    /**
     * Sorts a list of BitDT instances by their numerical value.
     * 
     * @param dates the list of BitDT instances to sort
     * @return a new sorted list (original list is not modified)
     */
    public static List<BitDT> sortByNumericalValue(List<BitDT> dates) {
        if (dates == null) return new ArrayList<BitDT>();
        
        List<BitDT> copy = new ArrayList<BitDT>(dates);
        Collections.sort(copy, new Comparator<BitDT>() {
            @Override
            public int compare(BitDT d1, BitDT d2) {
                return Long.compare(d1.getNumericalValue(), d2.getNumericalValue());
            }
        });
        return copy;
    }
    
    /**
     * Converts an array of numerical values to a list of BitDT instances.
     * 
     * @param numericalValues the array of packed numerical values
     * @return a list of BitDT instances
     */
    public static List<BitDT> fromNumericalArray(long[] numericalValues) {
        if (numericalValues == null) {
            return new ArrayList<BitDT>();
        }
        
        long[] valuesCopy = numericalValues.clone();
        List<BitDT> result = new ArrayList<BitDT>();
        for (long value : valuesCopy) {
            result.add(fromPackedValue(value, (byte)0, TYPE_FULL));
        }
        return result;
    }
    
    /**
     * Converts a list of BitDT instances to an array of numerical values.
     * 
     * @param dates the list of BitDT instances
     * @return an array of packed numerical values
     */
    public static long[] toNumericalArray(List<BitDT> dates) {
        long[] result = new long[dates.size()];
        for (int i = 0; i < dates.size(); i++) {
            result[i] = dates.get(i).getNumericalValue();
        }
        return result;
    }
    
    /**
     * Creates a BitDT instance from a numerical value.
     * 
     * @param numericalValue the packed numerical value
     * @return a BitDT instance
     */
    public static BitDT fromNumericalValue(long numericalValue) {
        return fromPackedValue(numericalValue, (byte)0, TYPE_FULL);
    }
    
    private static boolean isValidEncodedChar(char c) {
        return YEAR_CHARS.indexOf(c) != -1 || 
               MONTHS.indexOf(c) != -1 ||
               DAYS.indexOf(c) != -1 ||
               HOURS.indexOf(c) != -1 ||
               MINUTES.indexOf(c) != -1 ||
               c == '0' || c == '.' || c == ':' || c == ';' || c == '?' || c == '!' || c == '&';
    }
}