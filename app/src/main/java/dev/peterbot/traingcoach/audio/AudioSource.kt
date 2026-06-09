package dev.peterbot.traingcoach.audio

import android.media.MediaPlayer

/**
 * A single playable hype phrase.
 *
 * The abstraction keeps the player ignorant of where the audio actually lives.
 * In step 1 the bytes come from res/raw, in step 2 they will come from generated
 * wav files in the cache directory. Only the implementation changes, never the
 * player.
 */
interface AudioSource {

    /**
     * Configures the given (already reset) [player] with this source and prepares
     * it for playback. After this returns the caller only has to call
     * [MediaPlayer.start].
     *
     * Preparing here, rather than in the caller, lets each implementation keep any
     * resource it needs (for example a file descriptor) open across the prepare
     * call.
     */
    fun prepareInto(player: MediaPlayer)
}
