package cs.vsu.taskbench.data.auth

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.net.HttpURLConnection

class NetworkAuthService(
    private val networkAuthenticator: NetworkAuthenticator,
    private val dataStore: DataStore<Preferences>,
) : AuthService {
    companion object {
        private val TAG = NetworkAuthService::class.simpleName

        private val ACCESS_KEY = stringPreferencesKey("jwt_access")
        private val REFRESH_KEY = stringPreferencesKey("jwt_refresh")
    }

    private var cachedTokens: AuthTokens? = null

    override suspend fun getSavedTokens(): AuthTokens? {
        if (cachedTokens != null) {
            Log.d(TAG, "getSavedTokens: returning cached tokens")
            return cachedTokens
        }

        Log.d(TAG, "getSavedTokens: cached tokens not found, reading from data store")
        val access: String?
        val refresh: String?
        dataStore.data.first().let {
            access = it[ACCESS_KEY]
            refresh = it[REFRESH_KEY]
        }

        if (access.isNullOrEmpty() || refresh.isNullOrEmpty()) {
            Log.d(TAG, "getSavedTokens: no tokens saved in data store")
            return null
        }

        val saved = AuthTokens(access, refresh)
        cachedTokens = saved
        return saved
    }

    override suspend fun refreshTokens(): AuthTokens? {
        Log.d(TAG, "refreshTokens: refreshing tokens")
        val savedTokens = getSavedTokens() ?: run {
            Log.d(TAG, "refreshTokens: no saved tokens, returning null")
            return null
        }
        val request = AuthRefreshTokensRequest(savedTokens.refresh)
        try {
            val response = networkAuthenticator.refreshTokens(request)
            dataStore.edit {
                it[ACCESS_KEY] = response.access
                it[REFRESH_KEY] = response.refresh
            }
            val newTokens = savedTokens.copy(access = response.access, refresh = response.refresh)
            cachedTokens = newTokens
            return newTokens
        } catch (e: HttpException) {
            Log.e(TAG, "refreshTokens: auth error", e)
            return null
        }
    }

    override suspend fun login(email: String, password: String): AuthService.LoginResult {
        Log.d(TAG, "login: logging in user with email='$email'")
        val request = AuthLoginRequest(email, password)
        try {
            val response = networkAuthenticator.login(request)
            dataStore.edit {
                it[ACCESS_KEY] = response.access
                it[REFRESH_KEY] = response.refresh
            }
            val newTokens = AuthTokens(response.access, response.refresh)
            cachedTokens = newTokens
            return AuthService.LoginResult.Success
        } catch (e: HttpException) {
            Log.e(TAG, "login: auth error", e)
            return when (e.code()) {
                HttpURLConnection.HTTP_BAD_REQUEST -> AuthService.LoginResult.Failure
                else -> AuthService.LoginResult.UnknownError
            }
        }
    }

    override suspend fun signUp(email: String, password: String): AuthService.SignUpResult {
        Log.d(TAG, "signUp: signing up user with email='$email'")
        val request = AuthRegisterRequest(email, password)
        try {
            val response = networkAuthenticator.register(request)
            Log.d(TAG, "signUp: response=$response")
            dataStore.edit {
                it[ACCESS_KEY] = response.access
                it[REFRESH_KEY] = response.refresh
            }
            return AuthService.SignUpResult.Success
        } catch (e: HttpException) {
            Log.e(TAG, "signUp: auth error", e)
            return AuthService.SignUpResult.UnknownError
        }
    }

    override suspend fun logout() {
        Log.d(TAG, "logout: logging out")
        dataStore.edit {
            it[ACCESS_KEY] = ""
            it[REFRESH_KEY] = ""
        }
    }
}
