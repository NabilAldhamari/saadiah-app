#!/usr/bin/env python3
"""Generate the committed city database from a GeoNames dump.

Run manually; never invoked by Gradle. Downloads nothing — point it at local copies
whose checksums match docs/data-provenance.md.

    python tools/gen-city-db.py cities1000.txt admin1CodesASCII.txt \
        android/app/src/main/assets/cities.bin

Source: GeoNames (https://www.geonames.org), CC BY 4.0. Attribution is required by the
licence and lives in docs/data-provenance.md and the app's About screen.

Records are sorted by the ASCII name lowercased. That ordering is the contract between
this script and CityIndex.kt: the reader binary-searches on the same key, so if the two
ever disagree the search silently misses. `asciiname` is used rather than a folded
Unicode name precisely because ASCII lowercasing cannot drift between Python and Kotlin.
CityIndexTest asserts the whole file is ordered, so a drift fails the build.

Identity is the GeoNames id, not the record index. A record index would renumber every
city whenever the dump is refreshed, and a stored setting would silently come back as a
different place.

FORMAT (little-endian throughout)

    Header, 48 bytes
        0   magic "SDCITY01"          8 bytes
        8   cityCount                 u32
        12  countryCount              u16
        14  timeZoneCount             u16
        16  admin1Count               u16
        18  reserved                  u16
        20  recordsOffset             u32
        24  namePoolOffset            u32
        28  countryTableOffset        u32
        32  timeZoneTableOffset       u32
        36  admin1TableOffset         u32
        40  fileSize                  u32
        44  reserved                  u32

    Record, 23 bytes, sorted by lower(asciiname)
        0   nameOffset                u32   into the name pool
        4   geonameId                 u32   the stable public identity
        8   latitude                  i24   degrees * 1e4
        11  longitude                 i24   degrees * 1e4
        14  admin1                    u16   index into the admin1 table
        16  timeZone                  u16   index into the time zone table
        18  country                   u8    index into the country table
        19  population                u32   ranks matches; London GB must beat London CA

    Name blob, at nameOffset
        flags                         u8    bit0 display name, bit1 arabic name
        asciiLength                   u8    then that many bytes
        displayLength + bytes               present when bit0
        arabicLength + bytes                present when bit1

    String table
        count                         u16
        then count entries of u8 length followed by UTF-8 bytes

Latitude and longitude are stored at 1e-4 degrees, about 11 m. Prayer times move roughly
four minutes per degree of longitude, so that rounding is worth under a hundredth of a
second — far below the one-minute resolution anything is displayed at.
"""

import hashlib
import re
import struct
import sys

MAGIC = b"SDCITY01"
HEADER_SIZE = 48
RECORD_SIZE = 23
COORDINATE_SCALE = 1e4

ARABIC = re.compile(r"[؀-ۿݐ-ݿﭐ-﷿ﹰ-﻿]")

HAS_DISPLAY_NAME = 1
HAS_ARABIC_NAME = 2

# GeoNames columns, tab separated.
GEONAME_ID, NAME, ASCII_NAME, ALTERNATE_NAMES = 0, 1, 2, 3
LATITUDE, LONGITUDE = 4, 5
COUNTRY, ADMIN1 = 8, 10
POPULATION = 14
TIME_ZONE = 17
COLUMN_COUNT = 19


def read_admin1_names(path):
    """Maps "GB.ENG" to "England"; the dump gives cities only the bare code."""
    names = {}
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            fields = line.rstrip("\n").split("\t")
            if len(fields) >= 2:
                names[fields[0]] = fields[1]
    return names


def first_arabic(alternate_names):
    for candidate in alternate_names.split(","):
        if ARABIC.search(candidate):
            return candidate
    return None


def read_cities(path, admin1_names):
    cities = []
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            fields = line.rstrip("\n").split("\t")
            if len(fields) < COLUMN_COUNT:
                continue
            ascii_name = fields[ASCII_NAME] or fields[NAME]
            if not ascii_name:
                continue
            admin1_key = f"{fields[COUNTRY]}.{fields[ADMIN1]}"
            cities.append(
                {
                    "id": int(fields[GEONAME_ID]),
                    "ascii": ascii_name,
                    "display": fields[NAME] if fields[NAME] != ascii_name else None,
                    "arabic": first_arabic(fields[ALTERNATE_NAMES]),
                    "latitude": float(fields[LATITUDE]),
                    "longitude": float(fields[LONGITUDE]),
                    "country": fields[COUNTRY],
                    "admin1": admin1_names.get(admin1_key, ""),
                    "timezone": fields[TIME_ZONE],
                    "population": int(fields[POPULATION]) if fields[POPULATION].isdigit() else 0,
                }
            )
    cities.sort(key=lambda city: (city["ascii"].lower(), city["id"]))
    return cities


