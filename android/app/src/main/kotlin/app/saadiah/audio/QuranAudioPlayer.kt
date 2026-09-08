package app.saadiah.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.saadiah.content.Ayah
import app.saadiah.model.Reciter

class QuranAudioPlayer(
    private val context: Context? = null,
    var reciter: Reciter = Reciter.HUSARI_MUJAWWAD,
) {
    private var mediaPlayer: MediaPlayer? = null

    var currentAyah by mutableStateOf<Ayah?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun audioUrlFor(
        ayah: Ayah,
        currentReciter: Reciter = reciter,
    ): String {
        val suraPadded = ayah.sura.toString().padStart(3, '0')
        val ayahPadded = ayah.number.toString().padStart(3, '0')
        return "https://everyayah.com/data/${currentReciter.everyAyahFolder}/$suraPadded$ayahPadded.mp3"
    }

    fun playAyah(
        ayah: Ayah,
        currentReciter: Reciter = reciter,
        onComplete: (() -> Unit)? = null,
    ) {
        if (currentAyah == ayah && isPlaying) {
            pause()
            return
        }
        if (currentAyah == ayah && mediaPlayer != null) {
            mediaPlayer?.start()
            isPlaying = true
            return
        }

        stop()
        currentAyah = ayah
        isLoading = true

        runCatching {
            val player = MediaPlayer()
            val suraPadded = ayah.sura.toString().padStart(3, '0')
            val ayahPadded = ayah.number.toString().padStart(3, '0')
            val localFile =
                context?.let { ctx ->
                    AudioDownloadManager.surahFileFor(ctx, currentReciter, ayah.sura, ayah.number)
                }

            if (localFile != null && localFile.exists() && localFile.length() > 0) {
                player.setDataSource(localFile.absolutePath)
            } else {
                val url = "https://everyayah.com/data/${currentReciter.everyAyahFolder}/$suraPadded$ayahPadded.mp3"
                player.setDataSource(url)
            }

            player.setOnPreparedListener {
                isLoading = false
                it.start()
                this@QuranAudioPlayer.isPlaying = true
            }
            player.setOnCompletionListener {
                this@QuranAudioPlayer.isPlaying = false
                onComplete?.invoke()
            }
            player.setOnErrorListener { _, _, _ ->
                isLoading = false
                stop()
                true
            }
            player.prepareAsync()
            mediaPlayer = player
        }.onFailure {
            isLoading = false
            stop()
        }
    }

    fun pause() {
        runCatching {
            mediaPlayer?.pause()
            isPlaying = false
        }
    }

    fun resume() {
        runCatching {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    fun stop() {
        runCatching {
            mediaPlayer?.reset()
            mediaPlayer?.release()
        }
        mediaPlayer = null
        isPlaying = false
        isLoading = false
        currentAyah = null
    }
}
