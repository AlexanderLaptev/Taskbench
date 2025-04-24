package cs.vsu.taskbench.data.auth

import android.util.Log
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import cs.vsu.taskbench.data.SettingsRepository
import cs.vsu.taskbench.data.auth.AuthService.LoginResult
import cs.vsu.taskbench.data.auth.AuthService.SignUpResult
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.temporal.ChronoUnit

class FakeAuthService(
    private val settingsRepository: SettingsRepository,
) : AuthService {
    companion object {
        private val TAG = FakeAuthService::class.simpleName
        private const val ERROR_COUNTDOWN = 1
    }

    private data class UserData(
        val id: Int,
        val password: String,
    )

    @Suppress("SpellCheckingInspection")
    private val users = mutableMapOf(
        "normal@example.com" to UserData(1, "qwertyui"),
        "premium@example.com" to UserData(2, "12345678"),
    )

    private var id = users.size

    private var countdown = 0

    private var tokens: AuthTokens? = null

    override suspend fun getSavedTokens(): AuthTokens? {
        if (tokens != null) return tokens

        Log.d(TAG, "no cached tokens, reading from settings")
        val settings = settingsRepository.flow.first()
        if (settings.jwtAccess.isNullOrEmpty() || settings.jwtRefresh.isNullOrEmpty()) return null
        tokens = AuthTokens(settings.jwtAccess, settings.jwtRefresh)
        return tokens
    }

    override suspend fun refreshTokens(): AuthTokens? {
        Log.d(TAG, "refreshing tokens")
        val refresh = tokens?.refresh ?: return null
        val email = JWT.decode(refresh).subject
        generateNewTokens(email)
        return tokens
    }

    override suspend fun login(email: String, password: String): LoginResult {
        if (countdown == 0) {
            countdown = ERROR_COUNTDOWN
            return LoginResult.UnknownError
        }

        val userData = users[email] ?: return LoginResult.UserNotFound
        if (password != userData.password) return LoginResult.IncorrectPassword

        countdown--
        generateNewTokens(email)
        return LoginResult.Success
    }

    override suspend fun signUp(email: String, password: String): SignUpResult {
        if (countdown == 0) {
            countdown = ERROR_COUNTDOWN
            return SignUpResult.UnknownError
        }

        if (users[email] != null) return SignUpResult.UserAlreadyExists
        users[email] = UserData(++id, password)

        countdown--
        refreshTokens()
        return SignUpResult.Success
    }

    override suspend fun logout() {
        settingsRepository.clearJwtTokens()
    }

    private fun generateNewTokens(email: String) {
        Log.d(TAG, "issuing tokens for $email")

        val issued = Instant.now()
        val access = JWT.create()
            .withSubject(email)
            .withClaim("id", users[email]!!.id)
            .withIssuer("https://auth.taskbench.ru")
            .withAudience("android")
            .withExpiresAt(issued.plus(2, ChronoUnit.MINUTES))
            .withIssuedAt(issued)
            .withNotBefore(issued)
            .sign(Algorithm.none())
        val refresh = JWT.create()
            .withSubject(email)
            .withClaim("id", users[email]!!.id)
            .withIssuer("https://auth.taskbench.ru")
            .withAudience("android")
            .withExpiresAt(issued.plus(2, ChronoUnit.WEEKS))
            .withIssuedAt(issued)
            .withNotBefore(issued)
            .sign(Algorithm.none())

        tokens = AuthTokens(access, refresh)
    }
}
