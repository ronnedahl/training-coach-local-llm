package dev.peterbot.traingcoach.audio

import android.content.Context
import kotlin.random.Random

/**
 * A [HypeSource] that picks a random phrase from a fixed list of res/raw
 * resources, never playing the same one twice in a row. Used by step 1.
 */
class RawResHypeSource(
    context: Context,
    private val resIds: List<Int>
) : HypeSource {

    private val appContext: Context = context.applicationContext
    private var lastIndex = -1

    init {
        require(resIds.isNotEmpty()) { "RawResHypeSource needs at least one phrase" }
    }

    override fun next(): AudioSource {
        val index = pickIndex()
        lastIndex = index
        return RawResAudioSource(appContext, resIds[index])
    }

    private fun pickIndex(): Int {
        if (resIds.size == 1) return 0
        var candidate: Int
        do {
            candidate = Random.nextInt(resIds.size)
        } while (candidate == lastIndex)
        return candidate
    }
}
