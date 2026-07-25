# Data provenance

Every bundled data file is generated offline by a script in `tools/`, committed to the repository,
and listed here with its source, licence and checksum. Nothing in this table is generated at build
time — reproducible builds and build speed both depend on that.

| Artefact | Source | Licence | Generator | SHA-256 |
| --- | --- | --- | --- | --- |
| `core/content/.../quran.db` | Tanzil Uthmani text | CC BY 3.0, no modification | `tools/gen-quran-db.kt` | _pending_ |
| `core/content/.../adhkar.db` | Hisn al-Muslim corpus | _to record_ | `tools/gen-adhkar-db.kt` | _pending_ |
| `core/model/.../geo.bin` | GeoNames cities5000 | CC BY 4.0 | `tools/gen-geo.kt` | _pending_ |
| `core/calendar/.../UmmAlQuraTable.kt` | ICU4C `icu4c/source/i18n/islamcal.cpp` (`UMALQURA_MONTHLENGTH`), itself derived from the published Umm al-Qura calendar | Unicode Licence v3 | `tools/gen-hijri-table.py` | `90a0565a6dc6a8c28451333d45015ba5b6dea891818d38a4663a030d365a1aa0` |

## Regenerating

Each generator is a standalone script. It downloads nothing: point it at a local copy of
the source data, verify that copy's checksum against the row above, and commit both the output and
the updated checksum in the same commit.

Generators emit the exact formatting spotless produces, so regenerating an unchanged input
reproduces the committed file byte for byte. If a regeneration produces a diff, the input changed.

`tools/gen-hijri-table.py` prints the SHA-256 of the source it read to stderr. The ICU revision the
committed table was generated from hashes to
`a665b4eed397fc890786a27d27e80c754f71620101d79bc6a2b1bfa7d00bb6cb`. It also self-checks the source
before emitting: for all 300 years ICU covers, the accumulated month lengths must land exactly on
the next year's recorded start day, so the two independent ICU arrays corroborate each other.

## Text integrity

`core/content` stores a SHA-256 per āyah. The app verifies the full text once on first launch, off
the main thread, and refuses to render on a mismatch rather than displaying suspect text.
