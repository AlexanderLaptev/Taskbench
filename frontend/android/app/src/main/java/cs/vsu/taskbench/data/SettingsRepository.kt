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
        val JWT_ACCESS = stringPreferencesKey("jwt_access") // TODO: secure storage
        val JWT_REFRESH = stringPreferencesKey("jwt_refresh")
    }

    data class Settings(
        val jwtAccess: String?,
        val jwtRefresh: String?,
    )

    val flow: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            jwtAccess = prefs[PreferencesKeys.JWT_ACCESS].takeUnless { it.isNullOrEmpty() },
            jwtRefresh = prefs[PreferencesKeys.JWT_REFRESH].takeUnless { it.isNullOrEmpty() },
        )
    }

    // TODO: move to AuthTokenRepository
    suspend fun setJwtTokens(access: String, refresh: String) {
        dataStore.edit {
            it[PreferencesKeys.JWT_ACCESS] = access
            it[PreferencesKeys.JWT_REFRESH] = refresh
        }
    }

    suspend fun clearJwtTokens() {
        dataStore.edit {
            it[PreferencesKeys.JWT_ACCESS] = ""
            it[PreferencesKeys.JWT_REFRESH] = ""
        }
    }
}
