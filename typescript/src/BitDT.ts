import { ThousandCounter } from "./ThousandCounter.js";
const MONTHS = "ABCDEFGHIJKL";
const DAYS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345";
const HOURS = "BCDEFGHJKLMNOPQRSTUVWXYZ";
const MINUTES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345678";
const YEAR_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789";

const TWO_ZEROS = '.';
const THREE_ZEROS = ':';
const FOUR_ZEROS = ';';
const FIVE_ZEROS = '?';
const SIX_ZEROS = '!';
const SEVEN_ZEROS = '&';

const UTC_PLUS = '+';
const UTC_MINUS = '-';
const TIMEZONE_DIGITS = "0123456789";

const YEAR_MASK = 0xFFFFF00000000000n;
const MONTH_MASK = 0x00000F0000000000n;
const DAY_MASK = 0x000000F800000000n;
const HOUR_MASK = 0x00000007C0000000n;
const MINUTE_MASK = 0x000000003F000000n;
const SECOND_MASK = 0x0000000000FC0000n;
const MILLIS_MASK = 0x000000000003FF00n;
const TYPE_MASK = 0xF000000000000000n;

export const TYPE_EMPTY = 0;
export const TYPE_FULL = 1;
export const TYPE_DATE_ONLY = 2;
export const TYPE_TIME_ONLY = 3;
const TYPE_DATE_HOUR = 4;

const EMPTY_VALUE = BigInt(TYPE_EMPTY);

export class BitDT {
    private readonly packedValue: bigint;
    private readonly timezoneOffset: number;
    private readonly dateType: number;

    private constructor(packedValue: bigint, timezoneOffset: number, dateType: number) {
        this.packedValue = packedValue;
        this.timezoneOffset = timezoneOffset;
        this.dateType = dateType;
    }

    static fromPrimitives(
        year: number, 
        month: number, 
        day: number, 
        hour: number, 
        minute: number, 
        second: number, 
        millis: number, 
        timezone?: string
    ): BitDT {
        const dateType = BitDT.determineDateType(year, month, day, hour, minute, second, millis);
        
        const packed = (BigInt(year) << 44n) |
                      (BigInt(month) << 40n) |
                      (BigInt(day) << 35n) |
                      (BigInt(hour) << 30n) |
                      (BigInt(minute) << 24n) |
                      (BigInt(second) << 18n) |
                      (BigInt(millis) << 8n) |
                      BigInt(dateType);
        
        const tzOffset = BitDT.parseTimezoneOffset(timezone);
        return new BitDT(packed, tzOffset, dateType);
    }

    private static determineDateType(
        year: number, month: number, day: number, 
        hour: number, minute: number, second: number, millis: number
    ): number {
        if (year === 0 && month === 0 && day === 0 && hour === 0 && minute === 0 && second === 0 && millis === 0) {
            return TYPE_EMPTY;
        } else if (hour === 0 && minute === 0 && second === 0 && millis === 0) {
            return TYPE_DATE_ONLY;
        } else if (year === 0 && month === 0 && day === 0) {
            return TYPE_TIME_ONLY;
        } else if (minute === 0 && second === 0 && millis === 0 && hour !== 0) {
            return TYPE_DATE_HOUR;
        } else {
            return TYPE_FULL;
        }
    }

    static fromPackedValue(packedValue: bigint, timezoneOffset: number, dateType: number): BitDT {
        return new BitDT(packedValue, timezoneOffset, dateType);
    }

    static createEmpty(): BitDT {
        return new BitDT(EMPTY_VALUE, 0, TYPE_EMPTY);
    }

    getYear(): number {
        if (this.dateType === TYPE_EMPTY || this.dateType === TYPE_TIME_ONLY) return -1;
        return Number((this.packedValue & YEAR_MASK) >> 44n);
    }

    getMonth(): number {
        if (this.dateType === TYPE_EMPTY || this.dateType === TYPE_TIME_ONLY) return -1;
        return Number((this.packedValue & MONTH_MASK) >> 40n);
    }

    getDay(): number {
        if (this.dateType === TYPE_EMPTY || this.dateType === TYPE_TIME_ONLY) return -1;
        return Number((this.packedValue & DAY_MASK) >> 35n);
    }

