package cs.vsu.taskbench.data.subscription

import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.domain.model.UserStatus

interface SubscriptionManager : PreloadRepository {
    suspend fun activate()
    suspend fun deactivate()
    suspend fun getStatus(): UserStatus
}
