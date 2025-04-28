package cs.vsu.taskbench.data.user

import android.util.Log
import com.auth0.jwt.JWT
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.domain.model.User
import java.time.LocalDate

class FakeUserRepository(
    private val authService: AuthService,
) : UserRepository {
    companion object {
        private val TAG = FakeUserRepository::class.simpleName
    }

    private var _user: User? = null
    override val user: User? get() = _user

    override suspend fun preload(): Boolean {
        Log.d(TAG, "preloading user data")
        val tokens = authService.getSavedTokens() ?: let {
            Log.d(TAG, "preload failed: no saved tokens")
            return false
        }

        val decoded = JWT.decode(tokens.access)
        val status = User.Status.Premium(LocalDate.now().plusDays(7))

        _user = User(0, "", status)
        Log.d(TAG, "loaded user data: $_user")
        return true
    }
}
