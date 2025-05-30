@file:Suppress("PropertyName")

package cs.vsu.taskbench.data.subscription.network

import com.squareup.moshi.JsonClass
import cs.vsu.taskbench.util.HttpHeaders
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class SubscriptionStatusResponse(
    val is_subscribed: Boolean,
    val user_id: Int,
    val next_payment: String?,
    val is_active: Boolean?,
    val subscription_id: Int?,
)

data class SubscriptionActivateResponse(
    val confirmation_url: String?,
    val yookassa_payment_id: Int?,
    val subscription_id: Int,
)

interface SubscriptionNetworkInterface {
    @POST("subscription/manage/")
    suspend fun activate(@Header(HttpHeaders.AUTHORIZATION) auth: String): SubscriptionActivateResponse

    @DELETE("subscription/manage/")
    suspend fun deactivate(@Header(HttpHeaders.AUTHORIZATION) auth: String)

    @GET("subscription/status/")
    suspend fun getStatus(@Header(HttpHeaders.AUTHORIZATION) auth: String): SubscriptionStatusResponse
}
