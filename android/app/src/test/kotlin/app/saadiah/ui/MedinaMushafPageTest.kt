package app.saadiah.ui

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MedinaMushafPageTest {
    @Test
    fun surah2MedinaMushafPageBoundaries() {
        assertEquals(2, medinaMushafPage(2, 1))
        assertEquals(2, medinaMushafPage(2, 5))
        assertEquals(3, medinaMushafPage(2, 6))
        assertEquals(3, medinaMushafPage(2, 16))
        assertEquals(4, medinaMushafPage(2, 17))
        assertEquals(48, medinaMushafPage(2, 282))
        assertEquals(49, medinaMushafPage(2, 283))
        assertEquals(49, medinaMushafPage(2, 286))
    }

    @Test
    fun surah3MedinaMushafPageBoundaries() {
        assertEquals(50, medinaMushafPage(3, 1))
        assertEquals(50, medinaMushafPage(3, 9))
        assertEquals(51, medinaMushafPage(3, 10))
        assertEquals(76, medinaMushafPage(3, 195))
        assertEquals(76, medinaMushafPage(3, 200))
    }

    @Test
    fun cleanUthmaniDisplayRemovesBlackSignZeros() {
        val rawUthmani = "\u0623\u064f\u0648\u0652\u0644\u064e\u0640\u0670\u0626\u0650\u0643\u064e" // with U+06DF
        val withRoundZero = "أُوْلَـٰئِكَ".replace("ْ", "\u06df")
        val cleaned = withRoundZero.cleanUthmaniDisplay()

        assertFalse(cleaned.contains("\u06df"), "round zero must be stripped")
        assertFalse(cleaned.contains("\u06e0"), "rectangular zero must be stripped")
    }
}
