package cs.vsu.taskbench.data.subscription

import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.domain.model.UserStatus

interface SubscriptionManager : PreloadRepository {
    data class ActivateResult(val paymentUrl: String?)

    suspend fun activate(): ActivateResult
    suspend fun deactivate()
    suspend fun getStatus(): UserStatus
    suspend fun updateStatus(): UserStatus
}
