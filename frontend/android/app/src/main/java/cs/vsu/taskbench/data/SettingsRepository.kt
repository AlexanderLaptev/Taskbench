package cs.vsu.taskbench.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private object PreferencesKeys {
        val LOGGED_IN = booleanPreferencesKey("logged_in")
    }

    data class Settings(
        val isLoggedIn: Boolean,
    )

    val flow: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(prefs[PreferencesKeys.LOGGED_IN] ?: false)
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        dataStore.edit { prefs -> prefs[PreferencesKeys.LOGGED_IN] = loggedIn }
    }
}
