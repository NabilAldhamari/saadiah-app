package app.saadiah.prayer

import app.saadiah.model.CountryCode
import app.saadiah.model.Madhab
import kotlin.test.Test
import kotlin.test.assertEquals

class MethodInferenceTest {
    @Test
    fun saudiArabiaUsesUmmAlQura() {
        assertEquals(
            expected = Method.UMM_AL_QURA.toProfile(madhab = Madhab.SHAFI),
            actual = inferProfile(CountryCode(value = "SA")),
        )
    }

    @Test
    fun southAsiaUsesKarachiWithHanafiAsr() {
        for (code in listOf("PK", "IN", "BD")) {
            assertEquals(
                expected = Method.KARACHI.toProfile(madhab = Madhab.HANAFI),
                actual = inferProfile(CountryCode(value = code)),
                message = code,
            )
        }
    }

    @Test
    fun turkeyUsesDiyanet() {
        assertEquals(
            expected = Method.DIYANET.toProfile(madhab = Madhab.SHAFI),
            actual = inferProfile(CountryCode(value = "TR")),
        )
    }

    @Test
    fun egyptUsesEgyptian() {
        assertEquals(
            expected = Method.EGYPTIAN.toProfile(madhab = Madhab.SHAFI),
            actual = inferProfile(CountryCode(value = "EG")),
        )
    }

    @Test
    fun iranUsesTehran() {
        assertEquals(
            expected = Method.TEHRAN.toProfile(madhab = Madhab.SHAFI),
            actual = inferProfile(CountryCode(value = "IR")),
        )
    }

    @Test
    fun northAmericaUsesIsna() {
        for (code in listOf("US", "CA")) {
            assertEquals(
                expected = Method.NORTH_AMERICA.toProfile(madhab = Madhab.SHAFI),
                actual = inferProfile(CountryCode(value = code)),
                message = code,
            )
        }
    }

    @Test
    fun europeUsesMuslimWorldLeague() {
        for (code in listOf("GB", "DE", "FR")) {
            assertEquals(
                expected = Method.MUSLIM_WORLD_LEAGUE.toProfile(madhab = Madhab.SHAFI),
                actual = inferProfile(CountryCode(value = code)),
                message = code,
            )
        }
    }

    @Test
    fun unknownCountryFallsBackToMuslimWorldLeague() {
        assertEquals(
            expected = Method.MUSLIM_WORLD_LEAGUE.toProfile(madhab = Madhab.SHAFI),
            actual = inferProfile(CountryCode(value = "ZZ")),
        )
    }
}
