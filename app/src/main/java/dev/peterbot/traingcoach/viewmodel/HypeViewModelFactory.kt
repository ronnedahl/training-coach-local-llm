package dev.peterbot.traingcoach.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.peterbot.traingcoach.audio.HypeSource

/**
 * Builds a [HypeViewModel] with its [HypeSource] dependency. Keeping the source
 * injected (rather than created inside the ViewModel) is what lets step 2 hand
 * in a cache backed source without changing the ViewModel.
 */
class HypeViewModelFactory(
    private val application: Application,
    private val hypeSource: HypeSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HypeViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return HypeViewModel(application, hypeSource) as T
    }
}
