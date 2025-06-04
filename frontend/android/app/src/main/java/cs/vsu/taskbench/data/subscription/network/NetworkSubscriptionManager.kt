package cs.vsu.taskbench.data.subscription.network

import android.util.Log
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
    companion object {
        private val TAG = NetworkSubscriptionManager::class.simpleName
    }

    private var status: UserStatus? = null

    override suspend fun activate(): SubscriptionManager.ActivateResult {
        authService.withAuth { access ->
            val response = netInterface.activate(access)
            return SubscriptionManager.ActivateResult(response.confirmation_url).also {
                Log.d(TAG, "activated (url=${it.paymentUrl})")
            }
        }
        error("")
    }

    override suspend fun deactivate() {
        authService.withAuth { access ->
            netInterface.deactivate(access)
            Log.d(TAG, "deactivated")
        }
    }

    override suspend fun getStatus(): UserStatus {
        if (status == null) updateStatus()
        Log.d(TAG, "getStatus: $status")
        return status!!
    }

    override suspend fun updateStatus(): UserStatus {
        authService.withAuth { access ->
            val response = netInterface.getStatus(access)
            status = response.toModel()
        }
        Log.d(TAG, "updated status")
        return status!!
    }

    override suspend fun preload() {
        updateStatus()
    }

    private fun SubscriptionStatusResponse.toModel(): UserStatus = if (subscription_id != null) {
        Log.d(TAG, "toModel: premium")
        UserStatus.Premium(
            userId = user_id,
            nextPayment = LocalDateTime.parse(next_payment, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            isActive = is_active!!,
            subscriptionId = subscription_id,
        )
    } else {
        Log.d(TAG, "toModel: unpaid")
        UserStatus.Unpaid(userId = user_id)
    }
}
