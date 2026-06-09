package dev.peterbot.traingcoach.audio

import android.media.MediaPlayer
import java.io.File

/**
 * An [AudioSource] backed by a file on disk (a generated wav in the hype cache).
 * Used by step 2. The player treats it exactly like a bundled phrase.
 */
class FileAudioSource(private val file: File) : AudioSource {

    override fun prepareInto(player: MediaPlayer) {
        player.setDataSource(file.absolutePath)
        player.prepare()
    }
}
