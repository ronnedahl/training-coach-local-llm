package dev.peterbot.traingcoach.tts

import android.util.Log
import com.k2fsa.sherpa.onnx.GeneratedAudio
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import java.io.File

/**
 * [Synthesizer] backed by sherpa-onnx running the Piper voice. The voice files
 * live on disk (pushed via adb), so all paths are filesystem paths and no
 * AssetManager is needed.
 *
 * The native engine is created lazily on first use and reused for the whole
 * batch (it is the heavy part), then freed via [close]. This runs in the
 * background generation job, never in the hot workout loop.
 */
class PiperSynthesizer(private val voiceFiles: VoiceFiles) : Synthesizer {

    private var tts: OfflineTts? = null

    private fun obtainTts(): OfflineTts {
        tts?.let { return it }
        check(voiceFiles.isPresent) {
            "Piper voice missing at ${voiceFiles.baseDir}. Push it with adb first."
        }
        val config = OfflineTtsConfig(
            model = OfflineTtsModelConfig(
                vits = OfflineTtsVitsModelConfig(
                    model = voiceFiles.model.absolutePath,
                    tokens = voiceFiles.tokens.absolutePath,
                    dataDir = voiceFiles.espeakData.absolutePath
                ),
                numThreads = 2
            )
        )
        return OfflineTts(config = config).also { tts = it }
    }

    override fun synthesize(text: String, outFile: File): Boolean {
        return try {
            val audio: GeneratedAudio = obtainTts().generate(text = text, sid = 0, speed = 1.0f)
            outFile.parentFile?.mkdirs()
            val saved = audio.save(outFile.absolutePath)
            saved && outFile.isFile && outFile.length() > 0L
        } catch (e: Exception) {
            // A single bad phrase must not abort the batch.
            Log.e(TAG, "Synthesis failed for: \"$text\"", e)
            false
        }
    }

    override fun close() {
        tts?.release()
        tts = null
    }

    companion object {
        private const val TAG = "PiperSynthesizer"
    }
}
