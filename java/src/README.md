# BitDT for Java 🗜️⏰

[![Java Version](https://img.shields.io/badge/Java-7%2B-blue.svg)](https://java.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Enterprise-ready date-time compression library for Java applications. Provides **60-80% space savings** through advanced bit packing and character encoding.

## 🌟 Java-Specific Features

- **🏢 Enterprise Ready** - Full Java 7+ compatibility
- **📦 Maven/Gradle Ready** - Standard project structure
- **🔧 Bulk Operations** - `BitDTArray` for efficient processing
- **🛡️ Thread-Safe** - Immutable design for concurrent environments
- **🎯 Complete API** - Full date-time manipulation capabilities

## 🚀 Quick Start

### Installation

Since BitDT is currently available as source code, you have several options:

#### Option 1: Clone and Copy
```bash
git clone https://github.com/Danexcodr/BitDT.git
cd BitDT/java
# Copy src/main/java/danexcodr/time/ directory to your project
```

Option 2: Download Source Files

Download the Java files and add them to your project's source directory:

```
your-project/
└── src/main/java/danexcodr/time/
   ├── BitDT.java           # Main date-time class
   ├── BitDTArray.java      # Bulk operations
   ├── BitDTEpoch.java      # Epoch time utilities
   ├── ThousandCounter.java # Millisecond encoding
   ├── BitDTExample.java    # Usage examples
   ├── BitDTTest.java       # Test suite
   └── BitDTEpochTest.java  # Epoch test suite
```

Option 3: Manual Download

1. Download individual .java files from the java/src/main/java/danexcodr/time/ directory
2. Place them in your project maintaining the package structure
3. Compile with your Java compiler

🔜 Package Manager Support (Coming Soon)

Maven Central and Gradle plugin support planned for future release.

```xml
<!-- Future Maven Support -->
<dependency>
    <groupId>com.danexcodr</groupId>
    <artifactId>bitdt</artifactId>
    <version>1.0.0</version>
</dependency>
```

```groovy
// Future Gradle Support
implementation 'com.danexcodr:bitdt:1.0.0'
```

📖 Basic Usage

```java
import danexcodr.time.BitDT;
import danexcodr.time.BitDTArray;
import java.util.*;

// Create a compact date-time
BitDT dateTime = BitDT.fromPrimitives(
    BitDT.fromAbsoluteYear(2024), // Convert to relative year
    5,  // Month (0-11)
    15, // Day (1-31)
    14, // Hour (0-23)
    30, // Minute (0-59)
    45, // Second (0-59)
    123, // Milliseconds (0-999)
    "+08:00" // Timezone
);

String encoded = dateTime.encode(); // Returns compact string like "ABC123Xyz+08"
System.out.println("Encoded: " + encoded); // ~6-10 characters instead of 20+

// Decode back to full date-time
BitDT decoded = BitDT.decode(encoded);

// Verify round-trip
System.out.println("Round-trip successful: " + dateTime.equals(decoded));
```

🔧 Advanced Usage

Efficient Bulk Operations

```java
// Create array of dates for efficient processing
List<BitDT> dates = Arrays.asList(
    BitDT.fromPrimitives(BitDT.fromAbsoluteYear(2024), 5, 15, 14, 30, 0, 0, null),
    BitDT.fromPrimitives(BitDT.fromAbsoluteYear(2024), 3, 10, 9, 0, 0, 0, null),
    BitDT.fromPrimitives(BitDT.fromAbsoluteYear(2024), 1, 1, 0, 0, 0, 0, "+05:30")
);

BitDTArray dateArray = BitDTArray.fromList(dates);

// Sort efficiently
BitDTArray sortedArray = dateArray.sorted();

// Filter by date type
BitDTArray dateOnly = dateArray.getDateOnly();

// Slice and concatenate
BitDTArray slice = dateArray.slice(0, 2);
BitDTArray combined = slice.concat(dateOnly);
```

Epoch Time Utilities

```java
import danexcodr.time.BitDTEpoch;

// Convert epoch milliseconds to compact representation
long epochMillis = System.currentTimeMillis();
String encoded = BitDTEpoch.toBitDT(epochMillis); // Auto-detects best encoding

// Convert back to epoch time
long decodedMillis = BitDTEpoch.fromBitDT(encoded);

// Use specific encoding base
String base36 = BitDTEpoch.toBitDT(epochMillis, 36); // Force Base36 encoding
String withTimezone = BitDTEpoch.toBitDT(epochMillis, "+08:00", BitDTEpoch.MODE_FULL_BITDT);
```

Different Date Types

```java
// Full date-time with timezone
BitDT full = BitDT.fromPrimitives(BitDT.fromAbsoluteYear(2024), 5, 15, 14, 30, 45, 123, "+08:00");
System.out.println("Full: " + full.encode());

// Date-only (no time component)
BitDT dateOnly = BitDT.fromPrimitives(BitDT.fromAbsoluteYear(2024), 5, 15, 0, 0, 0, 0, null);
System.out.println("Date only: " + dateOnly.encode());

// Time-only (no date component)
BitDT timeOnly = BitDT.fromPrimitives(0, 0, 0, 14, 30, 45, 123, null);
System.out.println("Time only: " + timeOnly.encode());

// Empty timestamp
BitDT empty = BitDT.createEmpty();
System.out.println("Empty: " + empty.encode());
```

🏗️ Architecture

Bit Packing Strategy

BitDT uses sophisticated bit packing to store all date-time components in a single 64-bit long:

```
[4 bits: Type][20 bits: Year][4 bits: Month][5 bits: Day]
[5 bits: Hour][6 bits: Minute][6 bits: Second][10 bits: Millis][4 bits: Reserved]
```

Core Classes

· BitDT - Main date-time class with encoding/decoding
· BitDTArray - Efficient bulk operations and sorting
· BitDTEpoch - Epoch time conversion utilities
· ThousandCounter - Millisecond encoding (0-999 to 2 characters)

🧪 Testing

Run the comprehensive test suite:

```bash
cd java
javac -cp src/main/java src/main/java/danexcodr/time/BitDTTest.java
java -cp src/main/java danexcodr.time.BitDTTest
```

🎯 Use Cases

Ideal for Java Applications:

· Enterprise Systems - Database timestamp optimization
· Android Applications - Reduced storage footprint
· Microservices - Efficient network transmission
· Financial Systems - High-performance date processing
· Logging Frameworks - Compact timestamp storage

🔍 API Reference

BitDT Core Methods

· fromPrimitives() - Create from individual components
· encode()/decode() - Compact string representation
· getNumericalValue() - Raw 64-bit numerical form
· before()/after()/compareTo() - Date comparison

BitDTArray Bulk Operations

· fromList()/toList() - Convert between list and array
· sorted() - Efficient numerical sorting
· filterByType() - Filter by date type
· slice()/concat() - Array manipulation

BitDTEpoch Utilities

· toBitDT() - Convert epoch time to compact format
· fromBitDT() - Convert compact format to epoch time
· now() - Current time in compact format

---

Ready to optimize your Java application? ← Back to Main README

<div align="center">

Start saving 60-80% on date-time storage today! ⭐

</div>
