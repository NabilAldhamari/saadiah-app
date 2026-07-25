#!/usr/bin/env python3
"""Pack Sūrat al-Baqarah and Āl ʿImrān from the Tanzil Uthmani text.

Run manually; never invoked by Gradle. Downloads nothing — point it at a local copy whose
checksum matches docs/data-provenance.md.

    python tools/gen-quran-db.py tanzil-uthmani.txt android/app/src/main/assets/quran.bin

Source: Tanzil Project (https://tanzil.net), Creative Commons Attribution 3.0.

Tanzil's terms are the reason this script does no normalisation whatsoever. The text is
copied byte for byte: "Permission is granted to copy and distribute verbatim copies of this
text, but CHANGING IT IS NOT ALLOWED." Stripping a diacritic to make search easier would
break that, so search normalisation happens at query time against a copy, never here.

The notice must be reproduced in files derived from the text, so it is written into the
binary itself and shown in the app.

FORMAT (little-endian throughout)

    Header, 32 bytes
        0   magic "SDQURAN1"     8 bytes
        8   ayahCount            u32
        12  suraCount            u16
        14  reserved             u16
        16  suraTableOffset      u32
        20  ayahTableOffset      u32
        24  textOffset           u32
        28  noticeOffset         u32

    Sura entry, 8 bytes      number u16, firstAyahIndex u32, ayahCount u16
    Ayah entry, 38 bytes     textOffset u32, textLength u16, sha256 32 bytes
    Notice                   length u16 then UTF-8 bytes

A SHA-256 per āyah, as IMPLEMENTATION-PLAN requires: the app verifies them once off the
main thread and refuses to render on a mismatch rather than showing suspect text.
"""

import hashlib
import struct
import sys

MAGIC = b"SDQURAN1"
HEADER_SIZE = 32
SURAS = (2, 3)
EXPECTED = {2: 286, 3: 200}

NOTICE = (
    "Quran text from the Tanzil Project (tanzil.net), Creative Commons Attribution 3.0. "
    "Copied verbatim; changing it is not permitted."
)


def read_tanzil(path):
    verses = {}
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            parts = line.split("|", 2)
            if len(parts) != 3 or not parts[0].isdigit():
                continue
            verses.setdefault(int(parts[0]), []).append(parts[2])
    return verses


def build(verses):
    ayat = []
    for sura in SURAS:
        found = verses.get(sura, [])
        if len(found) != EXPECTED[sura]:
            raise ValueError(f"sura {sura} has {len(found)} ayat, expected {EXPECTED[sura]}")
        ayat.extend((sura, text) for text in found)

    pool = bytearray()
    entries = bytearray()
    for _, text in ayat:
        encoded = text.encode("utf-8")
        entries += struct.pack("<IH", len(pool), len(encoded))
        entries += hashlib.sha256(encoded).digest()
        pool += encoded

    suras = bytearray()
    start = 0
    for sura in SURAS:
        count = EXPECTED[sura]
        suras += struct.pack("<HIH", sura, start, count)
        start += count

    notice = NOTICE.encode("utf-8")
    sura_at = HEADER_SIZE
    ayah_at = sura_at + len(suras)
    text_at = ayah_at + len(entries)
    notice_at = text_at + len(pool)

    header = MAGIC + struct.pack(
        "<IHHIIII", len(ayat), len(SURAS), 0, sura_at, ayah_at, text_at, notice_at
    )
    if len(header) != HEADER_SIZE:
        raise AssertionError(f"header is {len(header)} bytes, expected {HEADER_SIZE}")
    return header + bytes(suras) + bytes(entries) + bytes(pool) + struct.pack("<H", len(notice)) + notice


def main():
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    source, output = sys.argv[1], sys.argv[2]

    blob = build(read_tanzil(source))
    with open(output, "wb") as handle:
        handle.write(blob)

    print(f"source sha256 {hashlib.sha256(open(source, 'rb').read()).hexdigest()}", file=sys.stderr)
    print(f"ayat          {sum(EXPECTED.values())}", file=sys.stderr)
    print(f"bytes         {len(blob)} ({len(blob) / 1e3:.1f} kB)", file=sys.stderr)
    print(f"sha256        {hashlib.sha256(blob).hexdigest()}", file=sys.stderr)


if __name__ == "__main__":
    main()
