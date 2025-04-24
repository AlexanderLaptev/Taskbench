package cs.vsu.taskbench.data.user

import android.util.Log
import com.auth0.jwt.JWT
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.domain.model.User
import java.time.LocalDate
import kotlin.math.abs

class FakeUserRepository(
    private val authService: AuthService,
) : UserRepository {
    companion object {
        private val TAG = FakeUserRepository::class.simpleName
    }

    private var _user: User? = null
    override val user: User? get() = _user

    override suspend fun preload(): Boolean {
        val tokens = authService.getSavedTokens() ?: return false

        val email = JWT.decode(tokens.refresh).subject!!.also {
            Log.d(TAG, "email: $it")
        }

        val id = abs(email.hashCode() % 30)

        val status = if (email.startsWith("premium")) {
            User.Status.Premium(LocalDate.now().plusDays(7))
        } else User.Status.Unpaid

        _user = User(id, email, status)
        return true
    }
}
