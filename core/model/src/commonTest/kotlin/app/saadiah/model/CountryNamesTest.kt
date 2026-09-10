package app.saadiah.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CountryNamesTest {
    @Test
    fun resolvesByTwoLetterIsoCode() {
        assertEquals(setOf(CountryCode("EG")), CountryNames.resolveCountryCodes("EG"))
        assertEquals(setOf(CountryCode("EG")), CountryNames.resolveCountryCodes("eg"))
        assertEquals(setOf(CountryCode("SA")), CountryNames.resolveCountryCodes("SA"))
        assertEquals(setOf(CountryCode("GB")), CountryNames.resolveCountryCodes("gb"))
        assertEquals(setOf(CountryCode("US")), CountryNames.resolveCountryCodes("us"))
    }

    @Test
    fun resolvesByEnglishName() {
        assertTrue(CountryNames.resolveCountryCodes("Egypt").contains(CountryCode("EG")))
        assertTrue(CountryNames.resolveCountryCodes("Saudi Arabia").contains(CountryCode("SA")))
        assertTrue(CountryNames.resolveCountryCodes("United Kingdom").contains(CountryCode("GB")))
        assertTrue(CountryNames.resolveCountryCodes("Yemen").contains(CountryCode("YE")))
        assertTrue(CountryNames.resolveCountryCodes("Canada").contains(CountryCode("CA")))
        assertTrue(CountryNames.resolveCountryCodes("Turkey").contains(CountryCode("TR")))
    }

    @Test
    fun resolvesByArabicName() {
        assertTrue(CountryNames.resolveCountryCodes("مصر").contains(CountryCode("EG")))
        assertTrue(CountryNames.resolveCountryCodes("السعودية").contains(CountryCode("SA")))
        assertTrue(CountryNames.resolveCountryCodes("اليمن").contains(CountryCode("YE")))
        assertTrue(CountryNames.resolveCountryCodes("بريطانيا").contains(CountryCode("GB")))
        assertTrue(CountryNames.resolveCountryCodes("المملكة المتحدة").contains(CountryCode("GB")))
        assertTrue(CountryNames.resolveCountryCodes("الإمارات").contains(CountryCode("AE")))
        assertTrue(CountryNames.resolveCountryCodes("تركيا").contains(CountryCode("TR")))
        assertTrue(CountryNames.resolveCountryCodes("المغرب").contains(CountryCode("MA")))
    }

    @Test
    fun resolvesArabicWithoutDefiniteArticle() {
        assertTrue(CountryNames.resolveCountryCodes("سعودية").contains(CountryCode("SA")))
        assertTrue(CountryNames.resolveCountryCodes("امارات").contains(CountryCode("AE")))
        assertTrue(CountryNames.resolveCountryCodes("يمن").contains(CountryCode("YE")))
        assertTrue(CountryNames.resolveCountryCodes("مغرب").contains(CountryCode("MA")))
        assertTrue(CountryNames.resolveCountryCodes("اردن").contains(CountryCode("JO")))
    }

    @Test
    fun resolvesArabicWithTashkeel() {
        assertTrue(CountryNames.resolveCountryCodes("مِصْرُ").contains(CountryCode("EG")))
        assertTrue(CountryNames.resolveCountryCodes("السَّعُودِيَّة").contains(CountryCode("SA")))
    }

    @Test
    fun resolvesByCommonAliases() {
        assertTrue(CountryNames.resolveCountryCodes("UK").contains(CountryCode("GB")))
        assertTrue(CountryNames.resolveCountryCodes("USA").contains(CountryCode("US")))
        assertTrue(CountryNames.resolveCountryCodes("UAE").contains(CountryCode("AE")))
        assertTrue(CountryNames.resolveCountryCodes("KSA").contains(CountryCode("SA")))
        assertTrue(CountryNames.resolveCountryCodes("England").contains(CountryCode("GB")))
    }

    @Test
    fun emptyOrBlankReturnsEmptySet() {
        assertTrue(CountryNames.resolveCountryCodes("").isEmpty())
        assertTrue(CountryNames.resolveCountryCodes("   ").isEmpty())
    }
}
