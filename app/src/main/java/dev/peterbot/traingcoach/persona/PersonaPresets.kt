package dev.peterbot.traingcoach.persona

import dev.peterbot.traingcoach.R

/**
 * The built in persona choices. The single place that lists presets, so the UI
 * and the (later) generation job agree on them.
 */
object PersonaPresets {

    val all: List<PersonaPreset> = listOf(
        PersonaPreset(R.string.persona_drill_sergeant, "drill sergeant"),
        PersonaPreset(R.string.persona_supportive_coach, "supportive coach"),
        PersonaPreset(R.string.persona_sarcastic_pt, "sarcastic personal trainer")
    )

    /** Persona used until the user picks another one. */
    val default: String = all.first().promptValue
}
