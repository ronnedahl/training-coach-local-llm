package dev.peterbot.traingcoach.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.peterbot.traingcoach.persona.PersonaRepository

/**
 * Holds the selected persona and persists changes through [PersonaRepository].
 * The generation job (later step 2 PRs) reads the same persona via the
 * repository.
 */
class PersonaViewModel(private val repository: PersonaRepository) : ViewModel() {

    var persona by mutableStateOf(repository.get())
        private set

    fun select(value: String) {
        persona = value
        repository.set(value)
    }
}
