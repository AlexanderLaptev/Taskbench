package cs.vsu.taskbench.data.auth

interface AuthorizationService {
    sealed interface Result {
        data object Error : Result // TODO: error types
        data class Success(val jwtToken: String) : Result
    }

    suspend fun authorize(email: String, password: String): Result
}
