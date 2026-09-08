package app.saadiah.audio

import android.content.Context
import app.saadiah.model.Reciter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AudioDownloadManagerTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @Before
    fun cleanup() {
        for (reciter in Reciter.entries) {
            AudioDownloadManager.deleteReciter(context, reciter)
        }
        AudioDownloadManager.cancelDownload()
    }

    @Test
    fun surahNotDownloadedInitially() {
        assertFalse(AudioDownloadManager.isSurahDownloaded(context, 2))
        assertFalse(AudioDownloadManager.isSurahDownloaded(context, 3))
        assertNull(AudioDownloadManager.surahFileFor(context, 2, 1))
    }

    @Test
    fun detectsDownloadedSurahForDefaultReciter() {
        val dir = AudioDownloadManager.quranAudioDir(context, Reciter.HUSARI_MUJAWWAD)
        val first = File(dir, "002001.mp3")
        val last = File(dir, "002286.mp3")
        first.writeBytes(byteArrayOf(1, 2, 3))
        last.writeBytes(byteArrayOf(4, 5, 6))

        assertTrue(AudioDownloadManager.isSurahDownloaded(context, Reciter.HUSARI_MUJAWWAD, 2))
        assertTrue(AudioDownloadManager.isSurahDownloaded(context, 2))
        val file = AudioDownloadManager.surahFileFor(context, 2, 1)
        assertNotNull(file)
        assertEquals(first.absolutePath, file.absolutePath)
    }

    @Test
    fun reciterDirectoryScopingKeepsDownloadsSeparated() {
        val husariDir = AudioDownloadManager.quranAudioDir(context, Reciter.HUSARI_MUJAWWAD)
        val minshawiDir = AudioDownloadManager.quranAudioDir(context, Reciter.MINSHAWI_MUJAWWAD)

        File(husariDir, "002001.mp3").writeBytes(byteArrayOf(1))
        File(husariDir, "002286.mp3").writeBytes(byteArrayOf(1))

        assertTrue(AudioDownloadManager.isSurahDownloaded(context, Reciter.HUSARI_MUJAWWAD, 2))
        assertFalse(AudioDownloadManager.isSurahDownloaded(context, Reciter.MINSHAWI_MUJAWWAD, 2))

        AudioDownloadManager.deleteReciter(context, Reciter.HUSARI_MUJAWWAD)
        assertFalse(AudioDownloadManager.isSurahDownloaded(context, Reciter.HUSARI_MUJAWWAD, 2))
    }

    @Test
    fun storageCalculationReportsAccurateBytes() {
        val husariDir = AudioDownloadManager.quranAudioDir(context, Reciter.HUSARI_MUJAWWAD)
        File(husariDir, "002001.mp3").writeBytes(ByteArray(1024))
        File(husariDir, "002002.mp3").writeBytes(ByteArray(2048))

        val bytes = AudioDownloadManager.getStorageUsedBytes(context, Reciter.HUSARI_MUJAWWAD)
        assertEquals(3072L, bytes)

        val total = AudioDownloadManager.getTotalStorageUsedBytes(context)
        assertTrue(total >= 3072L)
    }

    @Test
    fun deleteSurahRemovesOnlyTargetSurah() {
        val dir = AudioDownloadManager.quranAudioDir(context, Reciter.HUSARI_MUJAWWAD)
        File(dir, "002001.mp3").writeBytes(byteArrayOf(1))
        File(dir, "002286.mp3").writeBytes(byteArrayOf(1))
        File(dir, "003001.mp3").writeBytes(byteArrayOf(1))
        File(dir, "003200.mp3").writeBytes(byteArrayOf(1))

        assertTrue(AudioDownloadManager.isSurahDownloaded(context, 2))
        assertTrue(AudioDownloadManager.isSurahDownloaded(context, 3))

        AudioDownloadManager.deleteSurah(context, 2)
        assertFalse(AudioDownloadManager.isSurahDownloaded(context, 2))
        assertTrue(AudioDownloadManager.isSurahDownloaded(context, 3))
    }

    @Test
    fun cancelDownloadResetsState() {
        AudioDownloadManager.cancelDownload()
        assertEquals(DownloadState.Idle, AudioDownloadManager.downloadState.value)
    }
}
