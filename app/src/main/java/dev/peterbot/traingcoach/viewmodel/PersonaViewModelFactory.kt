package dev.peterbot.traingcoach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.peterbot.traingcoach.persona.PersonaRepository

/**
 * Builds a [PersonaViewModel] with its [PersonaRepository] dependency injected.
 */
class PersonaViewModelFactory(
    private val repository: PersonaRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PersonaViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return PersonaViewModel(repository) as T
    }
}
