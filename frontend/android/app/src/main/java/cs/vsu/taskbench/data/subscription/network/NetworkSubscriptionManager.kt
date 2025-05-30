package cs.vsu.taskbench.data.subscription.network

import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.withAuth
import cs.vsu.taskbench.data.subscription.SubscriptionManager
import cs.vsu.taskbench.domain.model.UserStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NetworkSubscriptionManager(
    private val authService: AuthService,
    private val netInterface: SubscriptionNetworkInterface,
) : SubscriptionManager {
    private var status: UserStatus? = null

    override suspend fun activate(): SubscriptionManager.ActivateResult {
        authService.withAuth { access ->
            val response = netInterface.activate(access)
            return SubscriptionManager.ActivateResult(response.confirmation_url)
        }
        error("")
    }

    override suspend fun deactivate() {
        authService.withAuth { access ->
            netInterface.deactivate(access)
        }
    }

    override suspend fun getStatus(): UserStatus {
        if (status == null) updateStatus()
        return status!!
    }

    override suspend fun updateStatus(): UserStatus {
        authService.withAuth { access ->
            val response = netInterface.getStatus(access)
            status = response.toModel()
        }
        return status!!
    }

    override suspend fun preload() {
        updateStatus()
    }

    private fun SubscriptionStatusResponse.toModel(): UserStatus = if (is_subscribed) {
        UserStatus.Premium(
            userId = user_id,
            nextPayment = LocalDateTime.parse(next_payment, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            isActive = is_active!!,
            subscriptionId = subscription_id!!,
        )
    } else {
        UserStatus.Unpaid(userId = user_id)
    }
}
