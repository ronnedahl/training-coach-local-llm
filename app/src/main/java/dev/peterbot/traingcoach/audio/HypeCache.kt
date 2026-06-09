package dev.peterbot.traingcoach.audio

import android.content.Context
import java.io.File

/**
 * The single place that knows where generated hype phrases live on disk.
 *
 * Generated phrases are regenerable, so they belong under cacheDir (the system
 * may reclaim them, and the background job will simply rebuild the bank). Both
 * the player (step 2, [CachedHypeSource]) and the generation job (step 2,
 * WorkManager) resolve the directory through here so they never disagree.
 */
object HypeCache {

    private const val DIR_NAME = "hype"

    /** Returns the hype cache directory, creating it if needed. */
    fun dir(context: Context): File =
        File(context.applicationContext.cacheDir, DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
}
