package app.saadiah.content

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val FATHATAN = "ً"
private const val DAMMATAN = "ٌ"
private const val KASRATAN = "ٍ"
private const val FATHA = "َ"
private const val DAMMA = "ُ"
private const val KASRA = "ِ"
private const val SHADDA = "ّ"
private const val SUKUN = "ْ"
private const val SUPERSCRIPT_ALEF = "ٰ"
private const val TATWEEL = "ـ"

private const val ALEF = "ا"
private const val ALEF_MADDA = "آ"
private const val ALEF_HAMZA_ABOVE = "أ"
private const val ALEF_HAMZA_BELOW = "إ"
private const val ALEF_WASLA = "ٱ"
private const val TEH_MARBUTA = "ة"
private const val HEH = "ه"
private const val ALEF_MAKSURA = "ى"
private const val YEH = "ي"
private const val BEH = "ب"
private const val SEEN = "س"
private const val MEEM = "م"
private const val LAM = "ل"
private const val NOON = "ن"
private const val REH = "ر"
private const val HAH = "ح"
private const val SMALL_HIGH_SEEN = "ۜ"
private const val SAJDA_SIGN = "۩"

class ArabicNormalisationTest {
    private val cases: List<Pair<String, String>> =
        listOf(
            // Every harakat is dropped.
            BEH + FATHATAN to BEH,
            BEH + DAMMATAN to BEH,
            BEH + KASRATAN to BEH,
            BEH + FATHA to BEH,
            BEH + DAMMA to BEH,
            BEH + KASRA to BEH,
            BEH + SHADDA to BEH,
            BEH + SUKUN to BEH,
            BEH + SUPERSCRIPT_ALEF to BEH,
            // Every alif form folds to bare alif.
            ALEF_MADDA to ALEF,
            ALEF_HAMZA_ABOVE to ALEF,
            ALEF_HAMZA_BELOW to ALEF,
            ALEF_WASLA to ALEF,
            ALEF to ALEF,
            // Ta marbuta becomes heh, alif maqsura becomes yeh.
            TEH_MARBUTA to HEH,
            ALEF_MAKSURA to YEH,
            HEH to HEH,
            YEH to YEH,
            // Tatweel is stretching, never meaning.
            BEH + TATWEEL + SEEN to BEH + SEEN,
            TATWEEL + TATWEEL to "",
            // Quranic annotation marks never belong in an index.
            BEH + SMALL_HIGH_SEEN to BEH,
            BEH + SAJDA_SIGN to BEH,
            // Whole words from the Uthmani text.
            BEH + KASRA + SEEN + MEEM + KASRA to BEH + SEEN + MEEM,
            ALEF + LAM + LAM + SHADDA + HEH + KASRA to ALEF + LAM + LAM + HEH,
            ALEF_HAMZA_ABOVE + MEEM + FATHA + NOON to ALEF + MEEM + NOON,
            // Rules interacting on one word.
            ALEF_HAMZA_BELOW + KASRA + LAM + FATHA + ALEF_MAKSURA to ALEF + LAM + YEH,
            ALEF_WASLA + LAM + REH + SHADDA + HAH + MEEM + SUPERSCRIPT_ALEF + NOON to
                ALEF + LAM + REH + HAH + MEEM + NOON,
            TEH_MARBUTA + FATHA + TATWEEL to HEH,
            // Nothing else is touched.
            "" to "",
            " " to " ",
            "abc" to "abc",
            "123" to "123",
            "Fajr 04:12" to "Fajr 04:12",
            BEH + " " + SEEN to BEH + " " + SEEN,
        )

    @Test
    fun normalisesEveryCase() {
        for ((input, expected) in cases) {
            assertEquals(expected = expected, actual = normalise(input), message = describe(input))
        }
    }

    @Test
    fun isIdempotent() {
        for ((input, _) in cases) {
            val once = normalise(input)
            assertEquals(expected = once, actual = normalise(once), message = describe(input))
        }
    }

    @Test
    fun foldsEveryAlifFormTogether() {
        val forms = listOf(ALEF, ALEF_MADDA, ALEF_HAMZA_ABOVE, ALEF_HAMZA_BELOW, ALEF_WASLA)

        assertEquals(expected = 1, actual = forms.map { normalise(it + LAM) }.distinct().size)
    }

    @Test
    fun leavesNoStrippableMarkBehind() {
        val marked = ALEF_WASLA + LAM + SHADDA + FATHA + HEH + TATWEEL + SMALL_HIGH_SEEN + SUPERSCRIPT_ALEF

        val result = normalise(marked)

        assertTrue(result.none { it in 'ً'..'ْ' }, "harakat survived: $result")
        assertTrue(result.none { it in 'ۖ'..'ۭ' }, "quranic mark survived: $result")
        assertTrue(TATWEEL !in result && SUPERSCRIPT_ALEF !in result, "stretching survived: $result")
    }

    private fun describe(input: String): String = input.map { it.code.toString(radix = 16) }.joinToString(" ")
}
