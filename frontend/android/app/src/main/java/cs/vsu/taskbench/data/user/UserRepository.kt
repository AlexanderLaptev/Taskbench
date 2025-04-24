package cs.vsu.taskbench.data.user

import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.domain.model.User

interface UserRepository : PreloadRepository {
    val user: User?
}