    getHour(): number {
        if (this.dateType === TYPE_EMPTY || this.dateType === TYPE_DATE_ONLY) return -1;
        return Number((this.packedValue & HOUR_MASK) >> 30n);
    }

    getMinute(): number {
        if (this.dateType === TYPE_EMPTY || this.dateType === TYPE_DATE_ONLY) return -1;
        if (this.dateType === TYPE_DATE_HOUR) return 0;
        return Number((this.packedValue & MINUTE_MASK) >> 24n);
    }

    getSecond(): number {
        if (this.dateType === TYPE_EMPTY || this.dateType === TYPE_DATE_ONLY) return -1;
        if (this.dateType === TYPE_DATE_HOUR) return 0;
        return Number((this.packedValue & SECOND_MASK) >> 18n);
    }

    getMillis(): number {
        if (this.dateType === TYPE_EMPTY || this.dateType === TYPE_DATE_ONLY) return -1;
        if (this.dateType === TYPE_DATE_HOUR) return 0;
        return Number((this.packedValue & MILLIS_MASK) >> 8n);
    }

    getTimezone(): string | null {
        return BitDT.formatTimezoneOffset(this.timezoneOffset);
    }

    getPackedValue(): bigint {
        return this.packedValue;
    }

    getTimezoneOffset(): number {
        return this.timezoneOffset;
    }

    getDateType(): number {
        return this.dateType;
    }

    isEmpty(): boolean {
        return this.dateType === TYPE_EMPTY;
    }

    private static parseTimezoneOffset(timezone?: string | null): number {
        if (!timezone || timezone.length === 0) {
            return 0;
        }

        if (timezone === "+" || timezone === "-") {
            return 0;
        }

        try {
            let hours = 0;
            let minutes = 0;
            
            if (timezone.length === 3) {
                hours = parseInt(timezone.substring(1, 3));
            } else if (timezone.length === 5) {
                hours = parseInt(timezone.substring(1, 3));
                minutes = parseInt(timezone.substring(3, 5));
            }
            
            let totalMinutes = hours * 60 + minutes;
            if (timezone.charAt(0) === '-') {
                totalMinutes = -totalMinutes;
            }
            
            return Math.floor(totalMinutes / 15);
        } catch {
            return 0;
        }
    }

    private static formatTimezoneOffset(offset: number): string | null {
        if (offset === 0) {
            return null;
        }
        
        const totalMinutes = offset * 15;
        const hours = Math.abs(totalMinutes) / 60;
        const minutes = Math.abs(totalMinutes) % 60;
        
        if (minutes === 0) {
            return `${totalMinutes >= 0 ? '+' : '-'}${hours.toString().padStart(2, '0')}`;
        } else {
            return `${totalMinutes >= 0 ? '+' : '-'}${hours.toString().padStart(2, '0')}${minutes.toString().padStart(2, '0')}`;
        }
    }

    static encodeYear(year: number): string {
        if (year < 0 || year >= 226981) {
            throw new Error("Year must be between 0 and 226,980");
        }
        const c1 = YEAR_CHARS.charAt(Math.floor(year / (61 * 61)));
        const c2 = YEAR_CHARS.charAt(Math.floor((year / 61) % 61));
        const c3 = YEAR_CHARS.charAt(year % 61);
        return c1 + c2 + c3;
    }

    static decodeYear(yearCode: string): number {
        if (!yearCode || yearCode.length !== 3) return -1;
        const idx1 = YEAR_CHARS.indexOf(yearCode.charAt(0));
        const idx2 = YEAR_CHARS.indexOf(yearCode.charAt(1));
        const idx3 = YEAR_CHARS.indexOf(yearCode.charAt(2));
        if (idx1 === -1 || idx2 === -1 || idx3 === -1) return -1;
        return idx1 * 61 * 61 + idx2 * 61 + idx3;
    }

    static toAbsoluteYear(relativeYear: number): number {
        return relativeYear - 50000;
    }

    static fromAbsoluteYear(absoluteYear: number): number {
        return absoluteYear + 50000;
    }

