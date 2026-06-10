package dev.peterbot.traingcoach.tts

/**
 * Describes the bundled Piper voice (vits-piper-en_US-ryan-medium) and where its
 * files live on the device. The voice is pushed manually via adb to the app's
 * external files dir (it is too large to ship in the APK), the same way the
 * Gemma model will be:
 *
 *   adb push vits-piper-en_US-ryan-medium \
 *     /sdcard/Android/data/dev.peterbot.traingcoach/files/voice/
 *
 * The single place that knows the voice's file names, so the synthesizer and
 * any setup docs agree.
 */
object PiperVoice {

    /** Folder name under the app external "voice" dir, matches the release tarball. */
    const val DIR_NAME = "vits-piper-en_US-ryan-medium"

    const val MODEL_FILE = "en_US-ryan-medium.onnx"
    const val TOKENS_FILE = "tokens.txt"

    /** espeak-ng phoneme data directory that Piper needs; must be a real path. */
    const val ESPEAK_DATA_DIR = "espeak-ng-data"
}
