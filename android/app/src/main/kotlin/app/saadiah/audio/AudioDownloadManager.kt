package app.saadiah.audio

import android.content.Context
import app.saadiah.model.Reciter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.PushbackInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

sealed interface DownloadState {
    data object Idle : DownloadState

    data class InProgress(
        val id: String,
        val title: String,
        val progress: Float,
        val currentItem: Int,
        val totalItems: Int,
    ) : DownloadState

    data class Completed(
        val id: String,
        val title: String,
    ) : DownloadState

    data class Failed(
        val id: String,
        val title: String,
        val error: String,
    ) : DownloadState
}

object AudioDownloadManager {
    const val ID_SURAH_BAQARAH = "quran_sura_2"
    const val ID_SURAH_IMRAN = "quran_sura_3"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeJob: Job? = null

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    fun quranAudioDir(
        context: Context,
        reciter: Reciter = Reciter.HUSARI_MUJAWWAD,
    ): File = File(context.filesDir, "audio/quran/${reciter.id}").apply { mkdirs() }

    fun isSurahDownloaded(
        context: Context,
        reciter: Reciter = Reciter.HUSARI_MUJAWWAD,
        sura: Int,
    ): Boolean {
        val dir = quranAudioDir(context, reciter)
        if (!dir.exists()) return false
        val total = if (sura == 2) 286 else 200
        val first = File(dir, "%03d%03d.mp3".format(sura, 1))
        val last = File(dir, "%03d%03d.mp3".format(sura, total))
        return first.exists() && first.length() > 0 && last.exists() && last.length() > 0
    }

    fun isSurahDownloaded(
        context: Context,
        sura: Int,
    ): Boolean = isSurahDownloaded(context, Reciter.HUSARI_MUJAWWAD, sura)

    fun surahFileFor(
        context: Context,
        reciter: Reciter = Reciter.HUSARI_MUJAWWAD,
        sura: Int,
        ayah: Int,
    ): File? {
        val dir = quranAudioDir(context, reciter)
        val file = File(dir, "%03d%03d.mp3".format(sura, ayah))
        return if (file.exists() && file.length() > 0) file else null
    }

    fun surahFileFor(
        context: Context,
        sura: Int,
        ayah: Int,
    ): File? = surahFileFor(context, Reciter.HUSARI_MUJAWWAD, sura, ayah)

    fun downloadSurah(
        context: Context,
        reciter: Reciter = Reciter.HUSARI_MUJAWWAD,
        sura: Int,
        title: String,
        verseCount: Int,
    ) {
        if (_downloadState.value is DownloadState.InProgress) return
        val id = if (sura == 2) ID_SURAH_BAQARAH else ID_SURAH_IMRAN

        activeJob =
            scope.launch {
                val dir = quranAudioDir(context, reciter)
                _downloadState.value =
                    DownloadState.InProgress(
                        id = id,
                        title = title,
                        progress = 0f,
                        currentItem = 0,
                        totalItems = verseCount,
                    )

                val suraPadded = sura.toString().padStart(3, '0')

                // Source 1: Bulk ZIP archive from saadiah.app (eventual source)
                val zipUrl =
                    "https://saadiah.app/artifacts/recitations/${reciter.zipName}/${reciter.zipType}_sura_$suraPadded.zip"
                val zipExtracted = downloadAndExtractZip(zipUrl, dir)

                if (zipExtracted && isSurahDownloaded(context, reciter, sura)) {
                    _downloadState.value = DownloadState.Completed(id = id, title = title)
                    return@launch
                }

                // Fallback: Ayah-by-ayah multi-source cycling
                // Source 2: EveryAyah CDN -> Source 3: mirrors.quranicaudio.com
                var success = true
                for (ayah in 1..verseCount) {
                    val fileName = "%03d%03d.mp3".format(sura, ayah)
                    val targetFile = File(dir, fileName)

                    if (!targetFile.exists() || targetFile.length() == 0L) {
                        val primaryAyahUrl = "https://everyayah.com/data/${reciter.everyAyahFolder}/$fileName"
                        val mirrorAyahUrl =
                            "https://mirrors.quranicaudio.com/everyayah/${reciter.everyAyahFolder}/$fileName"

                        val downloaded =
                            downloadFileWithFallback(
                                primaryUrl = primaryAyahUrl,
                                fallbackUrl = mirrorAyahUrl,
                                targetFile = targetFile,
                            )

                        if (!downloaded) {
                            success = false
                            break
                        }
                    }

                    val progress = ayah.toFloat() / verseCount.toFloat()
                    _downloadState.value =
                        DownloadState.InProgress(
                            id = id,
                            title = title,
                            progress = progress,
                            currentItem = ayah,
                            totalItems = verseCount,
                        )
                }

                if (success) {
                    _downloadState.value = DownloadState.Completed(id = id, title = title)
                } else {
                    _downloadState.value = DownloadState.Failed(id = id, title = title, error = "network_error")
                }
            }
    }

