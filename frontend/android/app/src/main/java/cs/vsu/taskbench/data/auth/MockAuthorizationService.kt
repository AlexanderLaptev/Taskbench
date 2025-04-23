package cs.vsu.taskbench.data.auth

import kotlin.random.Random

object MockAuthorizationService : AuthorizationService {
    private const val ERROR_RATE = 3
    private var countdown = ERROR_RATE

    private val random = Random

    override suspend fun authorize(
        email: String,
        password: String,
    ): AuthorizationService.Result {
        if (countdown == 0) {
            countdown = ERROR_RATE
            return AuthorizationService.Result.Error
        }

        countdown--
        val id = random.nextInt(1, 1000)
        return AuthorizationService.Result.Success("$id\n$email")
    }

    override suspend fun signUp(
        email: String,
        password: String,
    ): AuthorizationService.Result = authorize(email, password)
}
