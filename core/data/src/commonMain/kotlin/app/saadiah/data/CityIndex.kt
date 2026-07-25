package app.saadiah.data

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import kotlinx.datetime.TimeZone

private const val HEADER_SIZE = 48
private const val RECORD_SIZE = 23
private const val COORDINATE_SCALE = 1e4

private const val CITY_COUNT = 8
private const val RECORDS_OFFSET = 20
private const val NAME_POOL_OFFSET = 24
private const val COUNTRY_TABLE_OFFSET = 28
private const val TIME_ZONE_TABLE_OFFSET = 32
private const val ADMIN1_TABLE_OFFSET = 36

private const val RECORD_GEONAME_ID = 4
private const val RECORD_LATITUDE = 8
private const val RECORD_LONGITUDE = 11
private const val RECORD_ADMIN1 = 14
private const val RECORD_TIME_ZONE = 16
private const val RECORD_COUNTRY = 18
private const val RECORD_POPULATION = 19

private const val HAS_DISPLAY_NAME = 1
private const val HAS_ARABIC_NAME = 2

private const val DEFAULT_LIMIT = 50
private const val SIGN_BIT_24 = 0x800000
private const val SIGN_EXTEND_24 = -0x1000000
private const val BYTE_MASK = 0xFF
private const val BITS_PER_BYTE = 8

// Arabic, Arabic Supplement and Extended-A, then the presentation forms.
private const val ARABIC_START = 0x0600
private const val ARABIC_END = 0x08FF
private const val ARABIC_FORMS_START = 0xFB50
private const val ARABIC_FORMS_END = 0xFEFC

private val MAGIC = "SDCITY01".encodeToByteArray()

/**
 * Reads the city database produced by tools/gen-city-db.py. The format is documented there;
 * the ordering of records is the contract between the two, and CityIndexTest asserts it.
 *
 * Names are decoded only for records that survive the prefix scan, so a keystroke costs a
 * binary search plus a byte comparison per candidate rather than 170,000 string decodes.
 */
