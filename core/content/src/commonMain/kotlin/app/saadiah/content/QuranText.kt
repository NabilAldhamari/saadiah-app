package app.saadiah.content

private const val HEADER_SIZE = 32
private const val SURA_ENTRY_SIZE = 8
private const val AYAH_ENTRY_SIZE = 38
private const val DIGEST_SIZE = 32

// Field offsets within a record, named so the arithmetic below reads as a layout.
private const val SURA_FIRST_AYAH = 2
private const val SURA_AYAH_COUNT = 6
private const val AYAH_LENGTH = 4
private const val AYAH_DIGEST = 6
private const val LENGTH_PREFIX = 2

private const val AYAH_COUNT_AT = 8
private const val SURA_COUNT_AT = 12
private const val SURA_TABLE_AT = 16
private const val AYAH_TABLE_AT = 20
private const val TEXT_AT = 24
private const val NOTICE_AT = 28

private const val BYTE_MASK = 0xFF
private const val BITS_PER_BYTE = 8

private val MAGIC = "SDQURAN1".encodeToByteArray()

data class Ayah(
    val sura: Int,
    val number: Int,
    val text: String,
)

/**
 * The opening as Tanzil writes it, exactly. Compared against, never displayed from here —
 * what a reader sees is the slice of the bundled text, so the verbatim requirement holds
 * even for this line.
 */
const val BASMALAH = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

private const val AL_FATIHAH = 1

/**
 * A sura ready to read: its opening line, then its āyāt.
 *
 * Tanzil prefixes the basmalah to the first āyah of every sura that opens with one, so
 * rendering the stored text as-is prints "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ الٓمٓ ﴿1﴾" — the opening
 * counted as part of āyah 1, which for al-Baqarah it is not. Splitting it is a numbering
 * correction, not decoration.
 *
 * In al-Fātiḥah the basmalah *is* āyah 1, so it is never split off there.
 */
data class SuraReading(
    val basmalah: String?,
    val ayat: List<Ayah>,
)

fun List<Ayah>.asReading(): SuraReading {
    val first = firstOrNull() ?: return SuraReading(basmalah = null, ayat = emptyList())
    if (first.sura == AL_FATIHAH || !first.text.startsWith(BASMALAH)) {
        return SuraReading(basmalah = null, ayat = this)
    }
    val opened = first.copy(text = first.text.removePrefix(BASMALAH).trimStart())
    return SuraReading(basmalah = BASMALAH, ayat = listOf(opened) + drop(n = 1))
}

/**
 * Reads the Quran text packed by tools/gen-quran-db.py from the Tanzil Uthmani edition.
 *
 * The text is never normalised here. Tanzil's licence permits verbatim copies only, so a
 * reader that stripped diacritics to make matching easier would be redistributing something
 * it was not allowed to change.
 */
class QuranText(
    private val bytes: ByteArray,
) {
    init {
        require(bytes.size >= HEADER_SIZE && MAGIC.indices.all { bytes[it] == MAGIC[it] }) {
            "not a Saadiah Quran database"
        }
    }

    val ayahCount: Int = readInt(AYAH_COUNT_AT)

    /** Reproduced from the binary rather than written here, because the licence requires it. */
    val notice: String
        get() {
            val at = readInt(NOTICE_AT)
            return bytes.decodeToString(at + LENGTH_PREFIX, at + LENGTH_PREFIX + readShort(at))
        }

    val suras: List<Int>
        get() =
            (0 until readShort(SURA_COUNT_AT)).map { readShort(readInt(SURA_TABLE_AT) + it * SURA_ENTRY_SIZE) }

    fun sura(number: Int): List<Ayah> {
        val table = readInt(SURA_TABLE_AT)
        for (index in 0 until readShort(SURA_COUNT_AT)) {
            val at = table + index * SURA_ENTRY_SIZE
            if (readShort(at) != number) continue
            val first = readInt(at + SURA_FIRST_AYAH)
            val count = readShort(at + SURA_AYAH_COUNT)
            return (0 until count).map { Ayah(number, it + 1, textOf(first + it)) }
        }
        return emptyList()
    }

    /**
     * Every āyah against the digest recorded when it was packed. IMPLEMENTATION-PLAN asks
     * for this once on first launch, off the main thread: suspect scripture is not rendered
     * and quietly hoped about, it is refused.
     */
    fun firstMismatch(digest: (ByteArray) -> ByteArray): Int? {
        val table = readInt(AYAH_TABLE_AT)
        val textAt = readInt(TEXT_AT)
        for (index in 0 until ayahCount) {
            val at = table + index * AYAH_ENTRY_SIZE
            val from = textAt + readInt(at)
            val actual = digest(bytes.copyOfRange(from, from + readShort(at + AYAH_LENGTH)))
            val recordedAt = at + AYAH_DIGEST
            if (actual.indices.any { actual[it] != bytes[recordedAt + it] } || actual.size != DIGEST_SIZE) {
                return index
            }
        }
        return null
    }

    private fun textOf(index: Int): String {
        val at = readInt(AYAH_TABLE_AT) + index * AYAH_ENTRY_SIZE
        val from = readInt(TEXT_AT) + readInt(at)
        return bytes.decodeToString(from, from + readShort(at + AYAH_LENGTH))
    }

    private fun readByte(at: Int): Int = bytes[at].toInt() and BYTE_MASK

    private fun readShort(at: Int): Int = readByte(at) or (readByte(at + 1) shl BITS_PER_BYTE)

    private fun readInt(at: Int): Int = readShort(at) or (readShort(at + 2) shl (BITS_PER_BYTE * 2))
}
