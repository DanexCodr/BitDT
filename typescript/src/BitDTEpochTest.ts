const { BitDTEpoch } = require('./BitDTEpoch');

class BitDTEpochTest {
    static main() {
        console.log("=== BitDTEpoch Comprehensive Test ===\n");
        
        BitDTEpochTest.testNowFunction();
        BitDTEpochTest.testBasicFunctionality();
        BitDTEpochTest.testBaseEncodings();
        BitDTEpochTest.testTimezoneSupport();
        BitDTEpochTest.testAutoDetection();
        BitDTEpochTest.testEdgeCases();
        BitDTEpochTest.testPerformanceComparison();
        BitDTEpochTest.testBackwardCompatibility();
        
        console.log("=== All BitDTEpoch Tests Completed ===");
    }
    
    static testNowFunction() {
        const start = Date.now();
        const nowEncoded = BitDTEpoch.now();
        const decoded = BitDTEpoch.fromBitDT(nowEncoded);
        const end = Date.now();
        
        console.log("Now() Test:");
        console.log("  Encoded: " + nowEncoded);
        console.log("  Decoded: " + decoded);
        console.log("  Current: " + start);
        console.log("  Difference: " + (decoded - start) + " ms");
        
        if (Math.abs(decoded - start) < 100) {
            console.log("  ✅ Now() function working correctly");
        } else {
            console.log("  ❌ Now() function has issues");
        }
        console.log();
    }
    
    static testBasicFunctionality() {
        console.log("1. BASIC FUNCTIONALITY TEST");
        console.log("===========================");
        
        const now = Date.now();
        const testTime = 1718323456789;
        
        const autoEncoded = BitDTEpoch.toBitDT(testTime);
        const autoDecoded = BitDTEpoch.fromBitDT(autoEncoded);
        console.log(`Auto mode: ${autoEncoded} -> ${autoDecoded} : ${testTime === autoDecoded ? '✅ PASS' : '❌ FAIL'}`);
        
        const fullEncoded = BitDTEpoch.toBitDT(testTime, undefined, BitDTEpoch.MODE_FULL_BITDT);
        const fullDecoded = BitDTEpoch.fromBitDT(fullEncoded);
        console.log(`Full mode: ${fullEncoded} -> ${fullDecoded} : ${testTime === fullDecoded ? '✅ PASS' : '❌ FAIL'}`);
        
        const nowEncoded = BitDTEpoch.now();
        const nowDecoded = BitDTEpoch.fromBitDT(nowEncoded);
        console.log(`Now(): ${nowEncoded} -> ${nowDecoded} : ${Math.abs(now - nowDecoded) < 1000 ? '✅ PASS' : '❌ FAIL'}`);
        
        console.log();
    }
    
    static testBaseEncodings() {
        console.log("2. BASE ENCODING TEST");
        console.log("=====================");
        
        const testTime = 1718323456789;
        const testBases = [2, 8, 10, 16, 32, 36];
        
        for (const base of testBases) {
            try {
                const encoded = BitDTEpoch.toBitDT(testTime, base);
                const decoded = BitDTEpoch.fromBitDT(encoded, base);
                const pass = testTime === decoded;
                
                console.log(`Base ${base.toString().padStart(2)}: ${encoded.padEnd(20)} -> ${decoded} : ${pass ? '✅ PASS' : '❌ FAIL'}`);
                    
                if (!pass) {
                    console.log(`  Expected: ${testTime}, Got: ${decoded}`);
                }
            } catch (e) {
                console.log(`Base ${base.toString().padStart(2)}: ❌ ERROR - ${(e as any).message}`);
            }
        }
        
        console.log();
    }
    
    static testTimezoneSupport() {
        console.log("3. TIMEZONE SUPPORT TEST");
        console.log("========================");
        
        const testTime = 1718323456789;
        const timezones = [undefined, "UTC", "+00", "+08", "-05", "+0530"];
        
        for (const tz of timezones) {
            try {
                const encoded = BitDTEpoch.toBitDT(testTime, tz, BitDTEpoch.MODE_FULL_BITDT);
                const decoded = BitDTEpoch.fromBitDT(encoded);
                
                const pass = testTime === decoded;
                const tzDisplay = (tz === undefined ? "undefined" : tz);
                
                console.log(`TZ ${tzDisplay.padEnd(8)}: ${encoded.padEnd(18)} -> ${decoded} : ${pass ? '✅ PASS' : '❌ FAIL'}`);
                    
            } catch (e) {
                console.log(`TZ ${tz}: ❌ ERROR - ${(e as any).message}`);
            }
        }
        
        console.log();
    }
    
