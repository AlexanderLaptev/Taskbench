package cs.vsu.taskbench.data.auth

import androidx.datastore.preferences.core.stringPreferencesKey
import cs.vsu.taskbench.util.HttpStatusCodes
import retrofit2.HttpException

data class AuthTokens(
    val access: String,
    val refresh: String,
)

val EMAIL_PREFERENCES_KEY = stringPreferencesKey("email")

suspend inline fun AuthService.withAuth(block: (AuthTokens) -> Unit) {
    try {
        try {
            block(getSavedTokens())
        } catch (e: HttpException) {
            when (e.code()) {
                HttpStatusCodes.UNAUTHORIZED -> throw UnauthorizedException()
                else -> throw e
            }
        }
    } catch (e: UnauthorizedException) {
        refreshTokens()
        block(getSavedTokens())
    }
}

interface AuthService {
    suspend fun getSavedTokens(): AuthTokens
    suspend fun refreshTokens()
    suspend fun login(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun logout()
}
