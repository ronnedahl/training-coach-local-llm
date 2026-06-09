package dev.peterbot.traingcoach

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.peterbot.traingcoach.audio.CachedHypeSource
import dev.peterbot.traingcoach.audio.DefaultHypePhrases
import dev.peterbot.traingcoach.audio.HypeCache
import dev.peterbot.traingcoach.audio.RawResHypeSource
import dev.peterbot.traingcoach.ui.CoachScreen
import dev.peterbot.traingcoach.ui.theme.TraingCoachTheme
import dev.peterbot.traingcoach.viewmodel.HypeViewModel
import dev.peterbot.traingcoach.viewmodel.HypeViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraingCoachTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CoachScreen(
                        vm = rememberHypeViewModel(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Wires the player to the step 2 source: generated phrases from the hype cache,
 * falling back to the bundled res/raw phrases while the cache is still empty.
 * The player and ViewModel are unchanged from step 1, only the source differs.
 */
@Composable
private fun rememberHypeViewModel(): HypeViewModel {
    val application = LocalContext.current.applicationContext as Application
    val hypeSource = remember(application) {
        val fallback = RawResHypeSource(application, DefaultHypePhrases.rawResIds)
        CachedHypeSource(HypeCache.dir(application), fallback)
    }
    return viewModel(factory = HypeViewModelFactory(application, hypeSource))
}