    static testAutoDetection() {
        console.log("4. AUTO-DETECTION TEST");
        console.log("======================");
        
        const testTime = 1718323456789;
        
        const base36 = BitDTEpoch.toBitDT(testTime, 36);
        const base32 = BitDTEpoch.toBitDT(testTime, 32);
        const base16 = BitDTEpoch.toBitDT(testTime, 16);
        const fullBitDT = BitDTEpoch.toBitDT(testTime, undefined, BitDTEpoch.MODE_FULL_BITDT);
        
        console.log(`Base36 auto-detect: ${base36} -> ${BitDTEpoch.fromBitDT(base36)} : ${testTime === BitDTEpoch.fromBitDT(base36) ? '✅ PASS' : '❌ FAIL'}`);
        console.log(`Base32 auto-detect: ${base32} -> ${BitDTEpoch.fromBitDT(base32)} : ${testTime === BitDTEpoch.fromBitDT(base32) ? '✅ PASS' : '❌ FAIL'}`);
        console.log(`Base16 auto-detect: ${base16} -> ${BitDTEpoch.fromBitDT(base16)} : ${testTime === BitDTEpoch.fromBitDT(base16) ? '✅ PASS' : '❌ FAIL'}`);
        console.log(`Full BitDT auto-detect: ${fullBitDT} -> ${BitDTEpoch.fromBitDT(fullBitDT)} : ${testTime === BitDTEpoch.fromBitDT(fullBitDT) ? '✅ PASS' : '❌ FAIL'}`);
        
        console.log();
    }
    
    static testEdgeCases() {
        console.log("5. EDGE CASES TEST");
        console.log("==================");
        
        console.log(`Null input decode: ${BitDTEpoch.fromBitDT(null)} : ${BitDTEpoch.fromBitDT(null) === -1 ? '✅ PASS' : '❌ FAIL'}`);
        console.log(`Empty input decode: ${BitDTEpoch.fromBitDT('')} : ${BitDTEpoch.fromBitDT('') === -1 ? '✅ PASS' : '❌ FAIL'}`);
        console.log(`Invalid base: ${BitDTEpoch.toBitDT(123456789, 99)} : ${BitDTEpoch.toBitDT(123456789, 99).length > 0 ? '✅ PASS' : '❌ FAIL'}`);
        
        const ancient = -62135596800000;
        const future = 253402300799000;
        
        console.log(`Ancient date: ${BitDTEpoch.toBitDT(ancient, BitDTEpoch.MODE_FULL_BITDT)} -> ${BitDTEpoch.fromBitDT(BitDTEpoch.toBitDT(ancient, BitDTEpoch.MODE_FULL_BITDT))} : ${ancient === BitDTEpoch.fromBitDT(BitDTEpoch.toBitDT(ancient, BitDTEpoch.MODE_FULL_BITDT)) ? '✅ PASS' : '❌ FAIL'}`);
        console.log(`Future date: ${BitDTEpoch.toBitDT(future, BitDTEpoch.MODE_FULL_BITDT)} -> ${BitDTEpoch.fromBitDT(BitDTEpoch.toBitDT(future, BitDTEpoch.MODE_FULL_BITDT))} : ${future === BitDTEpoch.fromBitDT(BitDTEpoch.toBitDT(future, BitDTEpoch.MODE_FULL_BITDT)) ? '✅ PASS' : '❌ FAIL'}`);
        
        console.log();
    }
    
    static testPerformanceComparison() {
        console.log("6. PERFORMANCE COMPARISON");
        console.log("=========================");
        
        const testTime = Date.now();
        
        const base36 = BitDTEpoch.toBitDT(testTime, 36);
        const full = BitDTEpoch.toBitDT(testTime, undefined, BitDTEpoch.MODE_FULL_BITDT);
        const auto = BitDTEpoch.toBitDT(testTime);
        
        console.log("Size Comparison:");
        console.log(`  Base36:  ${base36} (${base36.length} chars)`);
        console.log(`  Full:    ${full} (${full.length} chars)`);
        console.log(`  Auto:    ${auto} (${auto.length} chars)`);
        console.log();
    }
    
    static testBackwardCompatibility() {
        console.log("7. BACKWARD COMPATIBILITY TEST");
        console.log("==============================");
        
        const testTime = 1718323456789;
        
        const theirOriginal = testTime.toString(36).toUpperCase();
        const theirDecoded = parseInt(theirOriginal.toLowerCase(), 36);
        
        const ourVersion = BitDTEpoch.toBitDT(testTime, 36);
        const ourDecoded = BitDTEpoch.fromBitDT(ourVersion, 36);
        
        console.log(`Their original: ${theirOriginal} -> ${theirDecoded}`);
        console.log(`Our version:    ${ourVersion} -> ${ourDecoded}`);
        console.log(`Compatibility: ${theirOriginal === ourVersion && theirDecoded === ourDecoded ? '✅ PERFECT MATCH' : '❌ BROKEN'}`);
        
        const theirStyle = BitDTEpoch.toBitDT(testTime, 36);
        const decoded = BitDTEpoch.fromBitDT(theirStyle);
        console.log(`Their use case: ${theirStyle} -> ${decoded} : ${testTime === decoded ? '✅ WORKS' : '❌ BROKEN'}`);
        
        console.log();
        
        console.log("8. BUILT-IN BENCHMARK");
        console.log("=====================");
        BitDTEpoch.benchmark(testTime);
    }
}

// Run tests if this file is executed directly
if (require.main === module) {
    BitDTEpochTest.main();
}
