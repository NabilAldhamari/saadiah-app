package app.saadiah.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HijriDateTest {
    @Test
    fun buildsValidDate() {
        val date = HijriDate(year = 1447, month = 1, day = 10)
        assertEquals(HijriDate(year = 1447, month = 1, day = 10), date)
    }

    @Test
    fun rejectsDayZero() {
        assertFailsWith<IllegalArgumentException> { HijriDate(year = 1447, month = 1, day = 0) }
    }

    @Test
    fun rejectsDayAboveThirty() {
        assertFailsWith<IllegalArgumentException> { HijriDate(year = 1447, month = 1, day = 31) }
    }

    @Test
    fun rejectsMonthZero() {
        assertFailsWith<IllegalArgumentException> { HijriDate(year = 1447, month = 0, day = 1) }
    }

    @Test
    fun rejectsMonthAboveTwelve() {
        assertFailsWith<IllegalArgumentException> { HijriDate(year = 1447, month = 13, day = 1) }
    }

    @Test
    fun rejectsNonPositiveYear() {
        assertFailsWith<IllegalArgumentException> { HijriDate(year = 0, month = 1, day = 1) }
    }
}
