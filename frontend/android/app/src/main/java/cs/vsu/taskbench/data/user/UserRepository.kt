package cs.vsu.taskbench.data.user

import cs.vsu.taskbench.model.User

interface UserRepository {
    suspend fun authenticateUser(email: String, password: String): User
}