def build_string_table(values):
    ordered = sorted(values)
    index = {value: position for position, value in enumerate(ordered)}
    body = bytearray(struct.pack("<H", len(ordered)))
    for value in ordered:
        encoded = value.encode("utf-8")
        if len(encoded) > 255:
            raise ValueError(f"string table entry too long: {value}")
        body += struct.pack("<B", len(encoded)) + encoded
    return bytes(body), index


def build_name_pool(cities):
    pool = bytearray()
    offsets = []
    for city in cities:
        offsets.append(len(pool))
        ascii_bytes = city["ascii"].encode("utf-8")[:255]
        flags = 0
        extra = b""
        if city["display"]:
            display = city["display"].encode("utf-8")[:255]
            flags |= HAS_DISPLAY_NAME
            extra += struct.pack("<B", len(display)) + display
        if city["arabic"]:
            arabic = city["arabic"].encode("utf-8")[:255]
            flags |= HAS_ARABIC_NAME
            extra += struct.pack("<B", len(arabic)) + arabic
        pool += struct.pack("<BB", flags, len(ascii_bytes)) + ascii_bytes + extra
    return bytes(pool), offsets


def pack_signed_24(value):
    if not -(1 << 23) <= value < (1 << 23):
        raise ValueError(f"coordinate out of range for 24 bits: {value}")
    return struct.pack("<i", value & 0xFFFFFF)[:3]


def build(cities):
    countries, country_index = build_string_table({city["country"] for city in cities})
    zones, zone_index = build_string_table({city["timezone"] for city in cities})
    admin1s, admin1_index = build_string_table({city["admin1"] for city in cities})
    pool, offsets = build_name_pool(cities)

    records = bytearray()
    for city, name_offset in zip(cities, offsets):
        records += struct.pack("<II", name_offset, city["id"])
        records += pack_signed_24(round(city["latitude"] * COORDINATE_SCALE))
        records += pack_signed_24(round(city["longitude"] * COORDINATE_SCALE))
        records += struct.pack(
            "<HHBI",
            admin1_index[city["admin1"]],
            zone_index[city["timezone"]],
            country_index[city["country"]],
            min(city["population"], 0xFFFFFFFF),
        )

    records_offset = HEADER_SIZE
    pool_offset = records_offset + len(records)
    country_offset = pool_offset + len(pool)
    zone_offset = country_offset + len(countries)
    admin1_offset = zone_offset + len(zones)
    file_size = admin1_offset + len(admin1s)

    header = MAGIC + struct.pack(
        "<IHHHHIIIIIII",
        len(cities),
        len(country_index),
        len(zone_index),
        len(admin1_index),
        0,
        records_offset,
        pool_offset,
        country_offset,
        zone_offset,
        admin1_offset,
        file_size,
        0,
    )
    if len(header) != HEADER_SIZE:
        raise AssertionError(f"header is {len(header)} bytes, expected {HEADER_SIZE}")
    return header + bytes(records) + pool + countries + zones + admin1s


def main():
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    cities_path, admin1_path, output_path = sys.argv[1:4]

    cities = read_cities(cities_path, read_admin1_names(admin1_path))
    if not cities:
        sys.exit("no cities were read; is this a GeoNames cities dump?")
    blob = build(cities)

    with open(output_path, "wb") as handle:
        handle.write(blob)

    arabic = sum(1 for city in cities if city["arabic"])
    print(f"cities      {len(cities)}", file=sys.stderr)
    print(f"with arabic {arabic}", file=sys.stderr)
    print(f"bytes       {len(blob)} ({len(blob) / 1e6:.2f} MB)", file=sys.stderr)
    print(f"sha256      {hashlib.sha256(blob).hexdigest()}", file=sys.stderr)


if __name__ == "__main__":
    main()
