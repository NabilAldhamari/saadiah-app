package app.saadiah.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomDhikrTest {
    @Test
    fun aDhikrSurvivesTheRoundTrip() {
        val written = listOf(CustomDhikr(id = "a", text = "سبحان الله", repetitions = 33))

        assertEquals(expected = written, actual = decodeCustomAdhkar(encodeCustomAdhkar(written)))
    }

    /**
     * The store keeps these in a set, which has no order of its own. The order therefore
     * travels inside each record; without it the list would reshuffle on every read.
     */
    @Test
    fun theOrderSurvivesAnUnorderedStore() {
        val written =
            listOf(
                CustomDhikr(id = "first", text = "one", repetitions = 1),
                CustomDhikr(id = "second", text = "two", repetitions = 2),
                CustomDhikr(id = "third", text = "three", repetitions = 3),
            )

        val shuffled = encodeCustomAdhkar(written).shuffled().toSet()

        assertEquals(expected = written, actual = decodeCustomAdhkar(shuffled))
    }

    @Test
    fun aSeparatorTypedByAReaderCannotSplitARecord() {
        val awkward = CustomDhikr(id = "x", text = "before${FIELD_SEPARATOR}after", repetitions = 1)

        val read = decodeCustomAdhkar(encodeCustomAdhkar(listOf(awkward))).single()

        assertEquals(expected = 1, actual = read.repetitions, message = "the record was split by the reader's text")
        assertTrue(FIELD_SEPARATOR !in read.text, "the separator must not reach storage")
        assertTrue(read.text.startsWith("before"), "the rest of the text must survive: '${read.text}'")
    }

    @Test
    fun textWithLineBreaksIsKept() {
        val multiline = CustomDhikr(id = "m", text = "first line\nsecond line", repetitions = 7)

        assertEquals(expected = multiline, actual = decodeCustomAdhkar(encodeCustomAdhkar(listOf(multiline))).single())
    }

    @Test
    fun nothingStoredIsAnEmptyList() {
        assertTrue(decodeCustomAdhkar(emptySet()).isEmpty())
    }

    @Test
    fun aCorruptRecordIsDroppedRatherThanCrashing() {
        val good = encodeCustomAdhkar(listOf(CustomDhikr(id = "a", text = "fine", repetitions = 1)))

        val read = decodeCustomAdhkar(good + setOf("not a record", "", "1"))

        assertEquals(expected = 1, actual = read.size, message = "a bad row should cost that row, not the section")
    }

    @Test
    fun aDhikrWithNoTextCannotBeConstructed() {
        assertTrue(runCatching { CustomDhikr(id = "a", text = "   ", repetitions = 1) }.isFailure)
    }

    @Test
    fun repetitionsAreAtLeastOne() {
        assertTrue(runCatching { CustomDhikr(id = "a", text = "x", repetitions = 0) }.isFailure)
    }
}
