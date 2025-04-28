package cs.vsu.taskbench.data.auth

data class AuthTokens(
    val access: String,
    val refresh: String,
)

interface AuthService {
    enum class LoginResult {
        Success,
        Failure,
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
    suspend fun logout()
}
