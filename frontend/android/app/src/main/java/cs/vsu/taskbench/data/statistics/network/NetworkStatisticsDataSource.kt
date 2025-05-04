@file:Suppress("PropertyName")

package cs.vsu.taskbench.data.statistics.network

import com.squareup.moshi.JsonClass
import cs.vsu.taskbench.util.HttpHeaders
import retrofit2.http.GET
import retrofit2.http.Header

@JsonClass(generateAdapter = true)
class StatisticsResponse(
    val done_today: Int,
    val max_done: Int,
    val weekly: FloatArray,
)

interface NetworkStatisticsDataSource {
    @GET("statistics")
    suspend fun getStatistics(@Header(HttpHeaders.AUTHORIZATION) auth: String): StatisticsResponse
}
