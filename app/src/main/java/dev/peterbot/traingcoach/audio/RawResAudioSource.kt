package dev.peterbot.traingcoach.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

/**
 * An [AudioSource] backed by a bundled file in res/raw. Used by step 1.
 */
class RawResAudioSource(
    context: Context,
    @param:RawRes private val resId: Int
) : AudioSource {

    private val appContext: Context = context.applicationContext

    override fun prepareInto(player: MediaPlayer) {
        // Keep the asset file descriptor open across prepare(). MediaPlayer reads
        // from it while preparing, so we must not close it before that is done.
        appContext.resources.openRawResourceFd(resId).use { afd ->
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            player.prepare()
        }
    }
}
