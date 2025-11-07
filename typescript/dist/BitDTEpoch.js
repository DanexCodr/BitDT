"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.BitDTEpoch = void 0;
const BitDT_js_1 = require("./BitDT.js");
class BitDTEpoch {
    static toBitDT(epochMillis, timezoneOrBase, base) {
        let timezone = undefined;
        let actualBase;
        if (typeof timezoneOrBase === 'string' || timezoneOrBase === undefined) {
            timezone = timezoneOrBase;
            actualBase = base ?? BitDTEpoch.MODE_AUTO;
        }
        else {
            actualBase = timezoneOrBase ?? BitDTEpoch.MODE_AUTO;
        }
        try {
            if (actualBase === BitDTEpoch.MODE_FULL_BITDT) {
                return BitDTEpoch.toFullBitDT(epochMillis, timezone);
            }
            if (actualBase === BitDTEpoch.MODE_AUTO) {
                return BitDTEpoch.toSmartBitDT(epochMillis, timezone);
            }
            if (actualBase >= 2 && actualBase <= 36) {
                return BitDTEpoch.encodeBase(epochMillis, actualBase);
            }
            return BitDTEpoch.toSmartBitDT(epochMillis, timezone);
        }
        catch (e) {
            return BitDT_js_1.BitDT.createEmpty().encode();
        }
    }
    static fromBitDT(bitdt, base) {
        if (!bitdt || bitdt.length === 0) {
            return -1;
        }
        try {
            if (base !== undefined && base >= 2 && base <= 36) {
                return parseInt(bitdt.toLowerCase(), base);
            }
            if (BitDTEpoch.isBaseEncoded(bitdt)) {
                return BitDTEpoch.decodeBaseAuto(bitdt);
            }
            return BitDTEpoch.fromFullBitDT(bitdt);
        }
        catch (e) {
            return -1;
        }
    }
    static toSmartBitDT(epochMillis, timezone) {
        if (timezone === undefined || timezone === "UTC" || timezone === "+00" ||
            timezone === "Z" || timezone === "+0000") {
            return BitDTEpoch.encodeBase(epochMillis, BitDTEpoch.MODE_BASE36);
        }
        else {
            return BitDTEpoch.toFullBitDT(epochMillis, timezone);
        }
    }
    static toFullBitDT(epochMillis, timezone) {
        const date = new Date(epochMillis);
        const year = date.getUTCFullYear();
        const month = date.getUTCMonth();
        const day = date.getUTCDate();
        const hour = date.getUTCHours();
        const minute = date.getUTCMinutes();
        const second = date.getUTCSeconds();
        const millis = date.getUTCMilliseconds();
        let finalTimezone = timezone;
        if (timezone && timezone !== "UTC") {
            const offset = BitDTEpoch.parseZoneOffset(timezone);
            if (offset !== 0) {
                finalTimezone = BitDTEpoch.formatTimezoneOffset(offset);
            }
            else {
                finalTimezone = undefined;
            }
        }
        return BitDT_js_1.BitDT.fromPrimitives(BitDT_js_1.BitDT.fromAbsoluteYear(year), month, day, hour, minute, second, millis, finalTimezone).encode();
    }
    static fromFullBitDT(bitdt) {
        try {
            const dt = BitDT_js_1.BitDT.decode(bitdt);
            if (dt.isEmpty()) {
                return -1;
            }
            let year = dt.getYear();
            if (year === -1) {
                year = BitDT_js_1.BitDT.fromAbsoluteYear(new Date().getUTCFullYear());
            }
            const absoluteYear = BitDT_js_1.BitDT.toAbsoluteYear(year);
            const month = dt.getMonth() !== -1 ? dt.getMonth() : 0;
            const day = dt.getDay() !== -1 ? dt.getDay() : 1;
            const hour = dt.getHour() !== -1 ? dt.getHour() : 0;
            const minute = dt.getMinute() !== -1 ? dt.getMinute() : 0;
            const second = dt.getSecond() !== -1 ? dt.getSecond() : 0;
            const millis = dt.getMillis() !== -1 ? dt.getMillis() : 0;
            let timezone = dt.getTimezone();
            let offsetMinutes = 0;
            if (timezone) {
                const offset = BitDTEpoch.parseZoneOffset(timezone);
                offsetMinutes = offset * 15;
            }
            const date = new Date(Date.UTC(absoluteYear, month, day, hour, minute, second, millis));
            if (offsetMinutes !== 0) {
                date.setMinutes(date.getMinutes() - offsetMinutes);
            }
            return date.getTime();
        }
        catch (e) {
            console.error("Failed to decode Full BitDT:", bitdt, e);
            return -1;
        }
    }
    static decodeBaseAuto(encoded) {
        const likelyBase = BitDTEpoch.guessMostLikelyBase(encoded);
        try {
            return parseInt(encoded.toLowerCase(), likelyBase);
        }
        catch (e) {
        }
        const bases = [36, 32, 16, 10];
        for (const base of bases) {
            if (base === likelyBase)
                continue;
            try {
                return parseInt(encoded.toLowerCase(), base);
            }
            catch (e) {
                continue;
            }
        }
        return BitDTEpoch.fromFullBitDT(encoded);
    }
    static guessMostLikelyBase(encoded) {
        let hasBase36Only = false;
        let hasBase32Only = false;
        let hasHexOnly = true;
        for (let i = 0; i < encoded.length; i++) {
            const c = encoded.charAt(i);
            if (c >= 'G' && c <= 'Z') {
                if (c >= 'W' && c <= 'Z') {
                    hasBase36Only = true;
                }
                else {
                    hasBase32Only = true;
                }
                hasHexOnly = false;
            }
            else if (c >= 'A' && c <= 'F') {
            }
            else if (c >= '0' && c <= '9') {
            }
            else {
                hasHexOnly = false;
            }
        }
        if (hasHexOnly)
            return 16;
        if (hasBase36Only)
            return 36;
        if (hasBase32Only)
            return 32;
        return 36;
    }
    static isBaseEncoded(str) {
        if (str.length < 6 || str.length > 12) {
            return false;
        }
        for (let i = 0; i < str.length; i++) {
            const c = str.charAt(i);
            if (BitDTEpoch.BASE36_CHARS.indexOf(c) === -1) {
                return false;
            }
        }
        return true;
    }
    static parseZoneOffset(timezone) {
        if (!timezone || timezone === "UTC") {
            return 0;
        }
        try {
            if (timezone.length === 3) {
                const hours = parseInt(timezone.substring(1, 3));
                let totalSeconds = hours * 3600;
                if (timezone.charAt(0) === '-') {
                    totalSeconds = -totalSeconds;
                }
                return Math.floor(totalSeconds / 900);
            }
            if (timezone.length === 6 && timezone.charAt(3) === ':') {
                const hours = parseInt(timezone.substring(1, 3));
                const minutes = parseInt(timezone.substring(4, 6));
                let totalSeconds = hours * 3600 + minutes * 60;
                if (timezone.charAt(0) === '-') {
                    totalSeconds = -totalSeconds;
                }
                return Math.floor(totalSeconds / 900);
            }
            if (timezone.length === 5) {
                const hours = parseInt(timezone.substring(1, 3));
                const minutes = parseInt(timezone.substring(3, 5));
                let totalSeconds = hours * 3600 + minutes * 60;
                if (timezone.charAt(0) === '-') {
                    totalSeconds = -totalSeconds;
                }
                return Math.floor(totalSeconds / 900);
            }
            return 0;
        }
        catch (e) {
            return 0;
        }
    }
    static formatTimezoneOffset(offset) {
        if (offset === 0) {
            return "+00";
        }
        const totalMinutes = offset * 15;
        const hours = Math.floor(Math.abs(totalMinutes) / 60);
        const minutes = Math.abs(totalMinutes) % 60;
        if (minutes === 0) {
            return `${totalMinutes >= 0 ? '+' : '-'}${hours.toString().padStart(2, '0')}`;
        }
        else {
            return `${totalMinutes >= 0 ? '+' : '-'}${hours.toString().padStart(2, '0')}${minutes.toString().padStart(2, '0')}`;
        }
    }
    static now(timezoneOrBase, base) {
        const currentTime = Date.now();
        if (typeof timezoneOrBase === 'string') {
            return BitDTEpoch.toBitDT(currentTime, timezoneOrBase, base ?? BitDTEpoch.MODE_AUTO);
        }
        else if (typeof timezoneOrBase === 'number') {
            return BitDTEpoch.toBitDT(currentTime, timezoneOrBase);
        }
        else {
            return BitDTEpoch.toBitDT(currentTime);
        }
    }
    static debugNow() {
        const currentMillis = Date.now();
        const base36 = BitDTEpoch.encodeBase(currentMillis, 36);
        const auto = BitDTEpoch.now();
        console.log("Debug Now():");
        console.log("  Date.now(): " + currentMillis);
        console.log("  Base36 encoded: " + base36);
        console.log("  now() result: " + auto);
        console.log("  now() decoded: " + BitDTEpoch.fromBitDT(auto));
        if (BitDTEpoch.fromBitDT(auto) === currentMillis) {
            console.log("  ✅ now() working correctly");
        }
        else {
            console.log("  ❌ now() has issues");
            console.log("  Expected: " + currentMillis);
            console.log("  Got: " + BitDTEpoch.fromBitDT(auto));
        }
    }
    static encodeBase(value, base) {
        return value.toString(base).toUpperCase();
    }
    static benchmark(epochMillis) {
        console.log("Benchmark for: " + epochMillis);
        const bases = [2, 10, 16, 32, 36];
        for (const base of bases) {
            try {
                const result = BitDTEpoch.toBitDT(epochMillis, base);
                console.log(`Base ${base.toString().padStart(2)}: ${result.padEnd(20)} (${result.length} chars)`);
            }
            catch (e) {
                console.log(`Base ${base.toString().padStart(2)}: ERROR - ${e.message}`);
            }
        }
        const full = BitDTEpoch.toBitDT(epochMillis, undefined, BitDTEpoch.MODE_FULL_BITDT);
        console.log(`Full BitDT: ${full} (${full.length} chars)`);
        const auto = BitDTEpoch.toBitDT(epochMillis);
        console.log(`Auto mode:  ${auto} (${auto.length} chars)`);
    }
}
exports.BitDTEpoch = BitDTEpoch;
BitDTEpoch.MODE_AUTO = 0;
BitDTEpoch.MODE_BASE36 = 36;
BitDTEpoch.MODE_FULL_BITDT = -1;
BitDTEpoch.BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
