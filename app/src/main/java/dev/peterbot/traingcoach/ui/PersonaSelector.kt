package dev.peterbot.traingcoach.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.peterbot.traingcoach.R
import dev.peterbot.traingcoach.persona.PersonaPresets

/**
 * Lets the user pick who should hype them: a preset chip or a free text persona.
 * The selected value is the LLM prompt value; the chip whose promptValue matches
 * is highlighted, otherwise the free text field holds the value.
 *
 * Chips are stacked in a plain Column on purpose: FlowRow is an experimental
 * foundation API and is prone to version skew, and a vertical list of three
 * presets is clear and never overflows the width.
 */
@Composable
fun PersonaSelector(
    persona: String,
    onPersonaChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = PersonaPresets.all
    val matchedPreset = presets.firstOrNull { it.promptValue == persona }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.persona_title),
            style = MaterialTheme.typography.titleMedium
        )
        presets.forEach { preset ->
            FilterChip(
                selected = preset == matchedPreset,
                onClick = { onPersonaChange(preset.promptValue) },
                label = { Text(stringResource(preset.labelRes)) }
            )
        }
        OutlinedTextField(
            value = if (matchedPreset == null) persona else "",
            onValueChange = onPersonaChange,
            label = { Text(stringResource(R.string.persona_custom_label)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}
