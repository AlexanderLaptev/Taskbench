package cs.vsu.taskbench.data.auth

import com.auth0.jwt.JWT

data class AuthTokens(
    val access: JWT,
    val refresh: JWT,
)

interface AuthTokenRepository {
    enum class LoginResult {
        Success,
        UserNotFound,
        IncorrectPassword,
        UnknownError,
    }

    enum class SignUpResult {
        Success,
        UserAlreadyExists,
        UnknownError,
    }

    suspend fun getSavedTokens(): AuthTokens?
    suspend fun refreshTokens(): AuthTokens?
    suspend fun login(email: String, password: String): LoginResult
    suspend fun signUp(email: String, password: String): SignUpResult
}
