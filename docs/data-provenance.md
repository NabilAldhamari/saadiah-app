# Data provenance

Every bundled data file is generated offline by a script in `tools/`, committed to the repository,
and listed here with its source, licence and checksum. Nothing in this table is generated at build
time — reproducible builds and build speed both depend on that.

| Artefact | Source | Licence | Generator | SHA-256 |
| --- | --- | --- | --- | --- |
| `core/content/.../quran.db` | Tanzil Uthmani text | CC BY 3.0, no modification | `tools/gen-quran-db.kt` | _pending_ |
| `core/content/.../adhkar.db` | Hisn al-Muslim corpus | _to record_ | `tools/gen-adhkar-db.kt` | _pending_ |
| `core/model/.../geo.bin` | GeoNames cities5000 | CC BY 4.0 | `tools/gen-geo.kt` | _pending_ |
| `core/calendar/.../UmmAlQuraTable.kt` | Umm al-Qura published month lengths | _to record_ | `tools/gen-hijri-table.kt` | _pending_ |

## Regenerating

Each generator is a standalone Kotlin script. It downloads nothing: point it at a local copy of
the source data, verify that copy's checksum against the row above, and commit both the output and
the updated checksum in the same commit.

## Text integrity

`core/content` stores a SHA-256 per āyah. The app verifies the full text once on first launch, off
the main thread, and refuses to render on a mismatch rather than displaying suspect text.
