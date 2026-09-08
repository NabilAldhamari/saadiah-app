package app.saadiah.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.CombineMode
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import app.saadiah.model.Tradition
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes

private val STORED_CITY =
    City(
        id = CityId(2643743),
        name = "London",
        country = CountryCode("GB"),
        admin1 = "England",
        coordinates = Coordinates(latitude = 51.50853, longitude = -0.12574),
        timeZone = TimeZone.of("Europe/London"),
        arabicName = "لندن",
    )

@RunWith(RobolectricTestRunner::class)
class SettingsStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val storeOverItsOwnFreshFile: SettingsStore by lazy {
        val file = folder.newFile("settings.preferences_pb").also { it.delete() }
        val backing: DataStore<Preferences> = PreferenceDataStoreFactory.create { file }
        SettingsStore(backing)
    }

    @Test
    fun theFirstReadIsTheDefaults() =
        runTest {
            val settings = storeOverItsOwnFreshFile.settings.first()

            assertNull(settings.tradition, "tradition must stay unchosen until the user says")
            assertNull(settings.madhab)
            assertNull(settings.city)
            assertEquals(expected = CombineMode.NONE, actual = settings.combineMode)
            assertEquals(expected = 5, actual = settings.enabledPrayers.size)
        }

    @Test
    fun aWriteIsObservableOnTheNextRead() =
        runTest {
            storeOverItsOwnFreshFile.update { it.copy(tradition = Tradition.SUNNI, madhab = Madhab.HANAFI) }

            val settings = storeOverItsOwnFreshFile.settings.first()

            assertEquals(expected = Tradition.SUNNI, actual = settings.tradition)
            assertEquals(expected = Madhab.HANAFI, actual = settings.madhab)
        }

    @Test
    fun everyFieldSurvivesARoundTrip() =
        runTest {
            val chosen =
                Settings(
                    tradition = Tradition.SUNNI,
                    madhab = Madhab.SHAFI,
                    city = STORED_CITY,
                    combineMode = CombineMode.ZUHRAYN_ISHAAYN,
                    enabledPrayers = setOf(Prayer.FAJR, Prayer.MAGHRIB),
                    preAlert = 15.minutes,
                    endOfWindow = 20.minutes,
                    fastingReminder = app.saadiah.model.FastingReminderCadence.ALL_NAFILAH,
                    afterPrayerReminder = app.saadiah.model.AfterPrayerReminderDelay.TEN_MINUTES,
                    showHomeDuas = true,
                    quranViewMode = app.saadiah.model.QuranViewMode.READING,
                    reciter = app.saadiah.model.Reciter.MINSHAWI_MUJAWWAD,
                )

            storeOverItsOwnFreshFile.update { chosen }

            assertEquals(expected = chosen, actual = storeOverItsOwnFreshFile.settings.first())
        }

    @Test
    fun clearingAValueRestoresItToUnchosen() =
        runTest {
            storeOverItsOwnFreshFile.update { it.copy(tradition = Tradition.SUNNI, preAlert = 10.minutes) }
            storeOverItsOwnFreshFile.update { it.copy(tradition = null, preAlert = null) }

            val settings = storeOverItsOwnFreshFile.settings.first()

            assertNull(settings.tradition, "an unset value must not linger from an earlier write")
            assertNull(settings.preAlert)
        }

    @Test
    fun anUpdateSeesTheValueAlreadyStored() =
        runTest {
            storeOverItsOwnFreshFile.update { it.copy(madhab = Madhab.HANAFI) }
            storeOverItsOwnFreshFile.update { it.copy(tradition = Tradition.SUNNI) }

            val settings = storeOverItsOwnFreshFile.settings.first()

            assertEquals(expected = Madhab.HANAFI, actual = settings.madhab, "the second write erased the first")
            assertEquals(expected = Tradition.SUNNI, actual = settings.tradition)
        }
}
