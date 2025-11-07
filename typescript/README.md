# BitDT for TypeScript/JavaScript 🗜️⏰

[![TypeScript](https://img.shields.io/badge/TypeScript-✓-3178C6.svg)](https://typescriptlang.org)
[![JavaScript](https://img.shields.io/badge/JavaScript-ES2020%2B-yellow.svg)](https://javascript.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Modern date-time compression library for TypeScript and JavaScript applications. Provides **60-80% space savings** with full type safety and zero dependencies.

## 🌟 TypeScript/JavaScript Features

- **🔄 Dual Support** - Use in both TypeScript and JavaScript projects
- **🎯 Full Type Safety** - Complete TypeScript definitions
- **🌐 Universal Runtime** - Works in Node.js, browsers, and Deno
- **📦 Zero Dependencies** - Pure TypeScript/JavaScript implementation
- **⚡ Modern ES2020+** - Latest JavaScript features
- **🔧 Build Ready** - Includes full build system

## 🚀 Quick Start

### Installation

#### For TypeScript Projects:
```bash
git clone https://github.com/Danexcodr/BitDT.git
cd BitDT/typescript
npm install
npm run build  # Compiles TypeScript to JavaScript
```

#### For JavaScript Projects:

```bash
git clone https://github.com/Danexcodr/BitDT.git
# Use pre-compiled files from typescript/dist/
```

Project Structure

```
typescript/
├── src/                 # TypeScript source files
│   ├── BitDT.ts         # Main date-time class
│   ├── BitDTEpoch.ts    # Epoch time utilities
│   ├── ThousandCounter.ts # Millisecond encoding
│   ├── BitDTTest.ts     # Test suite
│   ├── BitDTExample.ts  # Usage examples
│   └── index.ts         # Main exports
├── dist/                # Compiled JavaScript (.gitignored)
├── package.json         # Project configuration
├── tsconfig.json        # TypeScript configuration
└── README.md           # This file
```

## 🔜 NPM Package (Coming Soon)

```bash
# Future NPM installation
npm install bitdt
```

## 📖 Basic Usage

#### TypeScript

```typescript
import { BitDT } from './src/BitDT';

// Create a compact date-time
const dateTime = BitDT.fromPrimitives(
    BitDT.fromAbsoluteYear(2024), // Convert to relative year
    5,  // Month (0-11)
    15, // Day (1-31)
    14, // Hour (0-23)
    30, // Minute (0-59)
    45, // Second (0-59)
    123, // Milliseconds (0-999)
    "+08:00" // Timezone (optional)
);

const encoded = dateTime.encode(); // Compact string like "ABC123Xyz+08"
console.log("Encoded:", encoded); // ~6-10 characters instead of 20+

// Decode back to full date-time
const decoded = BitDT.decode(encoded);

// Verify round-trip
console.log("Round-trip successful:", dateTime.equals(decoded));
```

#### JavaScript

```javascript
const { BitDT } = require('./dist/BitDT');

// Create a compact date-time
const dateTime = BitDT.fromPrimitives(
    BitDT.fromAbsoluteYear(2024),
    5, 15, 14, 30, 45, 123, "+08:00"
);

const encoded = dateTime.encode();
console.log("Encoded:", encoded);

const decoded = BitDT.decode(encoded);
console.log("Round-trip successful:", dateTime.equals(decoded));
```

## 🔧 Advanced Usage

#### Epoch Time Utilities

```typescript
import { BitDTEpoch } from './src/BitDTEpoch';

// Convert epoch milliseconds to compact representation
const epochMillis = Date.now();
const encoded = BitDTEpoch.toBitDT(epochMillis); // Auto-detects best encoding

// Convert back to epoch time
const decodedMillis = BitDTEpoch.fromBitDT(encoded);

// Use specific encoding base
const base36 = BitDTEpoch.toBitDT(epochMillis, 36); // Force Base36 encoding
const withTimezone = BitDTEpoch.toBitDT(epochMillis, "+08:00", BitDTEpoch.MODE_FULL_BITDT);

// Get current time
const now = BitDTEpoch.now();
```

#### Different Date Types

```typescript
// Full date-time with timezone
const full = BitDT.fromPrimitives(BitDT.fromAbsoluteYear(2024), 5, 15, 14, 30, 45, 123, "+08:00");
console.log("Full:", full.encode());

// Date-only (no time component)
const dateOnly = BitDT.fromPrimitives(BitDT.fromAbsoluteYear(2024), 5, 15, 0, 0, 0, 0, undefined);
console.log("Date only:", dateOnly.encode());

// Time-only (no date component)
const timeOnly = BitDT.fromPrimitives(0, 0, 0, 14, 30, 45, 123, undefined);
console.log("Time only:", timeOnly.encode());

// Empty timestamp
const empty = BitDT.createEmpty();
console.log("Empty:", empty.encode());
```

#### Sorting and Comparison

```typescript
const events = [
    BitDT.fromPrimitives(BitDT.fromAbsoluteYear(2024), 5, 15, 14, 30, 0, 0, undefined),
    BitDT.fromPrimitives(BitDT.fromAbsoluteYear(2024), 3, 10, 9, 0, 0, 0, undefined),
    BitDT.fromPrimitives(BitDT.fromAbsoluteYear(2024), 1, 1, 0, 0, 0, 0, undefined)
];

// Sort by numerical value
const sorted = BitDT.sortByNumericalValue(events);

// Compare dates
const first = sorted[0];
const last = sorted[sorted.length - 1];
console.log("First before last:", first.before(last));
```

## 🛠️ Development

Building from Source

```bash
cd typescript
npm install          # Install dependencies
npm run build        # Compile TypeScript to JavaScript
npm run test         # Run test suite
npm run example      # Run examples
npm run epoch-test   # Run epoch utilities tests
npm run all          # Run all tests and examples
```

Available Scripts

· npm run build - Compile TypeScript to dist/
· npm run test - Run BitDT test suite
· npm run example - Run usage examples
· npm run epoch-test - Test epoch time utilities
· npm run all - Run all tests and examples
· npm run clean - Clear build directory

## 🏗️ Architecture

#### BigInt for Precision

Uses JavaScript BigInt for 64-bit precision, matching Java's long type:

```typescript
private readonly packedValue: bigint;
private readonly timezoneOffset: number;
private readonly dateType: number;
```

#### Core Classes

· BitDT - Main date-time class with encoding/decoding
· BitDTEpoch - Epoch time conversion utilities
· ThousandCounter - Millisecond encoding (0-999 to 2 characters)

## 🎯 Use Cases

Ideal for TypeScript/JavaScript Applications:

· Web Applications - Reduce localStorage/sessionStorage usage
· Node.js Servers - Efficient logging and database storage
· React/Vue/Angular - Compact state management
· API Development - Reduced payload sizes
· Mobile Apps - Minimize data transmission (React Native, etc.)

## 🌐 Browser Usage

Direct Script Include:

```html
<script src="path/to/BitDT.js"></script>
<script>
    const dateTime = BitDT.fromPrimitives(/* ... */);
    const encoded = dateTime.encode();
    console.log("Encoded date:", encoded);
</script>
```

Module Import:

```javascript
import { BitDT } from './dist/BitDT.js';
// or
const { BitDT } = await import('./dist/BitDT.js');
```

---

Ready to optimize your web application?     [← Back to Main README](./README.md)

<div align="center">

Start saving 60-80% on date-time storage today! ⭐

</div>
