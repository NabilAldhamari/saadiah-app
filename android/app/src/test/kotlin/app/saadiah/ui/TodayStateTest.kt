package app.saadiah.ui

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.CombineMode
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.Tradition
import app.saadiah.prayer.Method
import app.saadiah.prayer.PrayerCalculator
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

private val CAIRO =
    City(
        id = CityId(value = 1),
        name = "Cairo",
        country = CountryCode(value = "EG"),
        admin1 = "Cairo",
        coordinates = Coordinates(latitude = 30.0444, longitude = 31.2357),
        timeZone = TimeZone.of("Africa/Cairo"),
    )
private val DATE = LocalDate(2024, 3, 11)
private const val MINUTES_IN_DAY = 1440
private const val PROBE_STEP = 17

/** The clock is an input, so every boundary can be stood on exactly. */
class TodayStateTest {
    private val profile = Method.EGYPTIAN.toProfile(madhab = Madhab.SHAFI)
    private val combined = profile.copy(combineMode = CombineMode.ZUHRAYN_ISHAAYN)
    private val timings = PrayerCalculator().compute(CAIRO, DATE, profile)

    private fun stateAt(
        now: Instant,
        withProfile: app.saadiah.model.TimingProfile = profile,
    ) = todayState(CAIRO, withProfile, Tradition.SUNNI, now, EnglishStrings)

    @Test
    fun beforeFajrTheNextPrayerIsFajr() {
        val state = stateAt(timings[Prayer.FAJR] - 1.minutes)

        assertEquals(expected = "Fajr", actual = state.nextPrayerLatin)
    }

    @Test
    fun atEachBoundaryTheNextPrayerAdvances() {
        val expected =
            listOf(
                Prayer.FAJR to "Dhuhr",
                Prayer.DHUHR to "Asr",
                Prayer.ASR to "Maghrib",
                Prayer.MAGHRIB to "Isha",
            )
        for ((passed, next) in expected) {
            assertEquals(
                expected = next,
                actual = stateAt(timings[passed]).nextPrayerLatin,
                message = "standing exactly on $passed",
            )
        }
    }

    @Test
    fun aMinuteBeforeAPrayerItIsStillTheNextOne() {
        assertEquals(expected = "Asr", actual = stateAt(timings[Prayer.ASR] - 1.minutes).nextPrayerLatin)
    }

    @Test
    fun afterIshaTheNextPrayerIsTomorrowsFajr() {
        val state = stateAt(timings[Prayer.ISHA] + 1.minutes)

        assertEquals(expected = "Fajr", actual = state.nextPrayerLatin)
    }

    /** The highlight answers the same question the hero does, so the two never disagree. */
    @Test
    fun theHighlightedRowIsTheOneTheHeroNames() {
        for (passed in listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB)) {
            val state = stateAt(timings[passed] + 1.minutes)

            assertEquals(
                expected = listOf(state.nextPrayerLatin),
                actual = state.rows.filter { it.isNext }.map { it.name },
                message = "a minute after $passed",
            )
        }
    }

    @Test
    fun theHighlightMovesOnAtEveryBoundary() {
        val marked =
            listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB).map { passed ->
                stateAt(timings[passed] + 1.minutes).rows.single { it.isNext }.name
            }

        assertEquals(expected = listOf("Dhuhr", "Asr", "Maghrib", "Isha"), actual = marked)
    }

    @Test
    fun beforeFajrTheFajrRowIsTheHighlightedOne() {
        val state = stateAt(timings[Prayer.FAJR] - 1.minutes)

        assertEquals(expected = listOf("Fajr"), actual = state.rows.filter { it.isNext }.map { it.name })
    }

    @Test
    fun afterIshaTheHighlightReturnsToFajrForTomorrow() {
        val state = stateAt(timings[Prayer.ISHA] + 1.minutes)

        assertEquals(expected = listOf("Fajr"), actual = state.rows.filter { it.isNext }.map { it.name })
    }

    @Test
    fun exactlyOneRowIsEverHighlighted() {
        for (minutes in 0 until MINUTES_IN_DAY step PROBE_STEP) {
            val state = stateAt(timings[Prayer.FAJR] + minutes.minutes)

            assertEquals(expected = 1, actual = state.rows.count { it.isNext }, message = "at +$minutes")
        }
    }

    @Test
    fun remainingTimeIsSpelledOutInFull() {
        val state = stateAt(timings[Prayer.ASR] - 134.minutes)

        assertEquals(expected = "in 2 hours 14 minutes", actual = state.remaining)
    }

    @Test
    fun aSinglePrayerListsFivePrayers() {
        val state = stateAt(timings[Prayer.FAJR])

        assertEquals(
            expected = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"),
            actual = state.rows.map { it.name },
        )
    }

    @Test
    fun combiningCollapsesTheListToThreeRows() {
        val state = stateAt(timings[Prayer.FAJR], withProfile = combined)

        assertEquals(expected = listOf("Fajr", "Ẓuhrayn", "ʿIshāʾayn"), actual = state.rows.map { it.name })
    }

    @Test
    fun theCombinedRowsKeepTheOpeningPrayersTime() {
        val state = stateAt(timings[Prayer.FAJR], withProfile = combined)
        val plain = stateAt(timings[Prayer.FAJR])

        assertEquals(expected = plain.rows.single { it.name == "Dhuhr" }.time, actual = state.rows[1].time)
        assertEquals(expected = plain.rows.single { it.name == "Maghrib" }.time, actual = state.rows[2].time)
    }

    @Test
    fun theCombinedHeroNamesThePairInBothScripts() {
        val state = stateAt(timings[Prayer.DHUHR] - 1.minutes, withProfile = combined)

        assertEquals(expected = "Ẓuhrayn", actual = state.nextPrayerLatin)
        assertEquals(expected = "الظهرين", actual = state.nextPrayerArabic)
    }

    @Test
    fun combiningNeverDependsOnTradition() {
        for (tradition in Tradition.entries) {
            val rows = todayState(CAIRO, combined, tradition, timings[Prayer.FAJR], EnglishStrings).rows

            assertEquals(expected = 3, actual = rows.size, message = "$tradition")
        }
    }
}