    encode(): string {
        if (this.isEmpty()) {
            return "&";
        }
        
        let year = this.getYear();
        let month = this.getMonth();
        let day = this.getDay();
        let hour = this.getHour();
        let minute = this.getMinute();
        let second = this.getSecond();
        let millis = this.getMillis();
        const timezone = this.getTimezone();

        if (this.dateType === TYPE_DATE_ONLY) {
            hour = -1;
            minute = -1;
            second = -1;
            millis = -1;
        } else if (this.dateType === TYPE_TIME_ONLY) {
            year = -1;
            month = -1;
            day = -1;
        } else if (this.dateType === TYPE_DATE_HOUR) {
            minute = -1;
            second = -1;
            millis = -1;
        }

        const finalYear = year !== -1 ? year : null;
        const finalMonth = month !== -1 ? month : null;
        const finalDay = day !== -1 ? day : null;
        const finalHour = hour !== -1 ? hour : null;
        const finalMinute = minute !== -1 ? minute : null;
        const finalSecond = second !== -1 ? second : null;
        const finalMillis = millis !== -1 ? millis : null;

        return BitDT.encodeDateTime(finalYear, finalMonth, finalDay, finalHour, finalMinute, finalSecond, finalMillis, timezone);
    }

    static encodeDateTime(
        year: number | null,
        month: number | null, 
        day: number | null,
        hour: number | null,
        minute: number | null,
        second: number | null,
        millis: number | null,
        timezone?: string | null
    ): string {
        try {
            let yearCode = "0";
            if (year !== null && year >= 0) {
                yearCode = BitDT.encodeYear(year);
            }
            
            let monthChar = '0';
            if (month !== null && month >= 0) {
                if (month < 0 || month >= MONTHS.length) {
                    throw new Error(`Month must be between 0 and ${MONTHS.length - 1}`);
                }
                monthChar = MONTHS.charAt(month);
            }
            
            let dayChar = '0';
            if (day !== null && day >= 1) {
                if (day < 1 || day > DAYS.length) {
                    throw new Error(`Day must be between 1 and ${DAYS.length}`);
                }
                dayChar = DAYS.charAt(day - 1);
            }
            
            let hourChar = '0';
            if (hour !== null && hour >= 0) {
                if (hour < 0 || hour >= HOURS.length) {
                    throw new Error(`Hour must be between 0 and ${HOURS.length - 1}`);
                }
                hourChar = HOURS.charAt(hour);
            }
            
            let minuteChar = '0';
            if (minute !== null && minute >= 0) {
                if (minute < 0 || minute >= MINUTES.length) {
                    throw new Error(`Minute must be between 0 and ${MINUTES.length - 1}`);
                }
                minuteChar = MINUTES.charAt(minute);
            }
            
            let secondChar = '0';
            if (second !== null && second >= 0) {
                if (second < 0 || second >= MINUTES.length) {
                    throw new Error(`Second must be between 0 and ${MINUTES.length - 1}`);
                }
                secondChar = MINUTES.charAt(second);
            }
            
            let millisCode = "0";
            if (millis !== null && millis >= 0) {
                if (millis === 0) {
                    millisCode = "0";
                } else {
                    millisCode = ThousandCounter.encodeMilliseconds(millis);
                }
            }
            
            const uncompressed = yearCode + monthChar + dayChar + hourChar + minuteChar + secondChar + millisCode;
            const dateTimePart = BitDT.compressZeros(uncompressed);
            
            if (timezone && timezone.length > 0) {
                return dateTimePart + timezone;
            }
            
            return dateTimePart;
        } catch {
            return "&";
        }
    }

