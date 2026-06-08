package dev.peterbot.traingcoach.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.peterbot.traingcoach.audio.HypeSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Drives the workout session: a counting timer plus periodic hype playback.
 *
 * The heavy work (which phrase, where it comes from) lives behind [HypeSource],
 * so this class stays the same when step 2 swaps the bundled phrases for
 * generated ones.
 */
class HypeViewModel(
    application: Application,
    private val hypeSource: HypeSource
) : AndroidViewModel(application) {

    private val player = MediaPlayer()
    private var loopJob: Job? = null

    /** True while a set is running (between START and STOPP). */
    var isRunning by mutableStateOf(false)
        private set

    /** Seconds elapsed since the current set started. */
    var elapsed by mutableStateOf(0)
        private set

    fun toggle() {
        if (isRunning) stop() else start()
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        elapsed = 0
        playNext() // hype straight away at start
        loopJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                elapsed++
                if (elapsed % INTERVAL_SECONDS == 0) playNext()
            }
        }
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        loopJob?.cancel()
        loopJob = null
        player.reset()
    }

    private fun playNext() {
        try {
            val source = hypeSource.next()
            player.reset()
            source.prepareInto(player)
            player.start()
        } catch (e: Exception) {
            // A single failed phrase should never crash the workout.
            Log.e(TAG, "Failed to play hype phrase", e)
        }
    }

    override fun onCleared() {
        loopJob?.cancel()
        player.release()
    }

    companion object {
        private const val TAG = "HypeViewModel"

        /** How often a new hype phrase plays during a set, in seconds. */
        private const val INTERVAL_SECONDS = 25
    }
}
