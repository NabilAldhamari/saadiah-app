package app.saadiah.alarm

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import app.saadiah.model.AlarmKind
import app.saadiah.model.Prayer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * An alarm that fires and says nothing is worse than one that was never armed: the slot was
 * spent and the reader was told nothing. Every kind that gets a slot is checked here.
 */
@RunWith(RobolectricTestRunner::class)
class AlarmReceiverTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun posted(): List<Notification> = Shadows.shadowOf(notifications).allNotifications

    private fun textOfLastPost(): String? = posted().lastOrNull()?.extras?.getString(Notification.EXTRA_TEXT)

    private fun fire(
        kind: AlarmKind,
        prayer: Prayer = Prayer.FAJR,
        leadMinutes: Long = 0L,
    ) {
        AlarmReceiver().onReceive(
            context,
            Intent(context, AlarmReceiver::class.java)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .putExtra(EXTRA_KIND, kind.name)
                .putExtra(EXTRA_LEAD_MINUTES, leadMinutes),
        )
    }

    @Before
    fun startWithNothingPosted() {
        notifications.cancelAll()
    }

    @Test
    fun aPrayerTimeIsAnnounced() {
        fire(AlarmKind.AT_TIME)

        assertEquals(expected = 1, actual = posted().size, message = "the prayer time was not announced")
    }

    @Test
    fun aPreAlertSaysHowLongIsLeft() {
        fire(AlarmKind.PRE_ALERT, prayer = Prayer.DHUHR, leadMinutes = 10L)

        assertEquals(expected = 1, actual = posted().size, message = "the pre-alert fired silently")
        assertEquals(
            expected = "Dhuhr begins in 10 minutes.",
            actual = textOfLastPost(),
            message = "a pre-alert must say how long is left, not just that something is due",
        )
    }

    @Test
    fun aPreAlertSpellsOutAnHourRatherThanAbbreviatingIt() {
        fire(AlarmKind.PRE_ALERT, prayer = Prayer.ASR, leadMinutes = 90L)

        assertEquals(expected = "Asr begins in 1 hour 30 minutes.", actual = textOfLastPost())
    }

    @Test
    fun anEndOfWindowWarningSaysTheTimeIsClosing() {
        fire(AlarmKind.END_OF_WINDOW, prayer = Prayer.ISHA, leadMinutes = 20L)

        assertEquals(expected = "The time for Isha ends in 20 minutes.", actual = textOfLastPost())
    }

    @Test
    fun aReArmIsSilent() {
        fire(AlarmKind.RE_ARM)

        assertTrue(posted().isEmpty(), "a re-arm exists to refill the horizon, not to interrupt anyone")
    }

    @Test
    fun everyAnnouncementNamesThePrayerInArabicAndEnglish() {
        fire(AlarmKind.AT_TIME, prayer = Prayer.MAGHRIB)

        val title = posted().last().extras?.getString(Notification.EXTRA_TITLE)

        assertEquals(expected = "المغرب — Maghrib", actual = title)
    }
}
