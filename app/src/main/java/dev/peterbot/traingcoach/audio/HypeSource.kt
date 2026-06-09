package dev.peterbot.traingcoach.audio

/**
 * Supplies the next hype phrase to play.
 *
 * This is the seam that lets step 2 swap the bundled res/raw phrases for
 * generated and cached wav files without touching the player or the ViewModel.
 */
interface HypeSource {

    /** Returns the next phrase to play, avoiding an immediate repeat. */
    fun next(): AudioSource
}
