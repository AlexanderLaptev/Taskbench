package cs.vsu.taskbench.data.user

import cs.vsu.taskbench.model.User

interface UserRepository {
    val user: User?

    suspend fun fetchUser(jwtToken: String)
}
