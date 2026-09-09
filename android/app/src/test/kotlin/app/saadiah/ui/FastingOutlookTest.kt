package app.saadiah.ui

import app.saadiah.calendar.toGregorianDate
import app.saadiah.design.ObservanceMarker
import app.saadiah.model.HijriDate
import app.saadiah.model.Tradition
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FastingOutlookTest {
    @Test
    fun bundlesMondayAndWhiteDaysWhenCoinciding() {
        // Find a date where Monday falls on day 13, 14, or 15 of a Hijri month
        // Let's probe from 1447 AH
        var candidateHijri: HijriDate? = null
        var candidateGregorian: LocalDate? = null
        for (month in 1..12) {
            if (month == 9) continue // Ramadan has obligatory fast
            for (day in listOf(13, 14, 15)) {
                val h = HijriDate(year = 1448, month = month, day = day)
                val g = h.toGregorianDate()
                if (g.dayOfWeek == DayOfWeek.MONDAY) {
                    candidateHijri = h
                    candidateGregorian = g
                    break
                }
            }
            if (candidateHijri != null) break
        }

        assertNotNull(candidateHijri, "found a coinciding Monday White Day")
        val today = candidateGregorian!!
        val prompts = fastingOutlook(today, candidateHijri, Tradition.SUNNI, ArabicStrings)

        // Today must be bundled into 1 prompt containing both Monday and White Days
        val todayPrompt = prompts.first()
        assertTrue(
            todayPrompt.title.contains(ArabicStrings.fastingMonday),
            "bundled title should contain Monday fast: ${todayPrompt.title}",
        )
        assertTrue(
            todayPrompt.title.contains(ArabicStrings.ayyamAlBid),
            "bundled title should contain White Days: ${todayPrompt.title}",
        )
        assertEquals(ObservanceMarker.RECOMMENDED_FAST, todayPrompt.marker)
    }

    @Test
    fun whiteDaysAreTrackedOnDays14And15() {
        // Ensure that on day 14 and 15, White Days is active and not null/dropped
        val day14Hijri = HijriDate(year = 1448, month = 2, day = 14)
        val day14Date = day14Hijri.toGregorianDate()
        val prompts14 = fastingOutlook(day14Date, day14Hijri, Tradition.SUNNI, ArabicStrings)

        val hasWhiteDays14 = prompts14.any { it.title.contains(ArabicStrings.ayyamAlBid) }
        assertTrue(hasWhiteDays14, "White days must be present on day 14")

        val day15Hijri = HijriDate(year = 1448, month = 2, day = 15)
        val day15Date = day15Hijri.toGregorianDate()
        val prompts15 = fastingOutlook(day15Date, day15Hijri, Tradition.SUNNI, ArabicStrings)

        val hasWhiteDays15 = prompts15.any { it.title.contains(ArabicStrings.ayyamAlBid) }
        assertTrue(hasWhiteDays15, "White days must be present on day 15")
    }

    @Test
    fun whiteDaysMoreThanOneWeekAheadAreNotShown() {
        // On day 20, White days of the next month is > 1 week away -> must NOT be shown
        val day20Hijri = HijriDate(year = 1448, month = 2, day = 20)
        val day20Date = day20Hijri.toGregorianDate()
        val prompts20 = fastingOutlook(day20Date, day20Hijri, Tradition.SUNNI, ArabicStrings)
        val hasWhiteDays20 = prompts20.any { it.title.contains(ArabicStrings.ayyamAlBid) }
        kotlin.test.assertFalse(hasWhiteDays20, "White days > 7 days away must not be shown on day 20")

        // On day 2, White days is 11 days away -> must NOT be shown
        val day2Hijri = HijriDate(year = 1448, month = 2, day = 2)
        val day2Date = day2Hijri.toGregorianDate()
        val prompts2 = fastingOutlook(day2Date, day2Hijri, Tradition.SUNNI, ArabicStrings)
        val hasWhiteDays2 = prompts2.any { it.title.contains(ArabicStrings.ayyamAlBid) }
        kotlin.test.assertFalse(hasWhiteDays2, "White days > 7 days away must not be shown on day 2")

        // On day 7, White days is 6 days away (within 1 week) -> MUST be shown
        val day7Hijri = HijriDate(year = 1448, month = 2, day = 7)
        val day7Date = day7Hijri.toGregorianDate()
        val prompts7 = fastingOutlook(day7Date, day7Hijri, Tradition.SUNNI, ArabicStrings)
        val hasWhiteDays7 = prompts7.any { it.title.contains(ArabicStrings.ayyamAlBid) }
        assertTrue(hasWhiteDays7, "White days within 7 days must be shown on day 7")
    }

    @Test
    fun hijamahIsProperlyCategorizedAndNotShownIfMoreThanOneWeekAhead() {
        // On day 10, Hijamah is 7 days away (on day 17) -> within 1 week -> MUST be shown
        val hijri10 = HijriDate(year = 1448, month = 2, day = 10)
        val date10 = hijri10.toGregorianDate()
        val prompts10 = fastingOutlook(date10, hijri10, Tradition.SUNNI, EnglishStrings)

        val hijamaPrompt = prompts10.single { it.marker == ObservanceMarker.HIJAMAH }
        assertEquals(EnglishStrings.hijamah, hijamaPrompt.title)

        // On day 2, Hijamah is 15 days away -> > 1 week -> must NOT be shown
        val hijri2 = HijriDate(year = 1448, month = 2, day = 2)
        val date2 = hijri2.toGregorianDate()
        val prompts2 = fastingOutlook(date2, hijri2, Tradition.SUNNI, EnglishStrings)
        val hasHijamah2 = prompts2.any { it.marker == ObservanceMarker.HIJAMAH }
        kotlin.test.assertFalse(hasHijamah2, "Hijamah > 7 days away must not be shown on day 2")

        // On day 22, Hijamah is ~24 days away (in next month) -> must NOT be shown
        val hijri22 = HijriDate(year = 1448, month = 2, day = 22)
        val date22 = hijri22.toGregorianDate()
        val prompts22 = fastingOutlook(date22, hijri22, Tradition.SUNNI, EnglishStrings)
        val hasHijamah22 = prompts22.any { it.marker == ObservanceMarker.HIJAMAH }
        kotlin.test.assertFalse(hasHijamah22, "Hijamah > 7 days away must not be shown on day 22")
    }
}
