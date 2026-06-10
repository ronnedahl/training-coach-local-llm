package dev.peterbot.traingcoach.tts

import java.io.File

/**
 * Turns a phrase into a playable wav file. Kept behind an interface so the
 * background generation job (step 2 PR 5) depends on the abstraction, not on
 * sherpa-onnx directly, and so it can be faked in tests.
 */
interface Synthesizer {

    /**
     * Synthesizes [text] and writes a wav to [outFile]. Returns true on success.
     * Never throws for a single bad phrase, it logs and returns false.
     */
    fun synthesize(text: String, outFile: File): Boolean

    /** Releases native resources. Call when done generating a batch. */
    fun close()
}
