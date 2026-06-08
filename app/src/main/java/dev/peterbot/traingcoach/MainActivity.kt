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
import dev.peterbot.traingcoach.audio.DefaultHypePhrases
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
 * Wires the step 1 player: the bundled res/raw phrases behind [RawResHypeSource].
 * Step 2 only has to swap the source passed to the factory here.
 */
@Composable
private fun rememberHypeViewModel(): HypeViewModel {
    val application = LocalContext.current.applicationContext as Application
    val hypeSource = remember(application) {
        RawResHypeSource(application, DefaultHypePhrases.rawResIds)
    }
    return viewModel(factory = HypeViewModelFactory(application, hypeSource))
}
