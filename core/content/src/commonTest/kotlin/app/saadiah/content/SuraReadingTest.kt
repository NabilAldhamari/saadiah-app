package app.saadiah.content

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val AL_FATIHAH = 1
private const val AL_BAQARAH = 2
private const val ALIF_LAM_MIM = "الٓمٓ"

class SuraReadingTest {
    @Test
    fun theOpeningIsLiftedOutOfTheFirstAyah() {
        val stored = listOf(Ayah(AL_BAQARAH, 1, "$BASMALAH $ALIF_LAM_MIM"))

        val reading = stored.asReading()

        assertEquals(expected = BASMALAH, actual = reading.basmalah)
        assertEquals(expected = ALIF_LAM_MIM, actual = reading.ayat.single().text)
    }

    @Test
    fun liftingTheOpeningKeepsEveryAyahAndItsNumber() {
        val stored =
            listOf(
                Ayah(AL_BAQARAH, 1, "$BASMALAH $ALIF_LAM_MIM"),
                Ayah(AL_BAQARAH, 2, "ذَٰلِكَ ٱلْكِتَٰبُ"),
            )

        val reading = stored.asReading()

        assertEquals(expected = 2, actual = reading.ayat.size)
        assertEquals(expected = listOf(1, 2), actual = reading.ayat.map { it.number })
    }

    /** In al-Fātiḥah the basmalah is āyah 1. Splitting it there would drop a verse. */
    @Test
    fun alFatihahKeepsItsOpeningAsTheFirstAyah() {
        val stored = listOf(Ayah(AL_FATIHAH, 1, BASMALAH))

        val reading = stored.asReading()

        assertNull(reading.basmalah)
        assertEquals(expected = BASMALAH, actual = reading.ayat.single().text)
    }

    @Test
    fun aSuraThatDoesNotOpenWithTheBasmalahIsLeftAlone() {
        val stored = listOf(Ayah(sura = 9, number = 1, text = "بَرَآءَةٌ مِّنَ ٱللَّهِ"))

        val reading = stored.asReading()

        assertNull(reading.basmalah)
        assertEquals(expected = stored, actual = reading.ayat)
    }

    @Test
    fun anEmptySuraReadsAsEmptyRatherThanThrowing() {
        val reading = emptyList<Ayah>().asReading()

        assertNull(reading.basmalah)
        assertTrue(reading.ayat.isEmpty())
    }

    /** The split may only move the boundary, never alter a character of the text. */
    @Test
    fun theTextIsUnchangedApartFromTheSplit() {
        val stored = listOf(Ayah(AL_BAQARAH, 1, "$BASMALAH $ALIF_LAM_MIM"))

        val reading = stored.asReading()

        assertEquals(
            expected = "$BASMALAH $ALIF_LAM_MIM",
            actual = "${reading.basmalah} ${reading.ayat.single().text}",
        )
    }

    @Test
    fun liftsBasmalahFromVerbatimTanzilByteOrder() {
        val tanzilBaqarah1 = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ $ALIF_LAM_MIM"
        val stored = listOf(Ayah(AL_BAQARAH, 1, tanzilBaqarah1))

        val reading = stored.asReading()

        assertEquals(expected = BASMALAH, actual = reading.basmalah)
        assertEquals(expected = ALIF_LAM_MIM, actual = reading.ayat.single().text)
    }
}
