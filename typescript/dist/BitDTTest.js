"use strict";
const { BitDT } = require('./BitDT');
const { ThousandCounter } = require('./ThousandCounter');
class BitDTTest {
    static main() {
        BitDTTest.testBasicEncodingDecoding();
        BitDTTest.testEdgeCases();
        BitDTTest.testSorting();
        BitDTTest.testNumericalOperations();
        BitDTTest.testThousandCounter();
        BitDTTest.testYearEncoding();
        BitDTTest.testTimezone();
        BitDTTest.testEmptyAndNull();
        BitDTTest.testCompression();
        BitDTTest.testInvalidInputs();
        console.log("All tests completed.");
    }
    static testBasicEncodingDecoding() {
        console.log("=== Testing Basic Encoding/Decoding ===");
        const original = BitDT.fromPrimitives(50000, 0, 1, 12, 30, 45, 123, undefined);
        const encoded = original.encode();
        const decoded = BitDT.decode(encoded);
        console.log("Original: " + original.encode());
        console.log("Decoded: " + decoded.encode());
        console.log("Match: " + original.equals(decoded));
        console.log();
    }
    static testEdgeCases() {
        console.log("=== Testing Edge Cases ===");
        const minDate = BitDT.fromPrimitives(0, 0, 1, 0, 0, 0, 0, undefined);
        const maxDate = BitDT.fromPrimitives(226980, 11, 31, 23, 59, 59, 999, undefined);
        console.log("Min date: " + minDate.encode());
        console.log("Max date: " + maxDate.encode());
        console.log();
    }
    static testSorting() {
        console.log("=== Testing Sorting ===");
        const dates = [];
        dates.push(BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, undefined));
        dates.push(BitDT.fromPrimitives(50000, 3, 10, 9, 0, 0, 0, undefined));
        dates.push(BitDT.fromPrimitives(50001, 1, 1, 0, 0, 0, 0, undefined));
        const sorted = BitDT.sortByNumericalValue(dates);
        console.log("Original order:");
        for (const dt of dates) {
            console.log(dt.encode());
        }
        console.log("Sorted order:");
        for (const dt of sorted) {
            console.log(dt.encode());
        }
        console.log();
    }
    static testNumericalOperations() {
        console.log("=== Testing Numerical Operations ===");
        const dt = BitDT.fromPrimitives(50000, 6, 20, 15, 45, 30, 500, undefined);
        const numerical = dt.getNumericalValue();
        const fromNumerical = BitDT.fromNumericalValue(numerical);
        console.log("Original: " + dt.encode());
        console.log("From numerical: " + fromNumerical.encode());
        console.log("Match: " + dt.equals(fromNumerical));
        console.log();
    }
    static testThousandCounter() {
        console.log("=== Testing ThousandCounter ===");
        for (let i = 0; i <= 999; i += 100) {
            const encoded = ThousandCounter.encodeMilliseconds(i);
            const decoded = ThousandCounter.decodeMilliseconds(encoded);
            console.log(i + " -> " + encoded + " -> " + decoded + " : " + (i === decoded));
        }
        console.log();
    }
    static testYearEncoding() {
        console.log("=== Testing Year Encoding ===");
        const testYears = [0, 50000, 100000, 150000, 200000, 226980];
        for (const year of testYears) {
            const encoded = BitDT.encodeYear(year);
            const decoded = BitDT.decodeYear(encoded);
            console.log(year + " -> " + encoded + " -> " + decoded + " : " + (year === decoded));
        }
        console.log();
    }
    static testTimezone() {
        console.log("=== Testing Timezone ===");
        const withTz = BitDT.fromPrimitives(50000, 0, 1, 12, 0, 0, 0, "+05:30");
        const encoded = withTz.encode();
        const decoded = BitDT.decode(encoded);
        console.log("With timezone: " + encoded);
        console.log("Decoded timezone: " + decoded.getTimezone());
        console.log();
    }
    static testEmptyAndNull() {
        console.log("=== Testing Empty and Null ===");
        const empty = BitDT.createEmpty();
        const fromNull = BitDT.decode("");
        const fromEmptyString = BitDT.decode("");
        console.log("Empty: " + empty.encode());
        console.log("From null: " + fromNull.encode());
        console.log("From empty string: " + fromEmptyString.encode());
        console.log("All empty: " + (empty.isEmpty() && fromNull.isEmpty() && fromEmptyString.isEmpty()));
        console.log();
    }
    static testCompression() {
        console.log("=== Testing Compression ===");
        const allZeros = BitDT.fromPrimitives(0, 0, 0, 0, 0, 0, 0, undefined);
        const manyZeros = BitDT.fromPrimitives(0, 0, 1, 0, 0, 0, 0, undefined);
        console.log("All zeros: " + allZeros.encode());
        console.log("Many zeros: " + manyZeros.encode());
        console.log();
    }
    static testInvalidInputs() {
        console.log("=== Testing Invalid Inputs ===");
        try {
            const invalid = BitDT.fromPrimitives(-1, 0, 1, 0, 0, 0, 0, undefined);
            console.log("Should not reach here");
        }
        catch (e) {
            console.log("Correctly caught invalid year: " + e.message);
        }
        try {
            const invalid = BitDT.fromPrimitives(0, 12, 1, 0, 0, 0, 0, undefined);
            console.log("Should not reach here");
        }
        catch (e) {
            console.log("Correctly caught invalid month: " + e.message);
        }
        const invalidDecoded = BitDT.decode("INVALID");
        console.log("Invalid decoding gives empty: " + invalidDecoded.isEmpty());
        console.log();
    }
}
// Run tests if this file is executed directly
if (require.main === module) {
    BitDTTest.main();
}
