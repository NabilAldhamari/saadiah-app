package app.saadiah.alarm

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import app.saadiah.data.SettingsStore
import app.saadiah.model.City
import app.saadiah.model.EXTENDED_CITIES
import app.saadiah.ui.EnglishStrings
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class TravelDetectorTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val london: City = EXTENDED_CITIES.first { it.name == "London" }
    private val newYork: City = EXTENDED_CITIES.first { it.name == "New York City" }
    private val dubai: City = EXTENDED_CITIES.first { it.name == "Dubai" }

    // Fixed test instants
    private val winterInstant = Instant.parse("2026-01-15T12:00:00Z")
    private val summerInstant = Instant.parse("2026-07-15T12:00:00Z")

    @Before
    fun setUp() {
        notifications.cancelAll()
    }

    @Test
    fun sameZoneNoTravelDetected() {
        val result =
            TravelDetector.evaluate(
                systemZone = TimeZone.of("Europe/London"),
                networkCountry = "GB",
                currentCity = london,
                now = winterInstant,
            )

        assertFalse(result.isTraveling)
        assertNull(result.suggestedCity)
    }

    @Test
    fun seasonalDaylightSavingTimeDoesNotTriggerFalseAlert() {
        // In summer, London enters British Summer Time (BST, UTC+1).
        // The device clock shifts by 1 hour, but the IANA zone ID remains "Europe/London".
        val result =
            TravelDetector.evaluate(
                systemZone = TimeZone.of("Europe/London"),
                networkCountry = "GB",
                currentCity = london,
                now = summerInstant,
            )

        assertFalse(
            result.isTraveling,
            "Seasonal DST shift must not trigger a travel alert when the IANA zone ID is identical",
        )
        assertNull(result.suggestedCity)
    }

    @Test
    fun travelFromLondonToNewYorkDetected() {
        val result =
            TravelDetector.evaluate(
                systemZone = TimeZone.of("America/New_York"),
                networkCountry = "US",
                currentCity = london,
                now = summerInstant,
            )

        assertTrue(result.isTraveling)
        assertNotNull(result.suggestedCity)
        assertEquals(expected = "New York City", actual = result.suggestedCity?.name)
    }

    @Test
    fun travelFromLondonToDubaiDetected() {
        val result =
            TravelDetector.evaluate(
                systemZone = TimeZone.of("Asia/Dubai"),
                networkCountry = "AE",
                currentCity = london,
                now = winterInstant,
            )

        assertTrue(result.isTraveling)
        assertNotNull(result.suggestedCity)
        assertEquals(expected = "Dubai", actual = result.suggestedCity?.name)
    }

    @Test
    fun domesticTravelAcrossTimezonesDetected() {
        // Traveling from NYC to LA within the US (country unchanged, offset changes 3 hours)
        val result =
            TravelDetector.evaluate(
                systemZone = TimeZone.of("America/Los_Angeles"),
                networkCountry = "US",
                currentCity = newYork,
                now = winterInstant,
            )

        assertTrue(result.isTraveling)
        assertNotNull(result.suggestedCity)
        assertEquals(expected = "Los Angeles", actual = result.suggestedCity?.name)
    }

    @Test
    fun sameOffsetDifferentCountryTravelDetected() {
        // Traveling from London (GB) to Lisbon (PT)
        val result =
            TravelDetector.evaluate(
                systemZone = TimeZone.of("Europe/Lisbon"),
                networkCountry = "PT",
                currentCity = london,
                now = winterInstant,
            )

        assertTrue(result.isTraveling)
    }

    @Test
    fun postAndDismissTravelNotificationWorks() {
        val detection =
            TravelDetection(
                isTraveling = true,
                detectedZone = TimeZone.of("Asia/Dubai"),
                detectedCountry = "AE",
                suggestedCity = dubai,
            )

        postTravelNotification(context, detection, london, EnglishStrings)

        val posted = Shadows.shadowOf(notifications).allNotifications
        assertEquals(expected = 1, actual = posted.size)

        val notification = posted.first()
        assertEquals(
            expected = EnglishStrings.travelNotificationTitle,
            actual = notification.extras.getString(Notification.EXTRA_TITLE),
        )
        assertTrue(notification.extras.getString(Notification.EXTRA_TEXT)?.contains("Dubai") == true)

        dismissTravelNotification(context)
        val remaining = Shadows.shadowOf(notifications).allNotifications
        assertEquals(expected = 0, actual = remaining.size)
    }

    @Test
    fun systemChangeReceiverPostsNotificationOnTimezoneChanged() {
        // Set user's city in settings to London
        runBlocking {
            SettingsStore(context).update { it.copy(city = london) }
        }

        // Simulate device timezone being Asia/Dubai
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Dubai"))

        val receiver = SystemChangeReceiver()
        receiver.onReceive(context, Intent(Intent.ACTION_TIMEZONE_CHANGED))

        val posted = Shadows.shadowOf(notifications).allNotifications
        assertTrue(posted.isNotEmpty(), "Travel notification should be posted on ACTION_TIMEZONE_CHANGED")
        val travelNotification = posted.firstOrNull { it.channelId == TRAVEL_CHANNEL_ID }
        assertNotNull(travelNotification, "Notification on TRAVEL_CHANNEL_ID should be posted")
    }
}
