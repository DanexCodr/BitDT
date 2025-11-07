package danexcodr.time;

import java.util.*;

/**
 * The BitDTArray class provides an efficient, memory-optimized array implementation
 * for storing multiple BitDT instances. It uses parallel arrays to store packed
 * values, timezone offsets, and date types, reducing memory overhead compared to
 * storing individual BitDT objects.
 * 
 * <p>This class is immutable - all operations that modify the array return new instances.
 * This provides thread safety and predictable behavior.</p>
 * 
 * <p>Key features:
 * <ul>
 * <li>Memory-efficient storage using parallel arrays</li>
 * <li>Immutable operations for thread safety</li>
 * <li>Filtering by date type</li>
 * <li>Sorting and slicing operations</li>
 * <li>Defensive copying in getters</li>
 * </ul>
 * 
 * @author danexcodr
 * @version 1.0
 * @see BitDT
 */
public class BitDTArray {
    private final long[] packedValues;
    private final byte[] timezoneOffsets;
    private final byte[] dateTypes;
    
    /**
     * Constructs an empty BitDTArray with the specified size.
     * All elements will be initialized to default (empty) values.
     * 
     * @param size the number of elements in the array
     * @throws IllegalArgumentException if size is negative
     */
    public BitDTArray(int size) {
        this.packedValues = new long[size];
        this.timezoneOffsets = new byte[size];
        this.dateTypes = new byte[size];
    }
    
    /**
     * Constructs a BitDTArray from existing parallel arrays.
     * The arrays are cloned to ensure immutability.
     * 
     * @param packedValues the array of packed date-time values
     * @param timezoneOffsets the array of timezone offsets (15-minute increments)
     * @param dateTypes the array of date type constants
     * @throws IllegalArgumentException if arrays have different lengths
     */
    public BitDTArray(long[] packedValues, byte[] timezoneOffsets, byte[] dateTypes) {
        if (packedValues.length != timezoneOffsets.length || packedValues.length != dateTypes.length) {
            throw new IllegalArgumentException("Arrays must have same length");
        }
        this.packedValues = packedValues.clone();
        this.timezoneOffsets = timezoneOffsets.clone();
        this.dateTypes = dateTypes.clone();
    }
    
    /**
     * Creates a new BitDTArray with the specified element replaced.
     * The original array is not modified.
     * 
     * @param index the index of the element to replace
     * @param dt the new BitDT value to set at the specified index
     * @return a new BitDTArray with the updated value
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public BitDTArray withValue(int index, BitDT dt) {
        if (index < 0 || index >= packedValues.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + packedValues.length);
        }
        
        long[] newPacked = packedValues.clone();
        byte[] newTimezones = timezoneOffsets.clone();
        byte[] newTypes = dateTypes.clone();
        
        newPacked[index] = dt.getPackedValue();
        newTimezones[index] = dt.getTimezoneOffset();
        newTypes[index] = dt.getDateType();
        
        return new BitDTArray(newPacked, newTimezones, newTypes);
    }
    
    /**
     * Retrieves the BitDT element at the specified index.
     * 
     * @param index the index of the element to retrieve
     * @return the BitDT element at the specified index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public BitDT get(int index) {
        if (index < 0 || index >= packedValues.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + packedValues.length);
        }
        return BitDT.fromPackedValue(packedValues[index], timezoneOffsets[index], dateTypes[index]);
    }
    
    /**
     * Returns the number of elements in this array.
     * 
     * @return the number of elements in the array
     */
    public int size() {
        return packedValues.length;
    }
    