    static decode(bitdt: string): BitDT {
        if (!bitdt) {
            return BitDT.createEmpty();
        }
        
        const timezoneStr = BitDT.parseTimezone(bitdt);
        const dateTimePart = BitDT.extractDateTimePart(bitdt, timezoneStr);
        
        if (!dateTimePart) {
            return BitDT.createEmpty();
        }
        
        if (dateTimePart.length > 0 && dateTimePart.charAt(0) !== '0' && dateTimePart.length < 3) {
            return BitDT.createEmpty();
        }
        
        if (dateTimePart.length === 0) {
            return BitDT.createEmpty();
        }
        
        const expanded = BitDT.expandZeros(dateTimePart);
        
        if (expanded.length < 7) {
            return BitDT.createEmpty();
        }
        
        for (let i = 0; i < expanded.length; i++) {
            const c = expanded.charAt(i);
            if (!BitDT.isValidEncodedChar(c)) {
                return BitDT.createEmpty();
            }
        }
        
        let year = -1, month = -1, day = -1, hour = -1, minute = -1, second = -1, millis = -1;
        let pos = 0;
        
        if (pos < expanded.length) {
            if (expanded.charAt(pos) === '0') {
                year = -1;
                pos += 1;
            } else if (pos + 2 < expanded.length) {
                const yearCode = expanded.substring(pos, pos + 3);
                year = BitDT.decodeYear(yearCode);
                if (year === -1) {
                    return BitDT.createEmpty();
                }
                pos += 3;
            } else {
                return BitDT.createEmpty();
            }
        }
        
        if (pos < expanded.length) {
            if (expanded.charAt(pos) === '0') {
                month = -1;
            } else {
                month = MONTHS.indexOf(expanded.charAt(pos));
                if (month === -1) {
                    return BitDT.createEmpty();
                }
            }
            pos += 1;
        }
        
        if (pos < expanded.length) {
            if (expanded.charAt(pos) === '0') {
                day = -1;
            } else {
                day = DAYS.indexOf(expanded.charAt(pos)) + 1;
                if (day === 0) {
                    return BitDT.createEmpty();
                }
            }
            pos += 1;
        }
        
        if (pos < expanded.length) {
            if (expanded.charAt(pos) === '0') {
                hour = -1;
            } else {
                hour = HOURS.indexOf(expanded.charAt(pos));
                if (hour === -1) {
                    return BitDT.createEmpty();
                }
            }
            pos += 1;
        }
        
        if (pos < expanded.length) {
            if (expanded.charAt(pos) === '0') {
                minute = -1;
            } else {
                minute = MINUTES.indexOf(expanded.charAt(pos));
                if (minute === -1) {
                    return BitDT.createEmpty();
                }
            }
            pos += 1;
        }
        
        if (pos < expanded.length) {
            if (expanded.charAt(pos) === '0') {
                second = -1;
            } else {
                second = MINUTES.indexOf(expanded.charAt(pos));
                if (second === -1) {
                    return BitDT.createEmpty();
                }
            }
            pos += 1;
        }
        
        if (pos < expanded.length) {
            if (expanded.charAt(pos) === '0') {
                millis = 0;
                pos += 1;
            } else if (pos + 1 < expanded.length) {
                const millisCode = expanded.substring(pos, pos + 2);
                millis = ThousandCounter.decodeMilliseconds(millisCode);
                if (millis === -1) {
                    return BitDT.createEmpty();
                }
                pos += 2;
            } else {
                return BitDT.createEmpty();
            }
        }
        
        if (year === -1 && month === -1 && day === -1 && hour === -1 && minute === -1 && second === -1 && millis === -1) {
            return BitDT.createEmpty();
        }
        
        const tzOffset = BitDT.parseTimezoneOffset(timezoneStr);
        
        const finalYear = year !== -1 ? year : 0;
        const finalMonth = month !== -1 ? month : 0;
        const finalDay = day !== -1 ? day : 0;
        const finalHour = hour !== -1 ? hour : 0;
        const finalMinute = minute !== -1 ? minute : 0;
        const finalSecond = second !== -1 ? second : 0;
        const finalMillis = millis !== -1 ? millis : 0;
        
        return BitDT.fromPrimitives(finalYear, finalMonth, finalDay, finalHour, finalMinute, finalSecond, finalMillis, timezoneStr || undefined);
    }

    getNumericalValue(): bigint {
        return this.packedValue;
    }

    compareTo(other: BitDT): number {
        if (this.packedValue < other.packedValue) return -1;
        if (this.packedValue > other.packedValue) return 1;
        return 0;
    }

    before(other: BitDT): boolean {
        return this.packedValue < other.packedValue;
    }

    after(other: BitDT): boolean {
        return this.packedValue > other.packedValue;
    }

    equals(other: any): boolean {
        if (this === other) return true;
        if (!(other instanceof BitDT)) return false;
        return this.packedValue === other.packedValue && this.timezoneOffset === other.timezoneOffset;
    }

