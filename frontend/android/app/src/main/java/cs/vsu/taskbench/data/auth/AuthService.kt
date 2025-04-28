package cs.vsu.taskbench.data.auth

import androidx.datastore.preferences.core.stringPreferencesKey

data class AuthTokens(
    val access: String,
    val refresh: String,
)

val EMAIL_PREFERENCES_KEY = stringPreferencesKey("email")

interface AuthService {
    suspend fun getSavedTokens(): AuthTokens
    suspend fun refreshTokens()
    suspend fun login(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun logout()
}
