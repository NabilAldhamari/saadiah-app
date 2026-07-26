package app.saadiah.ui

import androidx.compose.ui.unit.LayoutDirection
import app.saadiah.model.CombineMode
import app.saadiah.model.Language
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.Tradition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

private val ARABIC = '؀'..'ۿ'

// A spread across every screen rather than the whole table: enough that English pasted
// into the Arabic side shows up, without restating the file.
private val ARABIC_SAMPLE =
    with(ArabicStrings) {
        listOf(back, comingSoon, tabToday, tabMore, titleSettings, titleChooseCity) +
            listOf(sectionLanguage, sectionWhichPrayers, privacyNote, everyPrayerSilent) +
            listOf(doNotWarnMe, followMyPhone, searchForYourCity, noCityMatches) +
            listOf(alertsArriveQuestion, noAlertYet, adhkarNotBundled) +
            listOf(baqarahNotBundled, nawafil, legendFast, itIsTimeForThisPrayer) +
            listOf(fastingRecommended, whyHighLatitude, hijamah, tomorrow)
    }

/**
 * The language setting used to change nothing at all, so these assert the switch actually
 * moves what a reader sees rather than only what is stored.
 */
class StringsTest {
    @Test
    fun choosingArabicChangesTheWords() {
        assertNotEquals(
            illegal = stringsFor(Language.ENGLISH).tabToday,
            actual = stringsFor(Language.ARABIC).tabToday,
        )
    }

    @Test
    fun choosingArabicTurnsTheLayoutAround() {
        assertEquals(expected = LayoutDirection.Rtl, actual = layoutDirectionFor(Language.ARABIC))
        assertEquals(expected = LayoutDirection.Ltr, actual = layoutDirectionFor(Language.ENGLISH))
    }

    /**
     * The interface already makes a *missing* translation a compile error. What it cannot
     * catch is English pasted into the Arabic table, so a spread of it is read back here.
     */
    @Test
    fun theArabicTableIsActuallyInArabic() {
        val untranslated = ARABIC_SAMPLE.filter { value -> value.none { it in ARABIC } }

        assertTrue(untranslated.isEmpty(), "these were left in English: $untranslated")
    }

    @Test
    fun everyPrayerAndMonthIsTranslated() {
        for (table in listOf(EnglishStrings, ArabicStrings)) {
            assertEquals(expected = Prayer.entries.size, actual = table.prayerNames.size)
            assertEquals(expected = 12, actual = table.hijriMonths.size)
            assertEquals(expected = 12, actual = table.gregorianMonths.size)
            assertEquals(expected = 7, actual = table.weekdays.size)
        }
    }

    @Test
    fun everySettingLabelHasAnArabicForm() {
        val english =
            Tradition.entries.map { it.spelledOut(EnglishStrings) } +
                Madhab.entries.map { it.spelledOut(EnglishStrings) } +
                CombineMode.entries.map { it.spelledOut(EnglishStrings) } +
                Language.entries.map { it.spelledOut(EnglishStrings) }
        val arabic =
            Tradition.entries.map { it.spelledOut(ArabicStrings) } +
                Madhab.entries.map { it.spelledOut(ArabicStrings) } +
                CombineMode.entries.map { it.spelledOut(ArabicStrings) } +
                Language.entries.map { it.spelledOut(ArabicStrings) }

        for ((left, right) in english.zip(arabic)) {
            assertNotEquals(illegal = left, actual = right, message = "'$left' was never translated")
        }
    }

    @Test
    fun durationsAreSpelledOutInBothLanguages() {
        assertEquals(expected = "2 hours 14 minutes", actual = 134.minutes.spelledOut(EnglishStrings))
        assertTrue(134.minutes.spelledOut(ArabicStrings).any { it in ARABIC })
    }

    @Test
    fun theSystemChoiceFallsBackToARealTable() {
        assertTrue(stringsFor(Language.SYSTEM) === EnglishStrings || stringsFor(Language.SYSTEM) === ArabicStrings)
    }
}
