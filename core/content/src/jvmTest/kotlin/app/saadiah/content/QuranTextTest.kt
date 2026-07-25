package app.saadiah.content

import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val BAQARAH = 2
private const val AL_IMRAN = 3
private const val BAQARAH_AYAT = 286
private const val AL_IMRAN_AYAT = 200

private fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)

class QuranTextTest {
    private val quran: QuranText by lazy {
        val file = File("../../android/app/src/main/assets/quran.bin")
        assertTrue(file.exists(), "quran.bin not found at ${file.absolutePath}; run tools/gen-quran-db.py")
        QuranText(file.readBytes())
    }

    @Test
    fun carriesBothSuras() {
        assertEquals(expected = listOf(BAQARAH, AL_IMRAN), actual = quran.suras)
        assertEquals(expected = BAQARAH_AYAT + AL_IMRAN_AYAT, actual = quran.ayahCount)
    }

    @Test
    fun eachSuraHasTheAyatItShould() {
        assertEquals(expected = BAQARAH_AYAT, actual = quran.sura(BAQARAH).size)
        assertEquals(expected = AL_IMRAN_AYAT, actual = quran.sura(AL_IMRAN).size)
    }

    @Test
    fun ayatAreNumberedFromOne() {
        val baqarah = quran.sura(BAQARAH)

        assertEquals(expected = 1, actual = baqarah.first().number)
        assertEquals(expected = BAQARAH_AYAT, actual = baqarah.last().number)
    }

    /**
     * Tanzil ships the Basmala as part of the first āyah of each sura, which is how the
     * Uthmani muṣḥaf prints it. It is left exactly as received: the licence permits verbatim
     * copies only, so the app renders what Tanzil published rather than a tidier split.
     */
    @Test
    fun theFirstAyahOfEachSuraCarriesTheBasmalaAsTanzilShipsIt() {
        for (sura in listOf(BAQARAH, AL_IMRAN)) {
            assertTrue(
                quran
                    .sura(sura)
                    .first()
                    .text
                    .startsWith("بِسْمِ"),
                "sura $sura should open as Tanzil publishes it",
            )
        }
    }

    /**
     * 2:282, the verse of debt, is by a wide margin the longest in the Qurʾān. Asserting on
     * a length rather than on Arabic text means the check cannot pass or fail on how this
     * file happens to encode a letter.
     */
    @Test
    fun theLongestVerseIsWhereItShouldBe() {
        val baqarah = quran.sura(BAQARAH)

        val longest = baqarah.maxBy { it.text.length }

        assertEquals(expected = 282, actual = longest.number, message = "the packing has slipped")
    }

    @Test
    fun everyAyahMatchesItsRecordedDigest() {
        assertNull(quran.firstMismatch(::sha256), "the bundled text does not match the digests packed with it")
    }

    @Test
    fun aTamperedTextIsRefusedRatherThanRendered() {
        val bytes = File("../../android/app/src/main/assets/quran.bin").readBytes()
        // Flip a byte deep inside the text pool, where a corrupted copy would differ.
        val textStart = bytes.size / 2
        bytes[textStart] = (bytes[textStart].toInt() xor 0x01).toByte()

        val mismatch = QuranText(bytes).firstMismatch(::sha256)

        assertNotNull(mismatch, "a changed byte must be caught, not rendered")
    }

    @Test
    fun theLicenceNoticeTravelsWithTheText() {
        assertTrue(quran.notice.contains("Tanzil"), "the notice must name its source: '${quran.notice}'")
    }

    @Test
    fun anUnknownSuraIsEmptyRatherThanAnError() {
        assertTrue(quran.sura(114).isEmpty())
    }
}
