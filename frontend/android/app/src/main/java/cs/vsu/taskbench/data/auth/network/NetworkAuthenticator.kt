@file:Suppress("PropertyName")

package cs.vsu.taskbench.data.auth.network

import com.squareup.moshi.JsonClass
import cs.vsu.taskbench.util.HttpHeaders
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class AuthRegisterRequest(
    val email: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class AuthRegisterResponse(
    val access: String,
    val refresh: String,
)

@JsonClass(generateAdapter = true)
data class AuthLoginRequest(
    val email: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class AuthLoginResponse(
    val user_id: Int,
    val access: String,
    val refresh: String,
)

@JsonClass(generateAdapter = true)
data class AuthRefreshTokensRequest(
    val refresh: String,
)

@JsonClass(generateAdapter = true)
data class AuthRefreshTokensResponse(
    val user_id: Int,
    val access: String,
    val refresh: String,
)

@JsonClass(generateAdapter = true)
data class AuthChangePasswordRequest(
    val old_password: String,
    val new_password: String,
)

interface NetworkAuthenticator {
    @POST("user/register/")
    suspend fun register(@Body request: AuthRegisterRequest): AuthRegisterResponse

    @POST("user/login/")
    suspend fun login(@Body request: AuthLoginRequest): AuthLoginResponse

    @POST("token/refresh/")
    suspend fun refreshTokens(@Body request: AuthRefreshTokensRequest): AuthRefreshTokensResponse

    @PATCH("user/password/")
    suspend fun changePassword(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Body request: AuthChangePasswordRequest,
    )
}
