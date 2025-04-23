package cs.vsu.taskbench.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private object PreferencesKeys {
        val JWT_TOKEN = stringPreferencesKey("jwt_token") // TODO: secure storage
    }

    data class Settings(
        val jwtToken: String,
    )

    val flow: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(prefs[PreferencesKeys.JWT_TOKEN] ?: "")
    }

    suspend fun setJwtToken(jwtToken: String) {
        dataStore.edit { it[PreferencesKeys.JWT_TOKEN] = jwtToken }
    }
}
