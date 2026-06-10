package dev.peterbot.traingcoach.tts

import android.content.Context
import java.io.File

/**
 * Resolves the Piper voice files in the app's external files dir, the location
 * adb can write to without root. Reports whether the voice has been pushed yet,
 * so callers can fail clearly instead of crashing deep inside the native code.
 */
class VoiceFiles(context: Context) {

    /** voice/<DIR_NAME> under getExternalFilesDir, where adb pushes the voice. */
    val baseDir: File =
        File(context.getExternalFilesDir(null), "voice/${PiperVoice.DIR_NAME}")

    val model: File get() = File(baseDir, PiperVoice.MODEL_FILE)
    val tokens: File get() = File(baseDir, PiperVoice.TOKENS_FILE)
    val espeakData: File get() = File(baseDir, PiperVoice.ESPEAK_DATA_DIR)

    /** True only when every required file is present on disk. */
    val isPresent: Boolean
        get() = model.isFile && tokens.isFile && espeakData.isDirectory
}
