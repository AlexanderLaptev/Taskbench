package cs.vsu.taskbench.data.auth

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import cs.vsu.taskbench.data.auth.AuthService.LoginResult
import cs.vsu.taskbench.data.auth.AuthService.SignUpResult
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.temporal.ChronoUnit

class FakeAuthService(
    private val dataStore: DataStore<Preferences>, // TODO: secure storage
) : AuthService {
    companion object {
        private val TAG = FakeAuthService::class.simpleName
        private const val ERROR_COUNTDOWN = 1

        private val ACCESS_KEY = stringPreferencesKey("jwt_access")
        private val REFRESH_KEY = stringPreferencesKey("jwt_refresh")
    }

    private data class UserData(
        val id: Int,
        val password: String,
    )

    private val users = mutableMapOf(
        "normal@example.com" to UserData(1, "00000000"),
        "premium@example.com" to UserData(2, "11111111"),
    )

    private var id = users.size

    private var countdown = 0

    private var tokens: AuthTokens? = null

    override suspend fun getSavedTokens(): AuthTokens? {
        if (tokens != null) return tokens

        Log.d(TAG, "no cached tokens, reading from data store")
        val access: String?
        val refresh: String?
        dataStore.data.first().let {
            access = it[ACCESS_KEY]
            refresh = it[REFRESH_KEY]
        }
        if (access.isNullOrEmpty() || refresh.isNullOrEmpty()) {
            Log.d(TAG, "no saved tokens")
            return null
        }

        val saved = AuthTokens(access, refresh)
        val email = JWT.decode(saved.refresh).subject!!
        Log.d(TAG, "read saved tokens for user '$email'")
        tokens = saved
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
        Log.d(TAG, "login attempt for user '$email' with password '$password'")
        if (countdown == 0) {
            Log.d(TAG, "simulating unknown error")
            countdown = ERROR_COUNTDOWN
            return LoginResult.UnknownError
        }
        countdown--

        val userData = users[email] ?: let {
            Log.d(TAG, "user '$email' not found")
            return LoginResult.UserNotFound
        }
        if (password != userData.password) {
            Log.d(TAG, "incorrect password")
            return LoginResult.IncorrectPassword
        }

        generateNewTokens(email)
        Log.d(TAG, "login success for user '$email'")
        return LoginResult.Success
    }

    override suspend fun signUp(email: String, password: String): SignUpResult {
        Log.d(TAG, "sign up attempt for user '$email' with password '$password'")
        if (countdown == 0) {
            Log.d(TAG, "simulating unknown error")
            countdown = ERROR_COUNTDOWN
            return SignUpResult.UnknownError
        }
        countdown--

        if (users[email] != null) {
            Log.d(TAG, "user '$email' already exists")
            return SignUpResult.UserAlreadyExists
        }
        users[email] = let {
            val data = UserData(++id, password)
            Log.d(TAG, "user data: $data")
            data
        }

        generateNewTokens(email)
        Log.d(TAG, "sign up success for user '$email'")
        return SignUpResult.Success
    }

    override suspend fun logout() {
        Log.d(TAG, "logout")
        dataStore.edit {
            it[ACCESS_KEY] = ""
            it[REFRESH_KEY] = ""
        }
    }

    private suspend fun generateNewTokens(email: String) {
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
            .withExpiresAt(issued.plus(2 * 7, ChronoUnit.DAYS))
            .withIssuedAt(issued)
            .withNotBefore(issued)
            .sign(Algorithm.none())

        val generated = AuthTokens(access, refresh)
        tokens = generated
        dataStore.edit {
            it[ACCESS_KEY] = access
            it[REFRESH_KEY] = refresh
        }
    }
}
