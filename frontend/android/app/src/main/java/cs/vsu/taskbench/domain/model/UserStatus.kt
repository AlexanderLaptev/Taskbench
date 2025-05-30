package cs.vsu.taskbench.domain.model

import java.time.LocalDateTime

sealed class UserStatus(
    open val userId: Int,
) {
    data class Unpaid(override val userId: Int) : UserStatus(userId)

    data class Premium(
        override val userId: Int,
        val nextPayment: LocalDateTime,
        val isActive: Boolean,
        val subscriptionId: Int,
    ) : UserStatus(userId)
}