class CityIndex(
    private val bytes: ByteArray,
) {
    init {
        require(bytes.size >= HEADER_SIZE && MAGIC.indices.all { bytes[it] == MAGIC[it] }) {
            "not a Saadiah city database"
        }
    }

    val size: Int = bytes.u32(CITY_COUNT)

    private val recordsAt = bytes.u32(RECORDS_OFFSET)
    private val namePoolAt = bytes.u32(NAME_POOL_OFFSET)
    private val countries = bytes.readStringTable(bytes.u32(COUNTRY_TABLE_OFFSET))
    private val timeZones = bytes.readStringTable(bytes.u32(TIME_ZONE_TABLE_OFFSET))
    private val admin1s = bytes.readStringTable(bytes.u32(ADMIN1_TABLE_OFFSET))

    /**
     * A one-letter query matches tens of thousands of places. Ranking them by sorting the
     * whole match set spent almost the entire per-keystroke budget on records that could
     * never appear, so only the best [limit] are ever held.
     */
    fun search(
        query: String,
        limit: Int = DEFAULT_LIMIT,
    ): List<City> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        val best = TopByPopulation(limit)
        if (trimmed.any {
                it.isArabic()
            }
        ) {
            forEachArabicMatch(trimmed, best::offer)
        } else {
            forEachPrefixMatch(trimmed, best::offer)
        }
        return best.records().map { cityAt(it) }
    }

    fun byId(id: CityId): City? {
        // A sorted-by-id side index would cost the file another 700 kB to save under a
        // millisecond on a lookup that happens once per settings change, not per keystroke.
        for (record in 0 until size) {
            if (bytes.u32(recordsAt + record * RECORD_SIZE + RECORD_GEONAME_ID) == id.value) return cityAt(record)
        }
        return null
    }

    internal fun searchKeyAt(record: Int): String {
        val nameAt = bytes.u32(recordsAt + record * RECORD_SIZE)
        val length = bytes.u8(namePoolAt + nameAt + 1)
        return bytes.decodeToString(namePoolAt + nameAt + 2, namePoolAt + nameAt + 2 + length).lowercase()
    }

    private fun forEachPrefixMatch(
        query: String,
        onMatch: (Int) -> Unit,
    ) {
        val needle = query.lowercase().encodeToByteArray()
        var record = lowerBound(needle)
        while (record < size && startsWith(record, needle)) {
            onMatch(record)
            record++
        }
    }

    private fun forEachArabicMatch(
        query: String,
        onMatch: (Int) -> Unit,
    ) {
        // Arabic names are not in sort order, so this is the one path that looks at every
        // entry. Reading the flag byte first skips the four fifths that carry no Arabic
        // name without decoding anything.
        val needle = query.encodeToByteArray()
        for (record in 0 until size) {
            val start = arabicNameStart(record)
            if (start < 0 || bytes.u8(start) < needle.size) continue
            if (needle.indices.all { bytes[start + 1 + it] == needle[it] }) onMatch(record)
        }
    }

    private fun arabicNameStart(record: Int): Int {
        // The offset of the Arabic name's length byte, or -1 when the record has none.
        var at = namePoolAt + bytes.u32(recordsAt + record * RECORD_SIZE)
        val flags = bytes.u8(at)
        if (flags and HAS_ARABIC_NAME == 0) return -1
        at += 2 + bytes.u8(at + 1)
        if (flags and HAS_DISPLAY_NAME != 0) at += 1 + bytes.u8(at)
        return at
    }

    private fun lowerBound(needle: ByteArray): Int {
        var low = 0
        var high = size
        while (low < high) {
            val middle = (low + high) / 2
            if (compareKey(middle, needle) < 0) low = middle + 1 else high = middle
        }
        return low
    }

    private fun compareKey(
        record: Int,
        needle: ByteArray,
    ): Int {
        val nameAt = namePoolAt + bytes.u32(recordsAt + record * RECORD_SIZE)
        val length = bytes.u8(nameAt + 1)
        val shared = minOf(length, needle.size)
        for (offset in 0 until shared) {
            val difference = lowercaseAscii(bytes.u8(nameAt + 2 + offset)) - (needle[offset].toInt() and BYTE_MASK)
            if (difference != 0) return difference
        }
        return length - needle.size
    }

    private fun startsWith(
        record: Int,
        needle: ByteArray,
    ): Boolean {
        val nameAt = namePoolAt + bytes.u32(recordsAt + record * RECORD_SIZE)
        if (bytes.u8(nameAt + 1) < needle.size) return false
        return needle.indices.all {
            lowercaseAscii(bytes.u8(nameAt + 2 + it)) == (needle[it].toInt() and BYTE_MASK)
        }
    }

    private fun cityAt(record: Int): City {
        val at = recordsAt + record * RECORD_SIZE
        val names = nameBlobAt(record)
        return City(
            id = CityId(bytes.u32(at + RECORD_GEONAME_ID)),
            name = names.display ?: names.ascii,
            country = CountryCode(countries[bytes.u8(at + RECORD_COUNTRY)]),
            admin1 = admin1s[bytes.u16(at + RECORD_ADMIN1)],
            coordinates =
                Coordinates(
                    latitude = bytes.i24(at + RECORD_LATITUDE) / COORDINATE_SCALE,
                    longitude = bytes.i24(at + RECORD_LONGITUDE) / COORDINATE_SCALE,
                ),
            timeZone = TimeZone.of(timeZones[bytes.u16(at + RECORD_TIME_ZONE)]),
            arabicName = names.arabic,
        )
    }

    private fun nameBlobAt(record: Int): Names {
        var at = namePoolAt + bytes.u32(recordsAt + record * RECORD_SIZE)
        val flags = bytes.u8(at)
        val asciiLength = bytes.u8(at + 1)
        val ascii = bytes.decodeToString(at + 2, at + 2 + asciiLength)
        at += 2 + asciiLength
        var display: String? = null
        if (flags and HAS_DISPLAY_NAME != 0) {
            val length = bytes.u8(at)
            display = bytes.decodeToString(at + 1, at + 1 + length)
            at += 1 + length
        }
        var arabic: String? = null
        if (flags and HAS_ARABIC_NAME != 0) {
            val length = bytes.u8(at)
            arabic = bytes.decodeToString(at + 1, at + 1 + length)
        }
        return Names(ascii, display, arabic)
    }

    private data class Names(
        val ascii: String,
        val display: String?,
        val arabic: String?,
    )

    /**
     * Keeps the [limit] most populous records seen, without boxing or holding the rest.
     * Replacements grow rare as the threshold rises, so rescanning for the new smallest is
     * cheaper here than carrying a heap.
     */
    private inner class TopByPopulation(
        private val limit: Int,
    ) {
        private val records = IntArray(limit)
        private val populations = IntArray(limit)
        private var held = 0
        private var smallest = 0

        fun offer(record: Int) {
            val population = bytes.u32(recordsAt + record * RECORD_SIZE + RECORD_POPULATION)
            if (held < limit) {
                records[held] = record
                populations[held] = population
                held++
                if (held == limit) locateSmallest()
                return
            }
            if (population <= populations[smallest]) return
            records[smallest] = record
            populations[smallest] = population
            locateSmallest()
        }

        fun records(): List<Int> =
            (0 until held)
                .sortedByDescending { populations[it] }
                .map { records[it] }

        private fun locateSmallest() {
            var at = 0
            for (candidate in 1 until held) {
                if (populations[candidate] < populations[at]) at = candidate
            }
            smallest = at
        }
    }
}

private fun ByteArray.readStringTable(at: Int): List<String> {
    val count = u16(at)
    val values = ArrayList<String>(count)
    var cursor = at + 2
    repeat(count) {
        val length = u8(cursor)
        values += decodeToString(cursor + 1, cursor + 1 + length)
        cursor += 1 + length
    }
    return values
}

private fun ByteArray.u8(at: Int): Int = this[at].toInt() and BYTE_MASK

private fun ByteArray.u16(at: Int): Int = u8(at) or (u8(at + 1) shl BITS_PER_BYTE)

private fun ByteArray.u32(at: Int): Int = u16(at) or (u16(at + 2) shl (BITS_PER_BYTE * 2))

private fun ByteArray.i24(at: Int): Int {
    val raw = u16(at) or (u8(at + 2) shl (BITS_PER_BYTE * 2))
    return if (raw and SIGN_BIT_24 != 0) raw or SIGN_EXTEND_24 else raw
}

private fun lowercaseAscii(byte: Int): Int = if (byte in 'A'.code..'Z'.code) byte + ('a'.code - 'A'.code) else byte

private fun Char.isArabic(): Boolean = code in ARABIC_START..ARABIC_END || code in ARABIC_FORMS_START..ARABIC_FORMS_END
