"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.BitDTExample = void 0;
const BitDT_1 = require("./BitDT");
const ThousandCounter_1 = require("./ThousandCounter");
class BitDTExample {
    static main() {
        BitDTExample.exampleBasicUsage();
        BitDTExample.exampleDateTypes();
        BitDTExample.exampleTimezones();
        BitDTExample.exampleSortingAndComparison();
        BitDTExample.exampleStorageEfficiency();
        BitDTExample.exampleErrorHandling();
        BitDTExample.exampleIntegration();
    }
    static exampleBasicUsage() {
        console.log("=== Basic Usage ===");
        const current = BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 14, 30, 45, 123, undefined);
        console.log("Current datetime: " + current.encode());
        const dateOnly = BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 0, 0, 0, 0, undefined);
        console.log("Date only: " + dateOnly.encode());
        const timeOnly = BitDT_1.BitDT.fromPrimitives(0, 0, 0, 14, 30, 45, 123, undefined);
        console.log("Time only: " + timeOnly.encode());
        console.log();
    }
    static exampleDateTypes() {
        console.log("=== Date Types ===");
        const full = BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 14, 30, 45, 123, undefined);
        const dateOnly = BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 0, 0, 0, 0, undefined);
        const timeOnly = BitDT_1.BitDT.fromPrimitives(0, 0, 0, 14, 30, 45, 123, undefined);
        const empty = BitDT_1.BitDT.createEmpty();
        console.log("Full type: " + full.getDateType());
        console.log("Date only type: " + dateOnly.getDateType());
        console.log("Time only type: " + timeOnly.getDateType());
        console.log("Empty type: " + empty.getDateType());
        console.log();
    }
    static exampleTimezones() {
        console.log("=== Timezones ===");
        const utc = BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, "+00");
        const est = BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, "-05");
        const ist = BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, "+05:30");
        console.log("UTC: " + utc.encode());
        console.log("EST: " + est.encode());
        console.log("IST: " + ist.encode());
        console.log();
    }
    static exampleSortingAndComparison() {
        console.log("=== Sorting and Comparison ===");
        const events = [];
        events.push(BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, undefined));
        events.push(BitDT_1.BitDT.fromPrimitives(50000, 3, 10, 9, 0, 0, 0, undefined));
        events.push(BitDT_1.BitDT.fromPrimitives(50001, 1, 1, 0, 0, 0, 0, undefined));
        events.push(BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 18, 45, 0, 0, undefined));
        const sorted = BitDT_1.BitDT.sortByNumericalValue(events);
        console.log("Sorted events:");
        for (const event of sorted) {
            console.log("  " + event.encode());
        }
        const first = sorted[0];
        const last = sorted[sorted.length - 1];
        console.log("First before last: " + first.before(last));
        console.log();
    }
    static exampleStorageEfficiency() {
        console.log("=== Storage Efficiency ===");
        const dates = new Array(5);
        dates[0] = BitDT_1.BitDT.fromPrimitives(50000, 0, 1, 0, 0, 0, 0, undefined);
        dates[1] = BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, undefined);
        dates[2] = BitDT_1.BitDT.fromPrimitives(50000, 11, 30, 23, 59, 59, 999, undefined);
        dates[3] = BitDT_1.BitDT.fromPrimitives(51000, 2, 28, 12, 0, 0, 0, "+08");
        dates[4] = BitDT_1.BitDT.createEmpty();
        for (let i = 0; i < dates.length; i++) {
            const encoded = dates[i].encode();
            console.log("Date " + (i + 1) + ": " + encoded + " (length: " + encoded.length + ")");
        }
        console.log();
    }
    static exampleErrorHandling() {
        console.log("=== Error Handling ===");
        const invalidDecoded = BitDT_1.BitDT.decode("INVALID_STRING");
        console.log("Invalid decode result: " + invalidDecoded.encode());
        console.log("Is empty: " + invalidDecoded.isEmpty());
        try {
            const invalidYear = BitDT_1.BitDT.fromPrimitives(300000, 0, 1, 0, 0, 0, 0, undefined);
        }
        catch (e) {
            console.log("Caught invalid year: " + e.message);
        }
        try {
            const invalidMillis = ThousandCounter_1.ThousandCounter.encodeMilliseconds(1000);
        }
        catch (e) {
            console.log("Caught invalid milliseconds: " + e.message);
        }
        console.log();
    }
    static exampleIntegration() {
        console.log("=== Integration Examples ===");
        const numericalValues = new Array(3);
        numericalValues[0] = BitDT_1.BitDT.fromPrimitives(50000, 0, 1, 0, 0, 0, 0, undefined).getNumericalValue();
        numericalValues[1] = BitDT_1.BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, undefined).getNumericalValue();
        numericalValues[2] = BitDT_1.BitDT.fromPrimitives(50001, 0, 1, 0, 0, 0, 0, undefined).getNumericalValue();
        console.log("Numerical values for storage:");
        for (const value of numericalValues) {
            console.log("  " + value.toString());
        }
        const restored = BitDT_1.BitDT.fromNumericalArray(numericalValues);
        console.log("Restored dates:");
        for (const dt of restored) {
            console.log("  " + dt.encode());
        }
        console.log();
    }
}
exports.BitDTExample = BitDTExample;
