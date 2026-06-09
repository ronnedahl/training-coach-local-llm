package dev.peterbot.traingcoach.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.peterbot.traingcoach.R
import dev.peterbot.traingcoach.viewmodel.HypeViewModel

/**
 * The step 2 workout screen: a persona selector before the set, plus the step 1
 * timer and START/STOPP button. The selector is hidden while a set runs so the
 * running screen stays minimal.
 */
@Composable
fun CoachScreen(
    vm: HypeViewModel,
    persona: String,
    onPersonaChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!vm.isRunning) {
            PersonaSelector(
                persona = persona,
                onPersonaChange = onPersonaChange,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatElapsed(vm.elapsed),
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = vm::toggle,
                modifier = Modifier.size(width = 240.dp, height = 120.dp)
            ) {
                Text(
                    text = stringResource(if (vm.isRunning) R.string.stop else R.string.start),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

/** Formats a number of seconds as mm:ss. */
private fun formatElapsed(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
