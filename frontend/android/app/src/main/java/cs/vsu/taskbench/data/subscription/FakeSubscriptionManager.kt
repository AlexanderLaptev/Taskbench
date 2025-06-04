package cs.vsu.taskbench.data.subscription

import cs.vsu.taskbench.domain.model.UserStatus
import kotlinx.coroutines.delay
import java.time.LocalDateTime

object FakeSubscriptionManager : SubscriptionManager {
    private const val USER_ID = 1234
    private const val SUBSCRIPTION_ID = 4242

    private var isPremium = false

    override suspend fun preload() {
    }

    override suspend fun activate(): SubscriptionManager.ActivateResult {
        delay(200)
        isPremium = true
        return SubscriptionManager.ActivateResult("")
    }

    override suspend fun deactivate() {
        delay(200)
        isPremium = false
    }

    override suspend fun getStatus(): UserStatus {
        return if (isPremium) {
            UserStatus.Premium(
                userId = USER_ID,
                nextPayment = LocalDateTime.now().plusDays(7),
                isActive = true,
                subscriptionId = SUBSCRIPTION_ID,
            )
        } else UserStatus.Unpaid(USER_ID)
    }

    override suspend fun updateStatus(): UserStatus {
        delay(500)
        isPremium = !isPremium
        return getStatus()
    }
}
