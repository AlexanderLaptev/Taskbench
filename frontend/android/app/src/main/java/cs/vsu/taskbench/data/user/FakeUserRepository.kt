package cs.vsu.taskbench.data.user

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.EMAIL_PREFERENCES_KEY
import cs.vsu.taskbench.data.auth.UnauthorizedException
import cs.vsu.taskbench.domain.model.User
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class FakeUserRepository(
    private val authService: AuthService,
    private val dataStore: DataStore<Preferences>,
) : UserRepository {
    companion object {
        private val TAG = FakeUserRepository::class.simpleName
    }

    private var _user: User? = null
    override val user: User? get() = _user

    override suspend fun preload() {
        Log.d(TAG, "preloading fake user data")
        val status = User.Status.Premium(LocalDate.now().plusDays(7))
        val email = dataStore.data.first()[EMAIL_PREFERENCES_KEY] ?: throw UnauthorizedException()
        _user = User(0, email, status)
        Log.d(TAG, "loaded user data: $_user")
    }
}
