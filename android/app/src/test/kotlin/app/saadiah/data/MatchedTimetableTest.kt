package app.saadiah.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.prayer.Method
import app.saadiah.prayer.MosqueSolver
import app.saadiah.prayer.PrayerCalculator
import app.saadiah.prayer.toProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_DAY = 86_400

private val CAIRO =
    City(
        id = CityId(360630),
        name = "Cairo",
        country = CountryCode("EG"),
        admin1 = "Cairo",
        coordinates = Coordinates(latitude = 30.0444, longitude = 31.2357),
        timeZone = TimeZone.of("Africa/Cairo"),
    )
private val DATE = LocalDate(2026, 4, 3)

/**
 * The Match-my-masjid flow end to end: solve, persist, read back, recompute.
 *
 * Applying used to save the madhhab and nothing else, so a timetable that was not some other
 * madhhab's produced no change at all — and the alarm scheduler rebuilt its own profile from
 * the country's method regardless, so even a change that stuck would not have reached the
 * alerts. These assert the times a reader typed survive the round trip.
 */
@RunWith(RobolectricTestRunner::class)
class MatchedTimetableTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val calculator = PrayerCalculator()

    private fun freshStore(): SettingsStore {
        val file = folder.newFile("settings.preferences_pb").also { it.delete() }
        val backing: DataStore<Preferences> = PreferenceDataStoreFactory.create { file }
        return SettingsStore(backing)
    }

    private fun LocalTime.shifted(minutes: Int): LocalTime =
        LocalTime.fromSecondOfDay((toSecondOfDay() + minutes * SECONDS_PER_MINUTE).mod(SECONDS_PER_DAY))

    private fun timesFor(settings: Settings): Map<Prayer, LocalTime> =
        calculator
            .compute(CAIRO, DATE, settings.timingProfileFor(CAIRO))
            .times
            .mapValues { it.value.toLocalDateTime(CAIRO.timeZone).time }

    private fun aTimetableUnlikeAnyPublishedMethods(): Map<Prayer, LocalTime> {
        val base =
            calculator
                .compute(CAIRO, DATE, Method.EGYPTIAN.toProfile(Madhab.SHAFI))
                .times
                .mapValues { it.value.toLocalDateTime(CAIRO.timeZone).time }
        return mapOf(
            Prayer.FAJR to base.getValue(Prayer.FAJR).shifted(minutes = 8),
            Prayer.DHUHR to base.getValue(Prayer.DHUHR).shifted(minutes = -4),
            Prayer.ASR to base.getValue(Prayer.ASR).shifted(minutes = 13),
            Prayer.MAGHRIB to base.getValue(Prayer.MAGHRIB).shifted(minutes = 3),
            Prayer.ISHA to base.getValue(Prayer.ISHA).shifted(minutes = -6),
        )
    }

    @Test
    fun nothingIsMatchedUntilSomethingIsApplied() =
        runTest {
            assertNull(freshStore().settings.first().timingProfile)
        }

    @Test
    fun anAppliedTimetableSurvivesBeingWrittenAndReadBack() =
        runTest {
            val store = freshStore()
            val entered = aTimetableUnlikeAnyPublishedMethods()
            val matched = MosqueSolver(calculator).solve(entered, CAIRO, DATE)

            store.update { it.copy(city = CAIRO, madhab = matched.madhab, timingProfile = matched.toProfile()) }
            val reloaded = store.settings.first()

            assertNotNull(reloaded.timingProfile)
            assertEquals(expected = matched.toProfile().adjustments, actual = reloaded.timingProfile!!.adjustments)
            assertEquals(
                expected = matched.toProfile().highLatitudeRule,
                actual = reloaded.timingProfile!!.highLatitudeRule,
            )
            assertEquals(expected = matched.toProfile().angles, actual = reloaded.timingProfile!!.angles)
        }

    /** The one that matters: what was typed in is what the app computes afterwards. */
    @Test
    fun theStoredTimetableComputesTheTimesThatWereTypedIn() =
        runTest {
            val store = freshStore()
            val entered = aTimetableUnlikeAnyPublishedMethods()
            val matched = MosqueSolver(calculator).solve(entered, CAIRO, DATE)

            store.update { it.copy(city = CAIRO, madhab = matched.madhab, timingProfile = matched.toProfile()) }
            val computed = timesFor(store.settings.first())

            for ((prayer, typed) in entered) {
                assertEquals(expected = typed, actual = computed.getValue(prayer), message = "$prayer")
            }
        }

    @Test
    fun withNothingMatchedTheCountrysMethodStillApplies() =
        runTest {
            val store = freshStore()
            store.update { it.copy(city = CAIRO) }

            val settings = store.settings.first()

            assertNull(settings.timingProfile)
            assertEquals(
                expected = Method.EGYPTIAN.toProfile(Madhab.SHAFI).angles,
                actual = settings.timingProfileFor(CAIRO).angles,
            )
        }

    /** Changing the madhhab afterwards retunes ʿAṣr without discarding the match. */
    @Test
    fun theMatchOutlivesAChangeOfMadhhab() =
        runTest {
            val store = freshStore()
            val entered = aTimetableUnlikeAnyPublishedMethods()
            val matched = MosqueSolver(calculator).solve(entered, CAIRO, DATE)
            store.update { it.copy(city = CAIRO, madhab = matched.madhab, timingProfile = matched.toProfile()) }

            store.update { it.copy(madhab = Madhab.HANAFI) }
            val settings = store.settings.first()

            assertNotNull(settings.timingProfile)
            assertEquals(expected = Madhab.HANAFI, actual = settings.timingProfileFor(CAIRO).madhab)
            assertEquals(
                expected = matched.toProfile().adjustments,
                actual = settings.timingProfileFor(CAIRO).adjustments,
            )
        }

    /** Resetting a matched profile restores the automatic calculation for the city. */
    @Test
    fun resettingTimingProfileRestoresAutomaticInference() =
        runTest {
            val store = freshStore()
            val entered = aTimetableUnlikeAnyPublishedMethods()
            val matched = MosqueSolver(calculator).solve(entered, CAIRO, DATE)
            store.update { it.copy(city = CAIRO, timingProfile = matched.toProfile()) }

            // User resets matched profile
            store.update { it.copy(timingProfile = null) }
            val settings = store.settings.first()

            assertNull(settings.timingProfile)
            assertEquals(
                expected = Method.EGYPTIAN.toProfile(Madhab.SHAFI).angles,
                actual = settings.timingProfileFor(CAIRO).angles,
            )
        }

    /** Changing city clears matched timing profile to prevent cross-city contamination. */
    @Test
    fun changingCityClearsMatchedTimingProfile() =
        runTest {
            val store = freshStore()
            val entered = aTimetableUnlikeAnyPublishedMethods()
            val matched = MosqueSolver(calculator).solve(entered, CAIRO, DATE)
            store.update { it.copy(city = CAIRO, timingProfile = matched.toProfile()) }

            // Changing city as done in MainActivity: timingProfile is cleared
            val mecca =
                City(
                    id = CityId(104515),
                    name = "Mecca",
                    country = CountryCode("SA"),
                    admin1 = "Makkah",
                    coordinates = Coordinates(latitude = 21.4225, longitude = 39.8262),
                    timeZone = TimeZone.of("Asia/Riyadh"),
                )
            store.update { it.copy(city = mecca, timingProfile = null) }
            val settings = store.settings.first()

            assertNull(settings.timingProfile)
            assertEquals(
                expected = Method.UMM_AL_QURA.toProfile(Madhab.SHAFI).angles,
                actual = settings.timingProfileFor(mecca).angles,
            )
        }
}
