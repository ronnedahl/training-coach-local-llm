package dev.peterbot.traingcoach.audio

import java.io.File
import kotlin.random.Random

/**
 * A [HypeSource] that plays generated wav files from the hype cache directory,
 * never repeating the previous one. When the cache is empty (for example before
 * the first background generation has run) it transparently delegates to a
 * [fallback] source, so the app is never silent.
 *
 * This is the step 2 source. The player and ViewModel use it unchanged: only the
 * source handed to the ViewModel factory in MainActivity differs from step 1.
 */
class CachedHypeSource(
    private val cacheDir: File,
    private val fallback: HypeSource
) : HypeSource {

    private var lastName: String? = null

    override fun next(): AudioSource {
        val files = listPhraseFiles()
        if (files.isEmpty()) return fallback.next()
        val file = pick(files)
        lastName = file.name
        return FileAudioSource(file)
    }

    private fun listPhraseFiles(): List<File> =
        cacheDir.listFiles { file ->
            file.isFile && file.extension.lowercase() in AUDIO_EXTENSIONS
        }?.sortedBy { it.name }.orEmpty()

    private fun pick(files: List<File>): File {
        if (files.size == 1) return files[0]
        var candidate: File
        do {
            candidate = files[Random.nextInt(files.size)]
        } while (candidate.name == lastName)
        return candidate
    }

    companion object {
        private val AUDIO_EXTENSIONS = setOf("wav", "mp3")
    }
}
