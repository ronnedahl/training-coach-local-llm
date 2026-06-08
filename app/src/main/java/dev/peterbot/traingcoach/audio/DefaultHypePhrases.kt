package dev.peterbot.traingcoach.audio

import dev.peterbot.traingcoach.R

/**
 * The bundled step 1 phrase bank. The single place that knows which res/raw
 * files exist. Add a new file here when you drop another pep_xx.mp3 into
 * res/raw and the player picks it up automatically.
 */
object DefaultHypePhrases {

    val rawResIds: List<Int> = listOf(
        R.raw.pep_01,
        R.raw.pep_02,
        R.raw.pep_03,
        R.raw.pep_04,
        R.raw.pep_05,
        R.raw.pep_06,
        R.raw.pep_07,
        R.raw.pep_08,
        R.raw.pep_09,
        R.raw.pep_10,
        R.raw.pep_11
    )
}
