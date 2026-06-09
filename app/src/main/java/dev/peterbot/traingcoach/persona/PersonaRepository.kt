package dev.peterbot.traingcoach.persona

/**
 * Stores the user's chosen persona so the background generation job (later step
 * 2 PRs) can read it. Kept behind an interface so the backing store can change
 * without touching the UI or ViewModel.
 */
interface PersonaRepository {

    /** The currently selected persona (an LLM prompt value), never empty on load. */
    fun get(): String

    /** Persists [value] as the selected persona. */
    fun set(value: String)
}
