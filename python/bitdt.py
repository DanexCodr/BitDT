import time
import math
from datetime import datetime, timezone, timedelta
from typing import List, Optional, Tuple, Union

# ==========================================
# ThousandCounter
# ==========================================

class ThousandCounter:
    """
    Provides encoding and decoding functionality for millisecond values (0-999)
    into compact 2-character representations using base-25 encoding.
    """
    FIRST_CHAR = "BCDFGHJKLNPQRSTVWXYZbcdfghjklnpqrstvwxyz"
    SECOND_CHAR = "ABCDEFGHIJKLMNOPQRTUVWXYZ"

    @staticmethod
    def encode_milliseconds(millis: int) -> str:
        """
        Encodes a millisecond value (0-999) into a 2-character string.
        """
        if millis < 0 or millis > 999:
            raise ValueError("Milliseconds must be between 0 and 999")

        first_index = millis // 25
        second_index = millis % 25

        if (first_index < 0 or first_index >= len(ThousandCounter.FIRST_CHAR) or 
            second_index < 0 or second_index >= len(ThousandCounter.SECOND_CHAR)):
            raise ValueError(f"Invalid millisecond value: {millis}")

        return ThousandCounter.FIRST_CHAR[first_index] + ThousandCounter.SECOND_CHAR[second_index]

    @staticmethod
    def decode_milliseconds(code: str) -> int:
        """
        Decodes a 2-character string back into a millisecond value.
        """
        if not code or len(code) != 2:
            return -1

        first_index = ThousandCounter.FIRST_CHAR.find(code[0])
        second_index = ThousandCounter.SECOND_CHAR.find(code[1])

        if first_index == -1 or second_index == -1:
            return -1

        return first_index * 25 + second_index


# ==========================================
# BitDT
# ==========================================

