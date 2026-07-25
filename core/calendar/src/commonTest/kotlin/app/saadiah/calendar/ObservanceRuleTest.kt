package app.saadiah.calendar

import app.saadiah.model.HijriDate
import app.saadiah.model.ObservanceKind
import app.saadiah.model.Tradition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val YEAR = 1445
private const val MUHARRAM = 1
private const val RAMADAN = 9
private const val SHAWWAL = 10
private const val DHU_AL_HIJJAH = 12

class ObservanceRuleTest {
    private fun kindsOn(
        month: Int,
        day: Int,
        tradition: Tradition,
    ): Set<ObservanceKind> =
        observancesOn(HijriDate(year = YEAR, month = month, day = day), tradition).map { it.kind }.toSet()

    private fun idsOn(
        month: Int,
        day: Int,
        tradition: Tradition,
    ): Set<Observance> =
        observancesOn(HijriDate(year = YEAR, month = month, day = day), tradition).map { it.observance }.toSet()

    @Test
    fun everyRuleIsTagged() {
        assertTrue(OBSERVANCE_RULES.isNotEmpty())
        for (rule in OBSERVANCE_RULES) {
            assertTrue(rule.traditions.isNotEmpty(), "untagged rule: ${rule.observance} ${rule.kind}")
        }
    }

    private fun everyDayOfTheYear(): List<HijriDate> =
        (1..12).flatMap { month -> (1..29).map { day -> HijriDate(year = YEAR, month = month, day = day) } }

    private fun leaksFor(tradition: Tradition): List<Observance> =
        everyDayOfTheYear()
            .flatMap { observancesOn(it, tradition) }
            .filterNot { tradition in it.traditions }
            .map { it.observance }

    @Test
    fun noRuleLeaksAcrossTraditions() {
        for (tradition in Tradition.entries) {
            assertEquals(expected = emptyList(), actual = leaksFor(tradition), message = "$tradition")
        }
    }

    @Test
    fun ramadanIsAnObligatoryFastForBothTraditions() {
        for (tradition in Tradition.entries) {
            for (day in 1..29) {
                assertTrue(
                    ObservanceKind.OBLIGATORY_FAST in kindsOn(RAMADAN, day, tradition),
                    "$tradition ramadan day $day",
                )
            }
        }
    }

    @Test
    fun bothEidsAreMarkedAndFastingIsProhibited() {
        for (tradition in Tradition.entries) {
            val fitr = kindsOn(SHAWWAL, 1, tradition)
            val adha = kindsOn(DHU_AL_HIJJAH, 10, tradition)

            assertTrue(ObservanceKind.EID in fitr && ObservanceKind.PROHIBITED_FAST in fitr, "$tradition eid al-fitr")
            assertTrue(ObservanceKind.EID in adha && ObservanceKind.PROHIBITED_FAST in adha, "$tradition eid al-adha")
        }
    }

    @Test
    fun tashriqDaysProhibitFasting() {
        for (tradition in Tradition.entries) {
            for (day in 11..13) {
                assertTrue(
                    ObservanceKind.PROHIBITED_FAST in kindsOn(DHU_AL_HIJJAH, day, tradition),
                    "$tradition tashriq day $day",
                )
            }
        }
    }

    @Test
    fun ayyamAlBidIsSuppressedOnTashriqButNotAfterIt() {
        for (tradition in Tradition.entries) {
            assertFalse(
                ObservanceKind.RECOMMENDED_FAST in kindsOn(DHU_AL_HIJJAH, 13, tradition),
                "$tradition ayyam al-bid must not be recommended on a tashriq day",
            )
            assertTrue(Observance.AYYAM_AL_BID in idsOn(DHU_AL_HIJJAH, 14, tradition), "$tradition 14")
            assertTrue(Observance.AYYAM_AL_BID in idsOn(DHU_AL_HIJJAH, 15, tradition), "$tradition 15")
        }
    }

    @Test
    fun ayyamAlBidRecursEveryMonth() {
        for (month in 1..12) {
            if (month == DHU_AL_HIJJAH || month == RAMADAN) continue
            for (day in 13..15) {
                assertTrue(
                    Observance.AYYAM_AL_BID in idsOn(month, day, Tradition.SUNNI),
                    "month $month day $day",
                )
            }
        }
    }

    @Test
    fun ayyamAlBidIsNotOfferedSeparatelyDuringRamadan() {
        for (tradition in Tradition.entries) {
            for (day in 13..15) {
                assertFalse(
                    ObservanceKind.RECOMMENDED_FAST in kindsOn(RAMADAN, day, tradition),
                    "$tradition ramadan day $day is already an obligatory fast",
                )
            }
        }
    }

    @Test
    fun arafahIsRecommendedForBothTraditions() {
        for (tradition in Tradition.entries) {
            assertTrue(Observance.ARAFAH in idsOn(DHU_AL_HIJJAH, 9, tradition), "$tradition")
        }
    }

    @Test
    fun ashuraDivergesBetweenTraditions() {
        assertTrue(Observance.ASHURA in idsOn(MUHARRAM, 10, Tradition.SUNNI))
        assertFalse(
            Observance.ASHURA in idsOn(MUHARRAM, 10, Tradition.TWELVER),
            "ashura must not be offered as a recommended fast to a twelver user",
        )
        assertFalse(ObservanceKind.RECOMMENDED_FAST in kindsOn(MUHARRAM, 10, Tradition.TWELVER))
    }

    @Test
    fun sixOfShawwalFollowsEidForSunniOnly() {
        for (day in 2..7) {
            assertTrue(Observance.SIX_OF_SHAWWAL in idsOn(SHAWWAL, day, Tradition.SUNNI), "day $day")
            assertFalse(Observance.SIX_OF_SHAWWAL in idsOn(SHAWWAL, day, Tradition.TWELVER), "day $day")
        }
        assertFalse(Observance.SIX_OF_SHAWWAL in idsOn(SHAWWAL, 1, Tradition.SUNNI))
    }

    private fun assertHijamaDay(
        month: Int,
        day: Int,
        tradition: Tradition,
        expected: Boolean,
    ) {
        assertEquals(
            expected = expected,
            actual = ObservanceKind.RECOMMENDED_HIJAMA in kindsOn(month, day, tradition),
            message = "$tradition month $month day $day",
        )
    }

    private fun assertHijamaMonth(
        month: Int,
        tradition: Tradition,
    ) {
        listOf(17, 19, 21).forEach { assertHijamaDay(month, it, tradition, expected = true) }
        listOf(16, 18, 20, 22).forEach { assertHijamaDay(month, it, tradition, expected = false) }
    }

    @Test
    fun hijamaFallsOnSeventeenNineteenAndTwentyOne() {
        for (tradition in Tradition.entries) {
            for (month in 1..12) {
                assertHijamaMonth(month, tradition)
            }
        }
    }
}
