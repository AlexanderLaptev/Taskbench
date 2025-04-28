package cs.vsu.taskbench.data.auth

data class AuthTokens(
    val access: String,
    val refresh: String,
)

interface AuthService {
    suspend fun getSavedTokens(): AuthTokens
    suspend fun refreshTokens()
    suspend fun login(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun logout()
}