    fun downloadSurah(
        context: Context,
        sura: Int,
        title: String,
        verseCount: Int,
    ) = downloadSurah(context, Reciter.HUSARI_MUJAWWAD, sura, title, verseCount)

    fun cancelDownload() {
        activeJob?.cancel()
        activeJob = null
        _downloadState.value = DownloadState.Idle
    }

    fun dismissCompleted() {
        if (_downloadState.value is DownloadState.Completed || _downloadState.value is DownloadState.Failed) {
            _downloadState.value = DownloadState.Idle
        }
    }

    fun deleteSurah(
        context: Context,
        reciter: Reciter,
        sura: Int,
    ): Boolean {
        val dir = quranAudioDir(context, reciter)
        if (!dir.exists()) return true
        val prefix = "%03d".format(sura)
        dir.listFiles()?.filter { it.name.startsWith(prefix) }?.forEach { it.delete() }
        return !isSurahDownloaded(context, reciter, sura)
    }

    fun deleteSurah(
        context: Context,
        sura: Int,
    ): Boolean = deleteSurah(context, Reciter.HUSARI_MUJAWWAD, sura)

    fun deleteReciter(
        context: Context,
        reciter: Reciter,
    ): Boolean {
        val dir = File(context.filesDir, "audio/quran/${reciter.id}")
        return if (dir.exists()) dir.deleteRecursively() else true
    }

    fun getStorageUsedBytes(
        context: Context,
        reciter: Reciter,
    ): Long {
        val dir = File(context.filesDir, "audio/quran/${reciter.id}")
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun getTotalStorageUsedBytes(context: Context): Long {
        val dir = File(context.filesDir, "audio/quran")
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun formatStorage(bytes: Long): String =
        when {
            bytes <= 0L -> "0 MB"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> {
                val mb = bytes.toDouble() / (1024.0 * 1024.0)
                "%.1f MB".format(mb)
            }
        }

    private fun downloadAndExtractZip(
        zipUrlStr: String,
        targetDir: File,
    ): Boolean {
        return runCatching {
            val url = URL(zipUrlStr)
            val connection =
                (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8_000
                    readTimeout = 20_000
                    requestMethod = "GET"
                }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return false
            }

            val contentType = connection.contentType ?: ""
            if (contentType.contains("text/html", ignoreCase = true) ||
                contentType.contains("text/plain", ignoreCase = true)
            ) {
                connection.disconnect()
                return false
            }

            val rawStream = connection.inputStream
            val pushback = PushbackInputStream(rawStream, 4)
            val header = ByteArray(4)
            val readCount = pushback.read(header)
            if (readCount < 4 ||
                header[0] != 0x50.toByte() ||
                header[1] != 0x4B.toByte() ||
                header[2] != 0x03.toByte() ||
                header[3] != 0x04.toByte()
            ) {
                connection.disconnect()
                return false
            }
            pushback.unread(header, 0, readCount)

            var extractedAny = false
            ZipInputStream(pushback).use { zip ->
                var entry = zip.nextEntry
                val buffer = ByteArray(8192)
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".mp3")) {
                        val fileName = File(entry.name).name
                        val outputFile = File(targetDir, fileName)
                        FileOutputStream(outputFile).use { out ->
                            var len: Int
                            while (zip.read(buffer).also { len = it } != -1) {
                                out.write(buffer, 0, len)
                            }
                        }
                        extractedAny = true
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
            connection.disconnect()
            extractedAny
        }.getOrElse { false }
    }

    private fun downloadFileWithFallback(
        primaryUrl: String,
        fallbackUrl: String,
        targetFile: File,
    ): Boolean {
        if (downloadFile(primaryUrl, targetFile)) return true
        return downloadFile(fallbackUrl, targetFile)
    }

    private fun downloadFile(
        urlStr: String,
        targetFile: File,
    ): Boolean {
        return runCatching {
            val url = URL(urlStr)
            val connection =
                (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10_000
                    readTimeout = 15_000
                    requestMethod = "GET"
                }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return false
            }

            val tempFile = File(targetFile.parentFile, targetFile.name + ".tmp")
            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            connection.disconnect()
            tempFile.renameTo(targetFile)
        }.getOrElse { false }
    }
}
