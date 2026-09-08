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

// Every no-argument String getter on the interface, read back off the table by reflection.
// This was a hand-listed sample of two dozen values, and the entries it did not name went
// unchecked — which is how "AM"/"PM" reached every clock in the Arabic build. A sample only
// ever covers what someone remembered to add to it; the interface knows the whole list.
private fun arabicValues(): Map<String, String> =
    Strings::class.java.methods
        .filter { it.parameterCount == 0 && it.returnType == String::class.java }
        .associate { it.name to (it.invoke(ArabicStrings) as String) }

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
     * catch is English pasted into the Arabic table, so every entry is read back here.
     */
    @Test
    fun everyStringInTheArabicTableIsActuallyInArabic() {
        val untranslated = arabicValues().filterValues { value -> value.none { it in ARABIC } }

        assertTrue(untranslated.isEmpty(), "these were left in English: ${untranslated.keys}")
    }

    @Test
    fun theTableIsBigEnoughThatReflectionFoundIt() {
        assertTrue(arabicValues().size > 100, "reflection found only ${arabicValues().size} strings")
    }

    @Test
    fun everyCalculationMethodIsNamedInBothTables() {
        for (method in app.saadiah.prayer.Method.entries) {
            assertTrue(method.name in EnglishStrings.methodNames, "${method.name} has no English name")
            assertTrue(method.name in ArabicStrings.methodNames, "${method.name} has no Arabic name")
        }
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
                Language.entries.map { it.spelledOut(EnglishStrings) } +
                app.saadiah.model.FastingReminderCadence.entries
                    .map { it.spelledOut(EnglishStrings) } +
                app.saadiah.model.AfterPrayerReminderDelay.entries
                    .map { it.spelledOut(EnglishStrings) } +
                app.saadiah.model.QuranViewMode.entries
                    .map { it.spelledOut(EnglishStrings) } +
                app.saadiah.model.Reciter.entries
                    .map { it.spelledOut(EnglishStrings) }
        val arabic =
            Tradition.entries.map { it.spelledOut(ArabicStrings) } +
                Madhab.entries.map { it.spelledOut(ArabicStrings) } +
                CombineMode.entries.map { it.spelledOut(ArabicStrings) } +
                Language.entries.map { it.spelledOut(ArabicStrings) } +
                app.saadiah.model.FastingReminderCadence.entries
                    .map { it.spelledOut(ArabicStrings) } +
                app.saadiah.model.AfterPrayerReminderDelay.entries
                    .map { it.spelledOut(ArabicStrings) } +
                app.saadiah.model.QuranViewMode.entries
                    .map { it.spelledOut(ArabicStrings) } +
                app.saadiah.model.Reciter.entries
                    .map { it.spelledOut(ArabicStrings) }

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
