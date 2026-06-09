package dev.peterbot.traingcoach.persona

import androidx.annotation.StringRes

/**
 * A predefined persona: a Swedish UI label plus the English [promptValue] that
 * gets injected into the LLM prompt later (step 2 generation). UI text stays
 * Swedish, the prompt stays English.
 */
data class PersonaPreset(
    @param:StringRes val labelRes: Int,
    val promptValue: String
)
