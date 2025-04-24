package cs.vsu.taskbench.data.auth

import com.auth0.jwt.JWT

data class AuthTokens(
    val access: JWT,
    val refresh: JWT,
)

interface AuthTokenRepository {
    suspend fun getSavedTokens(): AuthTokens?
    suspend fun refreshTokens(): AuthTokens?
    suspend fun login(email: String, password: String)
}
