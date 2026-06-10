package dev.peterbot.traingcoach.tts

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Exercises the real sherpa-onnx Piper engine on the device. Skipped (not
 * failed) when the voice has not been pushed, so it never blocks a build.
 *
 * To run it, push the voice first:
 *   adb shell mkdir -p /sdcard/Android/data/dev.peterbot.traingcoach/files/voice
 *   adb push vits-piper-en_US-ryan-medium \
 *     /sdcard/Android/data/dev.peterbot.traingcoach/files/voice/
 *   ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class PiperSynthesizerInstrumentedTest {

    @Test
    fun synthesizesWavWhenVoiceIsPresent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val voiceFiles = VoiceFiles(context)
        assumeTrue(
            "Piper voice not present at ${voiceFiles.baseDir}, skipping",
            voiceFiles.isPresent
        )

        val synthesizer = PiperSynthesizer(voiceFiles)
        try {
            val outFile = File(context.cacheDir, "tts_synth_test.wav")
            outFile.delete()

            val ok = synthesizer.synthesize("Push for one more rep", outFile)

            assertTrue("synthesize returned false", ok)
            assertTrue("wav file was not created", outFile.isFile)
            assertTrue("wav file is implausibly small", outFile.length() > 1000L)
        } finally {
            synthesizer.close()
        }
    }
}
