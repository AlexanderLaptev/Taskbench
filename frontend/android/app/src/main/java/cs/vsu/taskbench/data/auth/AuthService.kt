package cs.vsu.taskbench.data.auth

import androidx.datastore.preferences.core.stringPreferencesKey
import cs.vsu.taskbench.util.HttpStatusCodes
import cs.vsu.taskbench.util.toAuthHeader
import retrofit2.HttpException

data class AuthTokens(
    val access: String,
    val refresh: String,
)

val EMAIL_PREFERENCES_KEY = stringPreferencesKey("email")

const val TAG = "AuthService.withAuth"

suspend inline fun AuthService.withAuth(block: (String) -> Unit) {
    try {
        try {
            block(getSavedTokens().access.toAuthHeader())
        } catch (e: HttpException) {
            when (e.code()) {
                HttpStatusCodes.UNAUTHORIZED -> throw UnauthorizedException()
                else -> throw e
            }
        }
    } catch (e: UnauthorizedException) {
        refreshTokens()
        block(getSavedTokens().access.toAuthHeader())
    }
}

interface AuthService {
    suspend fun getSavedTokens(): AuthTokens
    suspend fun refreshTokens()
    suspend fun login(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun logout()
}
