@file:Suppress("PropertyName")

package cs.vsu.taskbench.data.task.suggestions.network

import com.squareup.moshi.JsonClass
import cs.vsu.taskbench.data.task.network.TaskDpc
import cs.vsu.taskbench.util.HttpHeaders
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class SuggestionsRequest(
    val title: String,
    val timestamp: String,
    val dpc: TaskDpc,
)

@JsonClass(generateAdapter = true)
data class SuggestionsResponse(
    val suggested_dpc: TaskDpc,
    val suggestions: List<String>,
)

interface NetworkSuggestionDataSource {
    @POST("ai/suggestions/")
    suspend fun getSuggestions(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Body request: SuggestionsRequest,
    ): SuggestionsResponse
}
