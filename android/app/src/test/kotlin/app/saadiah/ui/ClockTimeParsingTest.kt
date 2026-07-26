package app.saadiah.ui

import app.saadiah.model.Prayer
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * A masjid timetable is copied by hand, so this parses what a person actually types rather
 * than one canonical shape. A row it cannot read is dropped: the solver scores only the
 * prayers it is handed, so a typo costs that row's evidence instead of skewing the fit.
 */
class ClockTimeParsingTest {
    @Test
    fun readsTheShapesAReaderTypes() {
        for (typed in listOf("5:32", "05:32", "5.32", "0532")) {
            assertEquals(expected = LocalTime(5, 32), actual = typed.toClockTime(), message = typed)
        }
    }

    @Test
    fun readsAnAfternoonTime() {
        assertEquals(expected = LocalTime(17, 20), actual = "17:20".toClockTime())
    }

    @Test
    fun refusesImpossibleClockTimes() {
        for (typed in listOf("25:00", "12:75", "99:99")) {
            assertNull(typed.toClockTime(), typed)
        }
    }

    @Test
    fun refusesWhatIsNotATime() {
        for (typed in listOf("", "   ", "abc", "5")) {
            assertNull(typed.toClockTime(), "'$typed'")
        }
    }

    @Test
    fun anUnreadableRowIsDroppedNotGuessedAt() {
        val entered = mapOf(Prayer.FAJR to "05:12", Prayer.DHUHR to "nonsense", Prayer.ASR to "17:20")

        val times = entered.toClockTimes()

        assertEquals(expected = setOf(Prayer.FAJR, Prayer.ASR), actual = times.keys)
    }
}
