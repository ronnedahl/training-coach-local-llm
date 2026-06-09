package dev.peterbot.traingcoach.persona

import android.content.Context
import androidx.core.content.edit

/**
 * A [PersonaRepository] backed by SharedPreferences. The persona is a single
 * short string, so a preference file is plenty and avoids extra dependencies.
 */
class SharedPrefsPersonaRepository(context: Context) : PersonaRepository {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun get(): String =
        prefs.getString(KEY, PersonaPresets.default) ?: PersonaPresets.default

    override fun set(value: String) {
        prefs.edit { putString(KEY, value) }
    }

    companion object {
        private const val PREFS_NAME = "persona_prefs"
        private const val KEY = "selected_persona"
    }
}
