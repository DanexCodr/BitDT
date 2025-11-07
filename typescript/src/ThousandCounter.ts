const FIRST_CHAR = "BCDFGHJKLNPQRSTVWXYZbcdfghjklnpqrstvwxyz";
const SECOND_CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

export class ThousandCounter {
    static encodeMilliseconds(millis: number): string {
        if (millis < 0 || millis > 999) {
            throw new Error("Milliseconds must be between 0 and 999");
        }
        
        const firstIndex = Math.floor(millis / 25);
        const secondIndex = millis % 25;
        
        if (firstIndex < 0 || firstIndex >= FIRST_CHAR.length || 
            secondIndex < 0 || secondIndex >= SECOND_CHAR.length) {
            throw new Error(`Invalid millisecond value: ${millis}`);
        }
        
        return FIRST_CHAR.charAt(firstIndex) + SECOND_CHAR.charAt(secondIndex);
    }
    
    static decodeMilliseconds(code: string): number {
        if (!code || code.length !== 2) return -1;
        
        const firstIndex = FIRST_CHAR.indexOf(code.charAt(0));
        const secondIndex = SECOND_CHAR.indexOf(code.charAt(1));
        
        if (firstIndex === -1 || secondIndex === -1) return -1;
        
        return firstIndex * 25 + secondIndex;
    }
}