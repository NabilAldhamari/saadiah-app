package app.saadiah.data

import kotlin.test.Test
import kotlin.test.assertEquals

private const val MONDAY = "2026-07-27"
private const val TUESDAY = "2026-07-28"

class BaqarahCounterTest {
    @Test
    fun startsAtNothingRead() {
        assertEquals(expected = 0, actual = Settings().baqarahReadCount)
    }

    @Test
    fun markingTodayCountsTheDay() {
        val after = Settings().markBaqarahReadToday(MONDAY)

        assertEquals(expected = 1, actual = after.baqarahReadCount)
        assertEquals(expected = MONDAY, actual = after.baqarahLastRead)
    }

    @Test
    fun markingTwiceInADayCountsOnce() {
        val after = Settings().markBaqarahReadToday(MONDAY).markBaqarahReadToday(MONDAY)

        assertEquals(expected = 1, actual = after.baqarahReadCount, message = "a second tap must not inflate the count")
    }

    @Test
    fun theNextDayCountsAgain() {
        val after = Settings().markBaqarahReadToday(MONDAY).markBaqarahReadToday(TUESDAY)

        assertEquals(expected = 2, actual = after.baqarahReadCount)
    }

    @Test
    fun theCountSurvivesUnrelatedChanges() {
        val after = Settings().markBaqarahReadToday(MONDAY).copy(preAlert = null)

        assertEquals(expected = 1, actual = after.baqarahReadCount)
    }
}
