package cs.vsu.taskbench.data.user

import android.util.Log
import cs.vsu.taskbench.domain.model.User
import java.time.LocalDate

object FakeUserRepository : UserRepository {
    private val TAG = FakeUserRepository::class.simpleName

    private var _user: User? = null
    override val user: User? get() = _user

    override suspend fun fetchUser(jwtToken: String) {
        val jwt = jwtToken.replace("\n", "; ")
        Log.d(TAG, "fetching user with jwt='$jwt'")

        val lines = jwtToken.lines()
        _user = User(
            id = lines[0].toInt(),
            email = lines[1],
            status = User.Status.Premium(
                LocalDate.now().plusDays(7)
            )
        )
    }

    override suspend fun logout() {
        _user = null
    }
}
