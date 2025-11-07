import { BitDT } from './BitDT';
import { ThousandCounter } from './ThousandCounter';
import { TYPE_FULL, TYPE_DATE_ONLY, TYPE_TIME_ONLY, TYPE_EMPTY } from './BitDT';

export class BitDTExample {
    static main(): void {
        BitDTExample.exampleBasicUsage();
        BitDTExample.exampleDateTypes();
        BitDTExample.exampleTimezones();
        BitDTExample.exampleSortingAndComparison();
        BitDTExample.exampleStorageEfficiency();
        BitDTExample.exampleErrorHandling();
        BitDTExample.exampleIntegration();
    }
    
    static exampleBasicUsage(): void {
        console.log("=== Basic Usage ===");
        
        const current = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 45, 123, undefined);
        console.log("Current datetime: " + current.encode());
        
        const dateOnly = BitDT.fromPrimitives(50000, 5, 15, 0, 0, 0, 0, undefined);
        console.log("Date only: " + dateOnly.encode());
        
        const timeOnly = BitDT.fromPrimitives(0, 0, 0, 14, 30, 45, 123, undefined);
        console.log("Time only: " + timeOnly.encode());
        console.log();
    }
    
    static exampleDateTypes(): void {
        console.log("=== Date Types ===");
        
        const full = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 45, 123, undefined);
        const dateOnly = BitDT.fromPrimitives(50000, 5, 15, 0, 0, 0, 0, undefined);
        const timeOnly = BitDT.fromPrimitives(0, 0, 0, 14, 30, 45, 123, undefined);
        const empty = BitDT.createEmpty();
        
        console.log("Full type: " + full.getDateType());
        console.log("Date only type: " + dateOnly.getDateType());
        console.log("Time only type: " + timeOnly.getDateType());
        console.log("Empty type: " + empty.getDateType());
        console.log();
    }
    
    static exampleTimezones(): void {
        console.log("=== Timezones ===");
        
        const utc = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, "+00");
        const est = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, "-05");
        const ist = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, "+05:30");
        
        console.log("UTC: " + utc.encode());
        console.log("EST: " + est.encode());
        console.log("IST: " + ist.encode());
        console.log();
    }
    
    static exampleSortingAndComparison(): void {
        console.log("=== Sorting and Comparison ===");
        
        const events: BitDT[] = [];
        events.push(BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, undefined));
        events.push(BitDT.fromPrimitives(50000, 3, 10, 9, 0, 0, 0, undefined));
        events.push(BitDT.fromPrimitives(50001, 1, 1, 0, 0, 0, 0, undefined));
        events.push(BitDT.fromPrimitives(50000, 5, 15, 18, 45, 0, 0, undefined));
        
        const sorted = BitDT.sortByNumericalValue(events);
        
        console.log("Sorted events:");
        for (const event of sorted) {
            console.log("  " + event.encode());
        }
        
        const first = sorted[0];
        const last = sorted[sorted.length - 1];
        console.log("First before last: " + first.before(last));
        console.log();
    }
    
    static exampleStorageEfficiency(): void {
        console.log("=== Storage Efficiency ===");
        
        const dates: BitDT[] = new Array(5);
        dates[0] = BitDT.fromPrimitives(50000, 0, 1, 0, 0, 0, 0, undefined);
        dates[1] = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, undefined);
        dates[2] = BitDT.fromPrimitives(50000, 11, 30, 23, 59, 59, 999, undefined);
        dates[3] = BitDT.fromPrimitives(51000, 2, 28, 12, 0, 0, 0, "+08");
        dates[4] = BitDT.createEmpty();
        
        for (let i = 0; i < dates.length; i++) {
            const encoded = dates[i].encode();
            console.log("Date " + (i + 1) + ": " + encoded + " (length: " + encoded.length + ")");
        }
        console.log();
    }
    
    static exampleErrorHandling(): void {
        console.log("=== Error Handling ===");
        
        const invalidDecoded = BitDT.decode("INVALID_STRING");
        console.log("Invalid decode result: " + invalidDecoded.encode());
        console.log("Is empty: " + invalidDecoded.isEmpty());
        
        try {
            const invalidYear = BitDT.fromPrimitives(300000, 0, 1, 0, 0, 0, 0, undefined);
        } catch (e: any) {
            console.log("Caught invalid year: " + e.message);
        }
        
        try {
            const invalidMillis = ThousandCounter.encodeMilliseconds(1000);
        } catch (e: any) {
            console.log("Caught invalid milliseconds: " + e.message);
        }
        console.log();
    }
    
    static exampleIntegration(): void {
        console.log("=== Integration Examples ===");
        
        const numericalValues: bigint[] = new Array(3);
        numericalValues[0] = BitDT.fromPrimitives(50000, 0, 1, 0, 0, 0, 0, undefined).getNumericalValue();
        numericalValues[1] = BitDT.fromPrimitives(50000, 5, 15, 14, 30, 0, 0, undefined).getNumericalValue();
        numericalValues[2] = BitDT.fromPrimitives(50001, 0, 1, 0, 0, 0, 0, undefined).getNumericalValue();
        
        console.log("Numerical values for storage:");
        for (const value of numericalValues) {
            console.log("  " + value.toString());
        }
        
        const restored = BitDT.fromNumericalArray(numericalValues);
        console.log("Restored dates:");
        for (const dt of restored) {
            console.log("  " + dt.encode());
        }
        console.log();
    }
}