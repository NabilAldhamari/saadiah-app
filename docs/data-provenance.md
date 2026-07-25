# Data provenance

Every bundled data file is generated offline by a script in `tools/`, committed to the repository,
and listed here with its source, licence and checksum. Nothing in this table is generated at build
time — reproducible builds and build speed both depend on that.

| Artefact | Source | Licence | Generator | SHA-256 |
| --- | --- | --- | --- | --- |
| `core/content/.../quran.db` | Tanzil Uthmani text | CC BY 3.0, no modification | `tools/gen-quran-db.kt` | _pending_ |
| `core/content/.../generated/AdhkarCorpus.kt` | [Seen-Arabic/Morning-And-Evening-Adhkar-DB](https://github.com/Seen-Arabic/Morning-And-Evening-Adhkar-DB) — morning and evening adhkār, 34 entries | MIT | `tools/gen-adhkar-db.py` | `81ed9a77188a1f92d7138f4d420beb49acf090cb236abf1f6e6a0e8c4a35b5b8` |
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

## Adhkār

`tools/gen-adhkar-db.py` joins the dataset's `ar.json` and `en.json` on `(type, order)` and
prints the SHA-256 of each input to stderr. The copies the committed corpus was generated
from hash to:

- `ar.json` `9e0dd6a10d26ddc0e2b36b94098c287644996c7f81b707f8ad24f1ce40b8c821`
- `en.json` `eec053c4138bed6f3ec7dc3c0e06e926a935dca24663788e5b97a0bfb5f71896`

The generator refuses to emit an entry without a source reference, because DESIGN.md §6.4
refuses to render one. Replacing this with a larger corpus means writing a new reader that
emits the same `Dhikr` shape; nothing in the app changes, because the schema is the
contract rather than this particular source.

Every entry is tagged `SUNNI`. This compilation's hadith sourcing is Sunni, and tagging it
for both traditions would present Sunni-framed content to a Twelver reader as their own.
A Twelver corpus is tagged separately if and when one is sourced.
