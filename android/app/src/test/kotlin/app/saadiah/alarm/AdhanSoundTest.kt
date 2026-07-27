package app.saadiah.alarm

import android.content.Context
import app.saadiah.model.AdhanSound
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * A notification channel's sound is fixed when the channel is created, so the choice of adhān
 * is expressed as a choice of channel. These hold the two halves of that: three distinct
 * channels, and three raw resources that survive release shrinking to sound from them.
 */
@RunWith(RobolectricTestRunner::class)
class AdhanSoundTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun everyAdhanHasItsOwnChannel() {
        val channels = AdhanSound.entries.map { prayerChannelFor(it) }

        assertEquals(expected = AdhanSound.entries.size, actual = channels.toSet().size, message = "$channels")
    }

    /** The default keeps the original id, so an existing install is not silently re-channelled. */
    @Test
    fun theDefaultStaysOnTheChannelThatAlreadyShipped() {
        assertEquals(expected = PRAYER_CHANNEL_ID, actual = prayerChannelFor(AdhanSound.DEFAULT))
    }

    @Test
    fun bothRecordingsAreBundled() {
        for (name in listOf("adhan_short", "adhan_long")) {
            val id = context.resources.getIdentifier(name, "raw", context.packageName)

            assertTrue(id != 0, "$name is not bundled")
            assertNotNull(
                context.resources
                    .openRawResource(id)
                    .use { it.readBytes() }
                    .takeIf { it.isNotEmpty() },
            )
        }
    }

    @Test
    fun everyChannelIsCreatedBeforeAnyAlertNeedsIt() {
        ensureChannels(context)
        val manager = context.getSystemService(android.app.NotificationManager::class.java)

        for (sound in AdhanSound.entries) {
            assertNotNull(manager.getNotificationChannel(prayerChannelFor(sound)), "$sound has no channel")
        }
        assertNotNull(manager.getNotificationChannel(READING_CHANNEL_ID))
    }

    @Test
    fun theRecordingsCarryAnAlarmUsageSoTheyAreHeard() {
        ensureChannels(context)
        val manager = context.getSystemService(android.app.NotificationManager::class.java)

        for (sound in listOf(AdhanSound.SHORT, AdhanSound.LONG)) {
            val channel = manager.getNotificationChannel(prayerChannelFor(sound))

            assertNotNull(channel.sound, "$sound has no sound set")
            assertEquals(
                expected = android.media.AudioAttributes.USAGE_ALARM,
                actual = channel.audioAttributes?.usage,
                message = "$sound should sound like an alarm, not a notification",
            )
        }
    }
}