class BitDT:
    """
    Represents a compact, space-efficient date-time implementation.
    Supports encoding/decoding of date-time values with optional timezone information.
    """
    
    MONTHS = "ABCDEFGHIJKL"
    DAYS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345"
    HOURS = "BCDEFGHJKLMNOPQRSTUVWXYZ"
    MINUTES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345678"
    YEAR_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789"

    TWO_ZEROS = '.'
    THREE_ZEROS = ':'
    FOUR_ZEROS = ';'
    FIVE_ZEROS = '?'
    SIX_ZEROS = '!'
    SEVEN_ZEROS = '&'

    UTC_PLUS = '+'
    UTC_MINUS = '-'
    TIMEZONE_DIGITS = "0123456789"

    # Bit masks
    YEAR_MASK =   0xFFFFF00000000000
    MONTH_MASK =  0x00000F0000000000
    DAY_MASK =    0x000000F800000000
    HOUR_MASK =   0x00000007C0000000
    MINUTE_MASK = 0x000000003F000000
    SECOND_MASK = 0x0000000000FC0000
    MILLIS_MASK = 0x000000000003FF00
    TYPE_MASK =   0xF000000000000000
    
    # Date type constants
    TYPE_EMPTY = 0
    TYPE_FULL = 1
    TYPE_DATE_ONLY = 2
    TYPE_TIME_ONLY = 3
    TYPE_DATE_HOUR = 4

    EMPTY_VALUE = TYPE_EMPTY

    def __init__(self, packed_value: int, timezone_offset: int, date_type: int):
        self._packed_value = packed_value
        self._timezone_offset = timezone_offset
        self._date_type = date_type

    @classmethod
    def from_primitives(cls, year: int, month: int, day: int, hour: int, 
                        minute: int, second: int, millis: int, timezone: Optional[str] = None) -> 'BitDT':
        """
        Creates a BitDT instance from individual date-time components.
        """
        date_type = cls._determine_date_type(year, month, day, hour, minute, second, millis)
        
        packed = ((year << 44) |
                  (month << 40) |
                  (day << 35) |
                  (hour << 30) |
                  (minute << 24) |
                  (second << 18) |
                  (millis << 8) |
                  date_type)
        
        tz_offset = cls._parse_timezone_offset(timezone)
        return cls(packed, tz_offset, date_type)

    @classmethod
    def _determine_date_type(cls, year, month, day, hour, minute, second, millis):
        if year == 0 and month == 0 and day == 0 and hour == 0 and minute == 0 and second == 0 and millis == 0:
            return cls.TYPE_EMPTY
        elif hour == 0 and minute == 0 and second == 0 and millis == 0:
            return cls.TYPE_DATE_ONLY
        elif year == 0 and month == 0 and day == 0:
            return cls.TYPE_TIME_ONLY
        elif minute == 0 and second == 0 and millis == 0 and hour != 0:
            return cls.TYPE_DATE_HOUR
        else:
            return cls.TYPE_FULL

    @classmethod
    def from_packed_value(cls, packed_value: int, timezone_offset: int, date_type: int) -> 'BitDT':
        return cls(packed_value, timezone_offset, date_type)

    @classmethod
    def create_empty(cls) -> 'BitDT':
        return cls(cls.EMPTY_VALUE, 0, cls.TYPE_EMPTY)

    # Getters
    def get_year(self) -> int:
        if self._date_type == self.TYPE_EMPTY or self._date_type == self.TYPE_TIME_ONLY:
            return -1
        return (self._packed_value & self.YEAR_MASK) >> 44

    def get_month(self) -> int:
        if self._date_type == self.TYPE_EMPTY or self._date_type == self.TYPE_TIME_ONLY:
            return -1
        return (self._packed_value & self.MONTH_MASK) >> 40

    def get_day(self) -> int:
        if self._date_type == self.TYPE_EMPTY or self._date_type == self.TYPE_TIME_ONLY:
            return -1
        return (self._packed_value & self.DAY_MASK) >> 35

    def get_hour(self) -> int:
        if self._date_type == self.TYPE_EMPTY or self._date_type == self.TYPE_DATE_ONLY:
            return -1
        return (self._packed_value & self.HOUR_MASK) >> 30

    def get_minute(self) -> int:
        if self._date_type == self.TYPE_EMPTY or self._date_type == self.TYPE_DATE_ONLY:
            return -1
        if self._date_type == self.TYPE_DATE_HOUR:
            return 0
        return (self._packed_value & self.MINUTE_MASK) >> 24

    def get_second(self) -> int:
        if self._date_type == self.TYPE_EMPTY or self._date_type == self.TYPE_DATE_ONLY:
            return -1
        if self._date_type == self.TYPE_DATE_HOUR:
            return 0
        return (self._packed_value & self.SECOND_MASK) >> 18

    def get_millis(self) -> int:
        if self._date_type == self.TYPE_EMPTY or self._date_type == self.TYPE_DATE_ONLY:
            return -1
        if self._date_type == self.TYPE_DATE_HOUR:
            return 0
        return (self._packed_value & self.MILLIS_MASK) >> 8

    def get_timezone(self) -> Optional[str]:
        return self._format_timezone_offset(self._timezone_offset)

    def get_packed_value(self) -> int:
        return self._packed_value

    def get_timezone_offset(self) -> int:
        return self._timezone_offset

    def get_date_type(self) -> int:
        return self._date_type
    
    def is_empty(self) -> bool:
        return self._date_type == self.TYPE_EMPTY

    # Helpers
    @staticmethod
    def _parse_timezone_offset(timezone: Optional[str]) -> int:
        if not timezone:
            return 0
        if timezone == "+" or timezone == "-":
            return 0
        
        try:
            hours = 0
            minutes = 0
            
            if len(timezone) == 3:
                hours = int(timezone[1:3])
            elif len(timezone) == 5:
                hours = int(timezone[1:3])
                minutes = int(timezone[3:5])
            elif len(timezone) == 6 and timezone[3] == ':':
                hours = int(timezone[1:3])
                minutes = int(timezone[4:6])
            
            total_minutes = hours * 60 + minutes
            if timezone.startswith('-'):
                total_minutes = -total_minutes
                
            return int(total_minutes / 15)
        except ValueError:
            return 0

    @staticmethod
    def _format_timezone_offset(offset: int) -> Optional[str]:
        if offset == 0:
            return None
        
        total_minutes = offset * 15
        hours = abs(total_minutes) // 60
        minutes = abs(total_minutes) % 60
        
        sign = "+" if total_minutes >= 0 else "-"
        
        if minutes == 0:
            return f"{sign}{hours:02d}"
        else:
            return f"{sign}{hours:02d}{minutes:02d}"

    @staticmethod
    def encode_year(year: int) -> str:
        if year < 0 or year >= 226981:
            raise ValueError("Year must be between 0 and 226,980")
        c1 = BitDT.YEAR_CHARS[year // (61 * 61)]
        c2 = BitDT.YEAR_CHARS[(year // 61) % 61]
        c3 = BitDT.YEAR_CHARS[year % 61]
        return f"{c1}{c2}{c3}"

    @staticmethod
    def decode_year(year_code: str) -> int:
        if not year_code or len(year_code) != 3:
            return -1
        idx1 = BitDT.YEAR_CHARS.find(year_code[0])
        idx2 = BitDT.YEAR_CHARS.find(year_code[1])
        idx3 = BitDT.YEAR_CHARS.find(year_code[2])
        if idx1 == -1 or idx2 == -1 or idx3 == -1:
            return -1
        return idx1 * 61 * 61 + idx2 * 61 + idx3

    @staticmethod
    def to_absolute_year(relative_year: int) -> int:
        return relative_year - 50000

    @staticmethod
    def from_absolute_year(absolute_year: int) -> int:
        return absolute_year + 50000

    @classmethod
    def _compress_zeros(cls, fields: str) -> str:
        compressed = []
        zero_count = 0
        
        for c in fields:
            if c == '0':
                zero_count += 1
            else:
                if zero_count > 0:
                    compressed.append(cls._encode_zero_run(zero_count))
                    zero_count = 0
                compressed.append(c)
        
        if zero_count > 0:
            compressed.append(cls._encode_zero_run(zero_count))
            
        return "".join(compressed)

    @classmethod
    def _encode_zero_run(cls, zero_count: int) -> str:
        if zero_count == 1: return "0"
        if zero_count == 2: return "."
        if zero_count == 3: return ":"
        if zero_count == 4: return ";"
        if zero_count == 5: return "?"
        if zero_count == 6: return "!"
        if zero_count == 7: return "&"
        
        return "&" + ("0" * (zero_count - 7))

    @classmethod
    def _expand_zeros(cls, compressed: str) -> str:
        expanded = []
        for c in compressed:
            if c == '0': expanded.append('0')
            elif c == '.': expanded.append("00")
            elif c == ':': expanded.append("000")
            elif c == ';': expanded.append("0000")
            elif c == '?': expanded.append("00000")
            elif c == '!': expanded.append("000000")
            elif c == '&': expanded.append("0000000")
            else: expanded.append(c)
        return "".join(expanded)

    @classmethod
    def _parse_timezone(cls, bit_dt: str) -> Optional[str]:
        if not bit_dt or len(bit_dt) < 2:
            return None
        
        last_char = bit_dt[-1]
        if last_char in [cls.UTC_PLUS, cls.UTC_MINUS]:
            return str(last_char)
        
        for i in range(len(bit_dt) - 1, -1, -1):
            c = bit_dt[i]
            if c in [cls.UTC_PLUS, cls.UTC_MINUS]:
                timezone_part = bit_dt[i:]
                if cls._is_valid_timezone(timezone_part):
                    return timezone_part
        return None

    @classmethod
    def _is_valid_timezone(cls, timezone: str) -> bool:
        if not timezone or len(timezone) < 2:
            return False
        
        sign = timezone[0]
        if sign not in [cls.UTC_PLUS, cls.UTC_MINUS]:
            return False
        
        digits = timezone[1:]
        if not digits:
            return True
            
        for c in digits:
            if c not in cls.TIMEZONE_DIGITS:
                return False
                
        return len(digits) == 2 or len(digits) == 4

    @classmethod
    def _extract_datetime_part(cls, bit_dt: str) -> Optional[str]:
        if bit_dt is None:
            return None
        timezone = cls._parse_timezone(bit_dt)
        if timezone:
            return bit_dt[:len(bit_dt) - len(timezone)]
        return bit_dt

    def encode(self) -> str:
        if self.is_empty():
            return "&"
        
        year = self.get_year()
        month = self.get_month()
        day = self.get_day()
        hour = self.get_hour()
        minute = self.get_minute()
        second = self.get_second()
        millis = self.get_millis()
        timezone = self.get_timezone()

        if self._date_type == self.TYPE_DATE_ONLY:
            hour = minute = second = millis = -1
        elif self._date_type == self.TYPE_TIME_ONLY:
            year = month = day = -1
        elif self._date_type == self.TYPE_DATE_HOUR:
            minute = second = millis = -1

        return self.encode_date_time(
            None if year == -1 else year,
            None if month == -1 else month,
            None if day == -1 else day,
            None if hour == -1 else hour,
            None if minute == -1 else minute,
            None if second == -1 else second,
            None if millis == -1 else millis,
            timezone
        )

    @classmethod
    def encode_date_time(cls, year: Optional[int], month: Optional[int], day: Optional[int], 
                         hour: Optional[int], minute: Optional[int], second: Optional[int], 
                         millis: Optional[int], timezone: Optional[str] = None) -> str:
        try:
            year_code = "0"
            if year is not None and year >= 0:
                year_code = cls.encode_year(year)
            
            month_char = '0'
            if month is not None and month >= 0:
                if month < 0 or month >= len(cls.MONTHS):
                    raise ValueError(f"Month must be between 0 and {len(cls.MONTHS) - 1}")
                month_char = cls.MONTHS[month]
            
            day_char = '0'
            if day is not None and day >= 1:
                if day < 1 or day > len(cls.DAYS):
                    raise ValueError(f"Day must be between 1 and {len(cls.DAYS)}")
                day_char = cls.DAYS[day - 1]
            
            hour_char = '0'
            if hour is not None and hour >= 0:
                if hour < 0 or hour >= len(cls.HOURS):
                    raise ValueError(f"Hour must be between 0 and {len(cls.HOURS) - 1}")
                hour_char = cls.HOURS[hour]
            
            minute_char = '0'
            if minute is not None and minute >= 0:
                if minute < 0 or minute >= len(cls.MINUTES):
                    raise ValueError(f"Minute must be between 0 and {len(cls.MINUTES) - 1}")
                minute_char = cls.MINUTES[minute]
            
            second_char = '0'
            if second is not None and second >= 0:
                if second < 0 or second >= len(cls.MINUTES):
                    raise ValueError(f"Second must be between 0 and {len(cls.MINUTES) - 1}")
                second_char = cls.MINUTES[second]
            
            millis_code = "0"
            if millis is not None and millis >= 0:
                if millis == 0:
                    millis_code = "0"
                else:
                    millis_code = ThousandCounter.encode_milliseconds(millis)
            
            uncompressed = f"{year_code}{month_char}{day_char}{hour_char}{minute_char}{second_char}{millis_code}"
            date_time_part = cls._compress_zeros(uncompressed)
            
            if timezone:
                return date_time_part + timezone
            
            return date_time_part
            
        except Exception:
            return "&"

    @classmethod
    def decode(cls, bit_dt: Optional[str]) -> 'BitDT':
        if bit_dt is None:
            return cls.create_empty()
        
        timezone_str = cls._parse_timezone(bit_dt)
        date_time_part = cls._extract_datetime_part(bit_dt)
        
        if not date_time_part:
            return cls.create_empty()
        
        if date_time_part and date_time_part[0] != '0' and len(date_time_part) < 3:
            return cls.create_empty()
            
        expanded = cls._expand_zeros(date_time_part)
        
        if len(expanded) < 7:
            return cls.create_empty()
        
        if not all(cls._is_valid_encoded_char(c) for c in expanded):
            return cls.create_empty()
        
        year, month, day, hour, minute, second, millis = -1, -1, -1, -1, -1, -1, -1
        pos = 0
        
        if pos < len(expanded):
            if expanded[pos] == '0':
                year = -1
                pos += 1
            elif pos + 2 < len(expanded):
                year_code = expanded[pos:pos+3]
                year = cls.decode_year(year_code)
                if year == -1: return cls.create_empty()
                pos += 3
            else:
                return cls.create_empty()
        
        if pos < len(expanded):
            if expanded[pos] == '0':
                month = -1
            else:
                month = cls.MONTHS.find(expanded[pos])
                if month == -1: return cls.create_empty()
            pos += 1
            
        if pos < len(expanded):
            if expanded[pos] == '0':
                day = -1
            else:
                day_idx = cls.DAYS.find(expanded[pos])
                day = day_idx + 1
                if day == 0: return cls.create_empty()
            pos += 1
            
        if pos < len(expanded):
            if expanded[pos] == '0':
                hour = -1
            else:
                hour = cls.HOURS.find(expanded[pos])
                if hour == -1: return cls.create_empty()
            pos += 1
            
        if pos < len(expanded):
            if expanded[pos] == '0':
                minute = -1
            else:
                minute = cls.MINUTES.find(expanded[pos])
                if minute == -1: return cls.create_empty()
            pos += 1
            
        if pos < len(expanded):
            if expanded[pos] == '0':
                second = -1
            else:
                second = cls.MINUTES.find(expanded[pos])
                if second == -1: return cls.create_empty()
            pos += 1
            
        if pos < len(expanded):
            if expanded[pos] == '0':
                millis = 0
                pos += 1
            elif pos + 1 < len(expanded):
                millis_code = expanded[pos:pos+2]
                millis = ThousandCounter.decode_milliseconds(millis_code)
                if millis == -1: return cls.create_empty()
                pos += 2
            else:
                return cls.create_empty()
        
        if all(x == -1 for x in [year, month, day, hour, minute, second, millis]):
            return cls.create_empty()
            
        final_year = year if year != -1 else 0
        final_month = month if month != -1 else 0
        final_day = day if day != -1 else 0
        final_hour = hour if hour != -1 else 0
        final_minute = minute if minute != -1 else 0
        final_second = second if second != -1 else 0
        final_millis = millis if millis != -1 else 0
        
        return cls.from_primitives(final_year, final_month, final_day, 
                                 final_hour, final_minute, final_second, 
                                 final_millis, timezone_str)

    @staticmethod
    def _is_valid_encoded_char(c: str) -> bool:
        return (c in BitDT.YEAR_CHARS or 
                c in BitDT.MONTHS or
                c in BitDT.DAYS or
                c in BitDT.HOURS or
                c in BitDT.MINUTES or
                c in "0.:;?!&")

    def get_numerical_value(self) -> int:
        return self._packed_value

    def compare_to(self, other: 'BitDT') -> int:
        if self._packed_value < other._packed_value:
            return -1
        elif self._packed_value > other._packed_value:
            return 1
        else:
            return 0

    def before(self, other: 'BitDT') -> bool:
        return self._packed_value < other._packed_value

    def after(self, other: 'BitDT') -> bool:
        return self._packed_value > other._packed_value

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, BitDT):
            return False
        return (self._packed_value == other._packed_value and 
                self._timezone_offset == other._timezone_offset)

    def __hash__(self) -> int:
        return hash((self._packed_value, self._timezone_offset))

    def __str__(self) -> str:
        return self.encode()

    @staticmethod
    def sort_by_numerical_value(dates: List['BitDT']) -> List['BitDT']:
        return sorted(dates, key=lambda dt: dt.get_numerical_value())

    @staticmethod
    def from_numerical_array(numerical_values: List[int]) -> List['BitDT']:
        return [BitDT.from_packed_value(val, 0, BitDT.TYPE_FULL) for val in numerical_values]

    @staticmethod
    def to_numerical_array(dates: List['BitDT']) -> List[int]:
        return [dt.get_numerical_value() for dt in dates]

    @staticmethod
    def from_numerical_value(numerical_value: int) -> 'BitDT':
        return BitDT.from_packed_value(numerical_value, 0, BitDT.TYPE_FULL)


# ==========================================
# BitDTEpoch
# ==========================================

class BitDTEpoch:
    """
    BitDTEpoch provides flexible epoch-based utilities for the BitDT encoding system.
    Supports multiple encoding bases (2-36) for maximum flexibility, plus smart auto-detection.
    """
    MODE_AUTO = 0
    MODE_BASE36 = 36
    MODE_FULL_BITDT = -1
    
    BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    @staticmethod
    def to_bit_dt(epoch_millis: int, timezone: Optional[str] = None, base: int = MODE_AUTO) -> str:
        """
        Epoch-to-string conversion with timezone and base selection.
        """
        try:
            if base == BitDTEpoch.MODE_FULL_BITDT:
                return BitDTEpoch._to_full_bit_dt(epoch_millis, timezone)
            
            if base == BitDTEpoch.MODE_AUTO:
                return BitDTEpoch._to_smart_bit_dt(epoch_millis, timezone)
            
            if 2 <= base <= 36:
                return BitDTEpoch._encode_base(epoch_millis, base)
            
            return BitDTEpoch._to_smart_bit_dt(epoch_millis, timezone)
            
        except Exception:
            return BitDT.create_empty().encode()

    @staticmethod
    def from_bit_dt(bitdt: str, base: Optional[int] = None) -> int:
        """
        Smart decoding that automatically detects the encoding format, or uses specific base.
        Returns epoch time in milliseconds, or -1 if decoding fails.
        """
        if not bitdt:
            return -1
        
        try:
            if base is not None and 2 <= base <= 36:
                return int(bitdt, base)
            
            # Try full BitDT first, then base detection
            # This avoids misinterpreting full BitDT encodings as base numbers
            full_result = BitDTEpoch._from_full_bit_dt(bitdt)
            if full_result != -1:
                return full_result
        
            # If full BitDT decoding failed, try base detection
            if BitDTEpoch._is_base_encoded(bitdt):
                return BitDTEpoch._decode_base_auto(bitdt)
        
            return -1           
        except Exception:
            return -1

    @staticmethod
    def _to_smart_bit_dt(epoch_millis: int, timezone: Optional[str]) -> str:
        if timezone is None or timezone == "UTC" or timezone == "+00" or timezone == "Z" or timezone == "+0000":
            return BitDTEpoch._encode_base(epoch_millis, BitDTEpoch.MODE_BASE36)
        else:
            return BitDTEpoch._to_full_bit_dt(epoch_millis, timezone)

    @staticmethod
    def _to_full_bit_dt(epoch_millis: int, timezone: Optional[str]) -> str:
        dt_obj = datetime.fromtimestamp(epoch_millis / 1000.0, tz=timezone_module.utc)
        
        offset = timezone_module.utc
        if timezone and timezone != "UTC":
            offset = BitDTEpoch._parse_zone_offset_to_tz(timezone)
            dt_obj = dt_obj.astimezone(offset)
        
        return BitDT.from_primitives(
            BitDT.from_absolute_year(dt_obj.year),
            dt_obj.month - 1,
            dt_obj.day,
            dt_obj.hour,
            dt_obj.minute,
            dt_obj.second,
            int(dt_obj.microsecond / 1000),
            timezone
        ).encode()

    @staticmethod
    def _from_full_bit_dt(bitdt: str) -> int:
        try:
            dt = BitDT.decode(bitdt)
            if dt.is_empty():
                return -1

            # Get the relative year from BitDT
            year = dt.get_year()
            if year == -1:
                # Time-only format, use current date for year
                current_year = datetime.now(timezone.utc).year
                year = BitDT.from_absolute_year(current_year)

            # Convert relative year to absolute year
            abs_year = BitDT.to_absolute_year(year)

            # Determine timezone offset
            tz_offset = timezone.utc
            tz_str = dt.get_timezone()
            if tz_str:
                tz_offset = BitDTEpoch._parse_zone_offset_to_tz(tz_str)

            # Handle missing components with proper defaults
            month = dt.get_month() + 1 if dt.get_month() != -1 else 1
            day = dt.get_day() if dt.get_day() != -1 else 1
            hour = dt.get_hour() if dt.get_hour() != -1 else 0
            minute = dt.get_minute() if dt.get_minute() != -1 else 0
            second = dt.get_second() if dt.get_second() != -1 else 0
            millisecond = dt.get_millis() if dt.get_millis() != -1 else 0

            try:
                # Create datetime object
                zdt = datetime(
                abs_year, month, day, hour, minute, second, 
                    millisecond * 1000,  # Convert milliseconds to microseconds
                    tzinfo=tz_offset
                )

                # Convert to epoch milliseconds
                epoch_ms = int(zdt.timestamp() * 1000)
                return epoch_ms

            except ValueError as e:
                # Handle invalid dates (like February 30th, etc.)
                import calendar
                try:
                    # Try to create a valid date by adjusting the day
                    _, last_day = calendar.monthrange(abs_year, month)
                    adjusted_day = min(day, last_day)

                    zdt = datetime(
                        abs_year, month, adjusted_day, hour, minute, second,
                        millisecond * 1000, tzinfo=tz_offset
                    )
                    return int(zdt.timestamp() * 1000)
                except Exception:
                    # If still failing, use a safe fallback
                    zdt = datetime(abs_year, 1, 1, hour, minute, second, millisecond * 1000, tzinfo=tz_offset)
                    return int(zdt.timestamp() * 1000)

        except Exception as e:
            return -1

    @staticmethod
    def _decode_base_auto(encoded: str) -> int:
        likely_base = BitDTEpoch._guess_most_likely_base(encoded)
        
        try:
            return int(encoded, likely_base)
        except ValueError:
            pass
        
        for base in [36, 32, 16, 10]:
            if base == likely_base: continue
            try:
                return int(encoded, base)
            except ValueError:
                continue
                
        return BitDTEpoch._from_full_bit_dt(encoded)

    @staticmethod
    def _guess_most_likely_base(encoded: str) -> int:
        has_base36_only = False
        has_base32_only = False
        has_hex_only = True
        
        encoded = encoded.upper()
        
        for c in encoded:
            if 'G' <= c <= 'Z':
                if 'W' <= c <= 'Z':
                    has_base36_only = True
                else:
                    has_base32_only = True
                has_hex_only = False
            elif 'A' <= c <= 'F':
                pass
            elif '0' <= c <= '9':
                pass
            else:
                has_hex_only = False
        
        if has_hex_only: return 16
        if has_base36_only: return 36
        if has_base32_only: return 32
        return 36

    @staticmethod
    def _is_base_encoded(s: str) -> bool:
        if len(s) < 6 or len(s) > 12:
            return False
        s = s.upper()
        for c in s:
            if c not in BitDTEpoch.BASE36_CHARS:
                return False
        return True

    @staticmethod
    def _encode_base(num: int, base: int) -> str:
        if num == 0:
            return "0"
        
        sign = ""
        if num < 0:
            sign = "-"
            num = -num
        
        chars = BitDTEpoch.BASE36_CHARS
        result = []
        while num > 0:
            result.append(chars[num % base])
            num //= base
        
        return sign + "".join(reversed(result))

    @staticmethod
    def _parse_zone_offset_to_tz(tz_str: str) -> timezone:
        if not tz_str or tz_str == "UTC":
            return timezone.utc
        
        try:
            if tz_str == "+" or tz_str == "-":
                return timezone.utc
            
            sign_char = tz_str[0]
            if sign_char not in ['+', '-']:
                return timezone.utc
                
            sign = 1 if sign_char == '+' else -1
            content = tz_str[1:]
            
            hours = 0
            minutes = 0
            
            if len(content) == 2:
                hours = int(content)
            elif len(content) == 4:
                hours = int(content[0:2])
                minutes = int(content[2:4])
            elif len(content) == 5 and content[2] == ':':
                hours = int(content[0:2])
                minutes = int(content[3:5])
            else:
                return timezone.utc
                
            total_seconds = sign * (hours * 3600 + minutes * 60)
            return timezone(timedelta(seconds=total_seconds))
            
        except Exception:
            return timezone.utc

    @staticmethod
    def now(timezone: Optional[str] = None, base: int = MODE_AUTO) -> str:
        current_time = int(time.time() * 1000)
        return BitDTEpoch.to_bit_dt(current_time, timezone, base)


# ==========================================
# BitDTArray
# ==========================================

class BitDTArray:
    """
    The BitDTArray class provides an efficient array implementation
    for storing multiple BitDT instances. Using parallel lists to simulate
    primitive arrays for structure consistency.
    """

    def __init__(self, size_or_packed: Union[int, List[int]], 
                 timezone_offsets: Optional[List[int]] = None, 
                 date_types: Optional[List[int]] = None):
        
        if isinstance(size_or_packed, int):
            size = size_or_packed
            self.packed_values = [0] * size
            self.timezone_offsets = [0] * size
            self.date_types = [0] * size
        else:
            if timezone_offsets is None or date_types is None:
                raise ValueError("Must provide all arrays")
            if len(size_or_packed) != len(timezone_offsets) or len(size_or_packed) != len(date_types):
                raise ValueError("Arrays must have same length")
            
            self.packed_values = list(size_or_packed)
            self.timezone_offsets = list(timezone_offsets)
            self.date_types = list(date_types)

    def with_value(self, index: int, dt: BitDT) -> 'BitDTArray':
        if index < 0 or index >= len(self.packed_values):
            raise IndexError(f"Index: {index}, Size: {len(self.packed_values)}")
        
        new_packed = list(self.packed_values)
        new_tz = list(self.timezone_offsets)
        new_types = list(self.date_types)
        
        new_packed[index] = dt.get_packed_value()
        new_tz[index] = dt.get_timezone_offset()
        new_types[index] = dt.get_date_type()
        
        return BitDTArray(new_packed, new_tz, new_types)

    def get(self, index: int) -> BitDT:
        if index < 0 or index >= len(self.packed_values):
            raise IndexError(f"Index: {index}, Size: {len(self.packed_values)}")
        return BitDT.from_packed_value(
            self.packed_values[index], 
            self.timezone_offsets[index], 
            self.date_types[index]
        )

    def size(self) -> int:
        return len(self.packed_values)

    def sorted(self) -> 'BitDTArray':
        indices = list(range(len(self.packed_values)))
        indices.sort(key=lambda i: self.packed_values[i])
        
        new_packed = [self.packed_values[i] for i in indices]
        new_tz = [self.timezone_offsets[i] for i in indices]
        new_types = [self.date_types[i] for i in indices]
        
        return BitDTArray(new_packed, new_tz, new_types)

    def sorted_copy(self) -> 'BitDTArray':
        return self.sorted()

    @staticmethod
    def from_list(dates: List[BitDT]) -> 'BitDTArray':
        arr = BitDTArray(len(dates))
        for i, dt in enumerate(dates):
            arr.packed_values[i] = dt.get_packed_value()
            arr.timezone_offsets[i] = dt.get_timezone_offset()
            arr.date_types[i] = dt.get_date_type()
        return arr

    def to_list(self) -> List[BitDT]:
        return [self.get(i) for i in range(self.size())]

    def filter_by_type(self, date_type: int) -> 'BitDTArray':
        filtered_packed = []
        filtered_tz = []
        filtered_types = []
        
        for i in range(self.size()):
            if self.date_types[i] == date_type:
                filtered_packed.append(self.packed_values[i])
                filtered_tz.append(self.timezone_offsets[i])
                filtered_types.append(self.date_types[i])
                
        return BitDTArray(filtered_packed, filtered_tz, filtered_types)

    def count_by_type(self, date_type: int) -> int:
        return self.date_types.count(date_type)

    def get_full_dates(self) -> 'BitDTArray':
        return self.filter_by_type(BitDT.TYPE_FULL)

    def get_date_only(self) -> 'BitDTArray':
        return self.filter_by_type(BitDT.TYPE_DATE_ONLY)

    def get_time_only(self) -> 'BitDTArray':
        return self.filter_by_type(BitDT.TYPE_TIME_ONLY)

    def get_empty(self) -> 'BitDTArray':
        return self.filter_by_type(BitDT.TYPE_EMPTY)

    def get_packed_values(self) -> List[int]:
        return list(self.packed_values)

    def get_timezone_offsets(self) -> List[int]:
        return list(self.timezone_offsets)

    def get_date_types(self) -> List[int]:
        return list(self.date_types)

    def slice(self, start: int, end: int) -> 'BitDTArray':
        if start < 0 or end > self.size() or start > end:
            raise ValueError("Invalid slice range")
        
        return BitDTArray(
            self.packed_values[start:end],
            self.timezone_offsets[start:end],
            self.date_types[start:end]
        )

    def concat(self, other: 'BitDTArray') -> 'BitDTArray':
        return BitDTArray(
            self.packed_values + other.packed_values,
            self.timezone_offsets + other.timezone_offsets,
            self.date_types + other.date_types
        )

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, BitDTArray):
            return False
        return (self.packed_values == other.packed_values and
                self.timezone_offsets == other.timezone_offsets and
                self.date_types == other.date_types)

    def __hash__(self) -> int:
        return hash((tuple(self.packed_values), tuple(self.timezone_offsets), tuple(self.date_types)))

# Alias to resolve forward reference in timezone parsing
from datetime import timezone as timezone_module
