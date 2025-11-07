# Changelog

## [1.1.2] - 2025-11-07

### Added

· Typescript support

### Updated

· Reorganized the /src path to be /java/src instead. For the future update.

## [1.1.0] - 2025-11-03

### Added

· BitDTEpoch utility class for epoch timestamp conversions

· Multiple encoding modes: AUTO, BASE36, FULL_BITDT

· Smart base auto-detection (2-36) during decoding

· Timezone support for epoch conversions

· Comprehensive BitDTEpochTest suite

· now() convenience methods for current time

· Performance benchmarking utilities

· Backward compatibility with base36 encoding

### Enhanced

· Improved timezone parsing with multiple format support

· Better error handling for invalid inputs

· Optimized auto-mode selection logic

· Re-organized repo paths

## [1.0.0] - 2025-11-02

### Added

· Initial release of BitDT Java (BitDateTime)

· Core BitDT class with compact date-time encoding/decoding

· BitDTArray for memory-optimized bulk operations

· ThousandCounter for millisecond encoding

· BitDTExample class with practical usage examples

· Comprehensive test suite (BitDTTest)
