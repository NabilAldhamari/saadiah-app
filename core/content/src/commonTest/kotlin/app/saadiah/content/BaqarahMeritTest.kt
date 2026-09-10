package app.saadiah.content

import app.saadiah.model.Tradition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private val ARABIC = '؀'..'ۿ'
private val LATIN = 'A'..'Z'

class BaqarahMeritTest {
    private val merits = baqarahMerits(Tradition.SUNNI)

    @Test
    fun everyMeritCarriesItsOriginalArabic() {
        val missing = merits.filter { it.arabic.isNullOrBlank() }

        assertTrue(missing.isEmpty(), "no Arabic bundled for ${missing.map { it.id }}")
    }

    @Test
    fun noOriginalIsAccidentallyInLatin() {
        for (merit in merits) {
            val arabic = merit.arabic.orEmpty()

            assertTrue(arabic.any { it in ARABIC }, "${merit.id} has no Arabic in it")
            assertTrue(arabic.none { it in LATIN }, "${merit.id} has Latin letters in its original")
        }
    }

    /**
     * Muslim 804's English runs to the al-Baqarah portion — "to take recourse to it is a
     * blessing" — so an Arabic original that stopped short of it would leave the two
     * renderings saying different things on the same card.
     */
    @Test
    fun the804OriginalReachesTheBaqarahPortion() {
        val merit = merits.single { it.id == "muslim-804" }

        assertTrue(merit.arabic!!.contains("أخذَها بَرَكَةٌ"), "the original stops before the al-Baqarah portion")
        assertTrue(merit.arabic!!.contains("البَطَلَةُ"), "the original stops before the closing")
    }

    @Test
    fun everyMeritIsSourcedAndOrdered() {
        assertEquals(expected = merits.map { it.order }.sorted(), actual = merits.map { it.order })
        assertTrue(merits.all { it.source.isNotBlank() })
    }

    @Test
    fun aBlankOriginalIsRefusedRatherThanDrawnAsAnEmptyLine() {
        assertFailsWith<IllegalArgumentException> {
            BaqarahMerit(
                id = "blank",
                order = 1,
                arabic = "",
                translationArabic = null,
                translation = "something",
                source = "somewhere",
                kind = MeritKind.HADITH,
                traditions = setOf(Tradition.SUNNI),
            )
        }
    }

    @Test
    fun aBlankArabicRenderingIsRefusedToo() {
        assertFailsWith<IllegalArgumentException> {
            BaqarahMerit(
                id = "blank",
                order = 1,
                arabic = null,
                translationArabic = "   ",
                translation = "something",
                source = "somewhere",
                kind = MeritKind.HADITH,
                traditions = setOf(Tradition.SUNNI),
            )
        }
    }

    @Test
    fun aBlankSourceIsRefused() {
        assertFailsWith<IllegalArgumentException> {
            BaqarahMerit(
                id = "blank",
                order = 1,
                arabic = null,
                translationArabic = null,
                translation = "something",
                source = "   ",
                kind = MeritKind.HADITH,
                traditions = setOf(Tradition.SUNNI),
            )
        }
    }

    @Test
    fun aBlankTranslationIsRefused() {
        assertFailsWith<IllegalArgumentException> {
            BaqarahMerit(
                id = "blank",
                order = 1,
                arabic = null,
                translationArabic = null,
                translation = "   ",
                source = "somewhere",
                kind = MeritKind.HADITH,
                traditions = setOf(Tradition.SUNNI),
            )
        }
    }

    @Test
    fun emptyTraditionsAreRefused() {
        assertFailsWith<IllegalArgumentException> {
            BaqarahMerit(
                id = "blank",
                order = 1,
                arabic = null,
                translationArabic = null,
                translation = "something",
                source = "somewhere",
                kind = MeritKind.HADITH,
                traditions = emptySet(),
            )
        }
    }
}
