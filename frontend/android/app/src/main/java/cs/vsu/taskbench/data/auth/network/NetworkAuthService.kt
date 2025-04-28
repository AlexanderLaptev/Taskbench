package cs.vsu.taskbench.data.auth.network

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.AuthTokens
import cs.vsu.taskbench.data.auth.EMAIL_PREFERENCES_KEY
import cs.vsu.taskbench.data.auth.LoginException
import cs.vsu.taskbench.data.auth.UnauthorizedException
import cs.vsu.taskbench.util.HttpStatusCodes
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class NetworkAuthService(
    private val networkAuthenticator: NetworkAuthenticator,
    private val dataStore: DataStore<Preferences>, // TODO: use encryption for tokens
) : AuthService {
    companion object {
        private val TAG = NetworkAuthService::class.simpleName

        private val ACCESS_KEY = stringPreferencesKey("jwt_access")
        private val REFRESH_KEY = stringPreferencesKey("jwt_refresh")
    }

    private var cachedTokens: AuthTokens? = null

    override suspend fun getSavedTokens(): AuthTokens {
        if (cachedTokens != null) {
            Log.d(TAG, "getSavedTokens: returning cached tokens")
            return cachedTokens!!
        }

        Log.d(TAG, "getSavedTokens: cached tokens not found, reading from data store")
        val access: String?
        val refresh: String?
        dataStore.data.first().let {
            access = it[ACCESS_KEY]
            refresh = it[REFRESH_KEY]
        }

        if (access.isNullOrEmpty() || refresh.isNullOrEmpty()) {
            throw UnauthorizedException("No tokens saved on the device")
        }

        Log.d(TAG, "getSavedTokens: returning saved tokens")
        val saved = AuthTokens(access, refresh)
        cachedTokens = saved
        return saved
    }

    override suspend fun refreshTokens() {
        Log.d(TAG, "refreshTokens: refreshing tokens")
        val savedTokens = getSavedTokens()
        val request = AuthRefreshTokensRequest(savedTokens.refresh)
        val response = networkAuthenticator.refreshTokens(request)
        dataStore.edit {
            it[ACCESS_KEY] = response.access
            it[REFRESH_KEY] = response.refresh
        }
        val newTokens = AuthTokens(response.access, response.refresh)
        cachedTokens = newTokens
    }

    override suspend fun login(email: String, password: String) {
        Log.d(TAG, "login: logging in user with email='$email'")
        val request = AuthLoginRequest(email, password)
        val response: AuthLoginResponse
        try {
            response = networkAuthenticator.login(request)
        } catch (e: HttpException) {
            if (e.code() == HttpStatusCodes.BAD_REQUEST) {
                throw LoginException()
            } else {
                Log.d(TAG, "login: HTTP error ${e.code()}", e)
                throw e
            }
        }
        Log.d(TAG, "login: $response")
        dataStore.edit {
            it[ACCESS_KEY] = response.access
            it[REFRESH_KEY] = response.refresh
            it[EMAIL_PREFERENCES_KEY] = email
        }
        val newTokens = AuthTokens(response.access, response.refresh)
        cachedTokens = newTokens
    }

    override suspend fun signUp(email: String, password: String) {
        Log.d(TAG, "signUp: signing up user with email='$email'")
        val request = AuthRegisterRequest(email, password)
        val response = networkAuthenticator.register(request)
        Log.d(TAG, "signUp: $response")
        dataStore.edit {
            it[ACCESS_KEY] = response.access
            it[REFRESH_KEY] = response.refresh
            it[EMAIL_PREFERENCES_KEY] = email
        }
        val newTokens = AuthTokens(response.access, response.refresh)
        cachedTokens = newTokens
    }

    override suspend fun logout() {
        Log.d(TAG, "logout: logging out")
        dataStore.edit {
            it[ACCESS_KEY] = ""
            it[REFRESH_KEY] = ""
            it[EMAIL_PREFERENCES_KEY] = ""
        }
    }
}