    hashCode(): number {
        return Number(this.packedValue ^ (this.packedValue >> 32n)) + this.timezoneOffset;
    }

    static sortByNumericalValue(dates: BitDT[]): BitDT[] {
        if (!dates) return [];
        return [...dates].sort((a, b) => a.compareTo(b));
    }

    static fromNumericalArray(numericalValues: bigint[]): BitDT[] {
        if (!numericalValues) return [];
        return numericalValues.map(value => BitDT.fromPackedValue(value, 0, TYPE_FULL));
    }

    static toNumericalArray(dates: BitDT[]): bigint[] {
        return dates.map(dt => dt.getNumericalValue());
    }

    static fromNumericalValue(numericalValue: bigint): BitDT {
        return BitDT.fromPackedValue(numericalValue, 0, TYPE_FULL);
    }

    private static compressZeros(fields: string): string {
        let compressed = "";
        let zeroCount = 0;
        
        for (let i = 0; i < fields.length; i++) {
            const c = fields.charAt(i);
            if (c === '0') {
                zeroCount++;
            } else {
                if (zeroCount > 0) {
                    compressed += BitDT.encodeZeroRun(zeroCount);
                    zeroCount = 0;
                }
                compressed += c;
            }
        }
        
        if (zeroCount > 0) {
            compressed += BitDT.encodeZeroRun(zeroCount);
        }
        
        return compressed;
    }

    private static encodeZeroRun(zeroCount: number): string {
        switch (zeroCount) {
            case 1: return "0";
            case 2: return ".";
            case 3: return ":";
            case 4: return ";";
            case 5: return "?";
            case 6: return "!";
            case 7: return "&";
            default: 
                let sb = "&";
                for (let i = 0; i < zeroCount - 7; i++) {
                    sb += "0";
                }
                return sb;
        }
    }

    private static expandZeros(compressed: string): string {
        let expanded = "";
        
        for (let i = 0; i < compressed.length; i++) {
            const c = compressed.charAt(i);
            switch (c) {
                case '0': expanded += '0'; break;
                case '.': expanded += "00"; break;
                case ':': expanded += "000"; break;
                case ';': expanded += "0000"; break;
                case '?': expanded += "00000"; break;
                case '!': expanded += "000000"; break;
                case '&': expanded += "0000000"; break;
                default: expanded += c; break;
            }
        }
        
        return expanded;
    }

    private static parseTimezone(bitdt: string): string | null {
        if (!bitdt || bitdt.length < 2) {
            return null;
        }
        
        const lastIndex = bitdt.length - 1;
        const lastChar = bitdt.charAt(lastIndex);
        
        if (lastChar === UTC_PLUS || lastChar === UTC_MINUS) {
            return lastChar;
        }
        
        for (let i = bitdt.length - 1; i >= 0; i--) {
            const c = bitdt.charAt(i);
            if (c === UTC_PLUS || c === UTC_MINUS) {
                const timezonePart = bitdt.substring(i);
                if (BitDT.isValidTimezone(timezonePart)) {
                    return timezonePart;
                }
            }
        }
        
        return null;
    }

    private static isValidTimezone(timezone: string): boolean {
        if (!timezone || timezone.length < 2) {
            return false;
        }
        
        const sign = timezone.charAt(0);
        if (sign !== UTC_PLUS && sign !== UTC_MINUS) {
            return false;
        }
        
        const digits = timezone.substring(1);
        if (digits.length === 0) {
            return true;
        }
        
        for (let i = 0; i < digits.length; i++) {
            if (TIMEZONE_DIGITS.indexOf(digits.charAt(i)) === -1) {
                return false;
            }
        }
        
        return digits.length === 2 || digits.length === 4;
    }

    private static extractDateTimePart(bitdt: string, timezone: string | null): string {
        if (!bitdt) {
            return "";
        }
        
        if (timezone !== null) {
            return bitdt.substring(0, bitdt.length - timezone.length);
        }
        
        return bitdt;
    }

    private static isValidEncodedChar(c: string): boolean {
        return YEAR_CHARS.includes(c) || 
               MONTHS.includes(c) ||
               DAYS.includes(c) ||
               HOURS.includes(c) ||
               MINUTES.includes(c) ||
               '0.:;?!&'.includes(c);
    }
}