    /**
     * Returns a new BitDTArray with elements sorted by their packed numerical values.
     * The original array is not modified.
     * 
     * @return a new sorted BitDTArray
     */
    public BitDTArray sorted() {
        Integer[] indices = new Integer[packedValues.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        
        Arrays.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
                return Long.compare(packedValues[i1], packedValues[i2]);
            }
        });
        
        long[] newPacked = new long[packedValues.length];
        byte[] newTimezones = new byte[timezoneOffsets.length];
        byte[] newTypes = new byte[dateTypes.length];
        
        for (int i = 0; i < indices.length; i++) {
            newPacked[i] = packedValues[indices[i]];
            newTimezones[i] = timezoneOffsets[indices[i]];
            newTypes[i] = dateTypes[indices[i]];
        }
        
        return new BitDTArray(newPacked, newTimezones, newTypes);
    }
    
    /**
     * Returns a new BitDTArray with elements sorted by their packed numerical values.
     * This is an alias for the sorted() method.
     * 
     * @return a new sorted BitDTArray
     * @see #sorted()
     */
    public BitDTArray sortedCopy() {
        return sorted();
    }
    
    /**
     * Creates a BitDTArray from a list of BitDT instances.
     * 
     * @param dates the list of BitDT instances to convert
     * @return a new BitDTArray containing the elements from the list
     */
    public static BitDTArray fromList(List<BitDT> dates) {
        long[] packed = new long[dates.size()];
        byte[] timezones = new byte[dates.size()];
        byte[] types = new byte[dates.size()];
        
        for (int i = 0; i < dates.size(); i++) {
            BitDT dt = dates.get(i);
            packed[i] = dt.getPackedValue();
            timezones[i] = dt.getTimezoneOffset();
            types[i] = dt.getDateType();
        }
        
        return new BitDTArray(packed, timezones, types);
    }
    
    /**
     * Converts this BitDTArray to a list of BitDT instances.
     * 
     * @return a new list containing BitDT instances from this array
     */
    public List<BitDT> toList() {
        List<BitDT> result = new ArrayList<BitDT>();
        for (int i = 0; i < packedValues.length; i++) {
            result.add(BitDT.fromPackedValue(packedValues[i], timezoneOffsets[i], dateTypes[i]));
        }
        return result;
    }
    
    /**
     * Filters the array to include only elements of the specified date type.
     * Returns a new array containing only the matching elements.
     * 
     * @param dateType the date type to filter by (TYPE_EMPTY, TYPE_FULL, etc.)
     * @return a new BitDTArray containing only elements of the specified type
     */
    public BitDTArray filterByType(final byte dateType) {
        List<Long> filteredPacked = new ArrayList<Long>();
        List<Byte> filteredTimezones = new ArrayList<Byte>();
        List<Byte> filteredTypes = new ArrayList<Byte>();
        
        for (int i = 0; i < packedValues.length; i++) {
            if (dateTypes[i] == dateType) {
                filteredPacked.add(packedValues[i]);
                filteredTimezones.add(timezoneOffsets[i]);
                filteredTypes.add(dateTypes[i]);
            }
        }
        
        long[] packedArray = new long[filteredPacked.size()];
        byte[] tzArray = new byte[filteredTimezones.size()];
        byte[] typeArray = new byte[filteredTypes.size()];
        
        for (int i = 0; i < filteredPacked.size(); i++) {
            packedArray[i] = filteredPacked.get(i);
            tzArray[i] = filteredTimezones.get(i);
            typeArray[i] = filteredTypes.get(i);
        }
        
        return new BitDTArray(packedArray, tzArray, typeArray);
    }
    
    /**
     * Counts the number of elements of the specified date type.
     * 
     * @param dateType the date type to count (TYPE_EMPTY, TYPE_FULL, etc.)
     * @return the number of elements matching the specified date type
     */
    public int countByType(byte dateType) {
        int count = 0;
        for (int i = 0; i < dateTypes.length; i++) {
            if (dateTypes[i] == dateType) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Returns a new array containing only full date-time elements.
     * 
     * @return a new BitDTArray with only TYPE_FULL elements
     */
    public BitDTArray getFullDates() {
        return filterByType(BitDT.TYPE_FULL);
    }
    
    /**
     * Returns a new array containing only date-only elements.
     * 
     * @return a new BitDTArray with only TYPE_DATE_ONLY elements
     */
    public BitDTArray getDateOnly() {
        return filterByType(BitDT.TYPE_DATE_ONLY);
    }
    
    /**
     * Returns a new array containing only time-only elements.
     * 
     * @return a new BitDTArray with only TYPE_TIME_ONLY elements
     */
    public BitDTArray getTimeOnly() {
        return filterByType(BitDT.TYPE_TIME_ONLY);
    }
    
    /**
     * Returns a new array containing only empty elements.
     * 
     * @return a new BitDTArray with only TYPE_EMPTY elements
     */
    public BitDTArray getEmpty() {
        return filterByType(BitDT.TYPE_EMPTY);
    }
    
    /**
     * Returns a copy of the packed values array.
     * 
     * @return a defensive copy of the packed values array
     */
    public long[] getPackedValues() {
        return packedValues.clone();
    }
    
    /**
     * Returns a copy of the timezone offsets array.
     * 
     * @return a defensive copy of the timezone offsets array
     */
    public byte[] getTimezoneOffsets() {
        return timezoneOffsets.clone();
    }
    
    /**
     * Returns a copy of the date types array.
     * 
     * @return a defensive copy of the date types array
     */
    public byte[] getDateTypes() {
        return dateTypes.clone();
    }
    
    /**
     * Returns a slice of this array from the specified start index (inclusive)
     * to the specified end index (exclusive).
     * 
     * @param start the starting index (inclusive)
     * @param end the ending index (exclusive)
     * @return a new BitDTArray containing the specified slice
     * @throws IllegalArgumentException if the slice range is invalid
     */
    public BitDTArray slice(int start, int end) {
        if (start < 0 || end > packedValues.length || start > end) {
            throw new IllegalArgumentException("Invalid slice range");
        }
        
        int length = end - start;
        long[] slicedPacked = new long[length];
        byte[] slicedTimezones = new byte[length];
        byte[] slicedTypes = new byte[length];
        
        System.arraycopy(packedValues, start, slicedPacked, 0, length);
        System.arraycopy(timezoneOffsets, start, slicedTimezones, 0, length);
        System.arraycopy(dateTypes, start, slicedTypes, 0, length);
        
        return new BitDTArray(slicedPacked, slicedTimezones, slicedTypes);
    }
    
    /**
     * Concatenates this array with another BitDTArray.
     * 
     * @param other the other BitDTArray to concatenate
     * @return a new BitDTArray containing elements from both arrays
     */
    public BitDTArray concat(BitDTArray other) {
        int newLength = this.packedValues.length + other.packedValues.length;
        long[] newPacked = new long[newLength];
        byte[] newTimezones = new byte[newLength];
        byte[] newTypes = new byte[newLength];
        
        System.arraycopy(this.packedValues, 0, newPacked, 0, this.packedValues.length);
        System.arraycopy(this.timezoneOffsets, 0, newTimezones, 0, this.timezoneOffsets.length);
        System.arraycopy(this.dateTypes, 0, newTypes, 0, this.dateTypes.length);
        
        System.arraycopy(other.packedValues, 0, newPacked, this.packedValues.length, other.packedValues.length);
        System.arraycopy(other.timezoneOffsets, 0, newTimezones, this.timezoneOffsets.length, other.timezoneOffsets.length);
        System.arraycopy(other.dateTypes, 0, newTypes, this.dateTypes.length, other.dateTypes.length);
        
        return new BitDTArray(newPacked, newTimezones, newTypes);
    }
    
    /**
     * Compares this BitDTArray with the specified object for equality.
     * Returns true if the other object is a BitDTArray with the same
     * packed values, timezone offsets, and date types in the same order.
     * 
     * @param obj the object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BitDTArray)) return false;
        
        BitDTArray other = (BitDTArray) obj;
        return Arrays.equals(packedValues, other.packedValues) &&
               Arrays.equals(timezoneOffsets, other.timezoneOffsets) &&
               Arrays.equals(dateTypes, other.dateTypes);
    }
    
    /**
     * Returns a hash code value for this BitDTArray.
     * The hash code is computed from the packed values, timezone offsets,
     * and date types arrays.
     * 
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        int result = Arrays.hashCode(packedValues);
        result = 31 * result + Arrays.hashCode(timezoneOffsets);
        result = 31 * result + Arrays.hashCode(dateTypes);
        return result;
    }
}