package cs.vsu.taskbench.data.auth

interface AuthorizationService {
    suspend fun getJwtToken(email: String, password: String): String
}
