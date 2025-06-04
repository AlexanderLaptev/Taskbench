package cs.vsu.taskbench.data.auth

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class FakeAuthService(
    private val dataStore: DataStore<Preferences>,
) : AuthService {
    companion object {
        private val TAG = FakeAuthService::class.simpleName

        private const val MOCK_VALUE = "MOCK"
        private const val PASSWORD = "00000000"

        private val ACCESS_KEY = stringPreferencesKey("jwt_access")
        private val REFRESH_KEY = stringPreferencesKey("jwt_refresh")
    }

    private var tokens: AuthTokens = AuthTokens(MOCK_VALUE, MOCK_VALUE)

    override suspend fun getSavedTokens(): AuthTokens {
        Log.d(TAG, "getSavedTokens: enter")
        dataStore.data.first().let { prefs ->
            val access = prefs[ACCESS_KEY] ?: throw UnauthorizedException()
            val refresh = prefs[REFRESH_KEY] ?: throw UnauthorizedException()
            tokens = AuthTokens(access, refresh)
        }
        return tokens
    }

    override suspend fun refreshTokens() {
        Log.d(TAG, "refreshTokens: enter")
    }

    override suspend fun login(email: String, password: String) {
        Log.d(TAG, "login: enter")
        if (password != PASSWORD) throw UnauthorizedException()
        dataStore.edit { it[EMAIL_PREFERENCES_KEY] = email }
    }

    override suspend fun signUp(email: String, password: String) {
        Log.d(TAG, "signUp: enter")
        dataStore.edit { it[EMAIL_PREFERENCES_KEY] = email }
    }

    override suspend fun logout() {
        Log.d(TAG, "logout: enter")
        dataStore.edit { prefs ->
            prefs[ACCESS_KEY] = ""
            prefs[REFRESH_KEY] = ""
            prefs[EMAIL_PREFERENCES_KEY] = ""
        }
    }

    override suspend fun changePassword(old: String, new: String) {
        delay(700)
    }
}
