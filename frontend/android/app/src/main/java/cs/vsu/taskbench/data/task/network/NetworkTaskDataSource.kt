@file:Suppress("PropertyName")

package cs.vsu.taskbench.data.task.network

import com.squareup.moshi.JsonClass
import cs.vsu.taskbench.util.HttpHeaders
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class TaskResponse(
    val id: Int,
    val content: String,
    val is_done: Boolean,
    val dpc: TaskDpc,
    val subtasks: List<SubtaskResponse>,
)

@JsonClass(generateAdapter = true)
data class SubtaskResponse(
    val id: Int,
    val content: String,
    val is_done: Boolean,
)

@JsonClass(generateAdapter = true)
data class SubtaskInput(
    val content: String,
)

@JsonClass(generateAdapter = true)
data class AddTaskRequest(
    val content: String,
    val dpc: TaskDpc,
    val subtasks: List<SubtaskInput>,
)

@JsonClass(generateAdapter = true)
data class EditTaskRequest(
    val text: String,
    val dpc: TaskDpc,
)

@JsonClass(generateAdapter = true)
data class AddSubtaskRequest(
    val content: String,
    val is_done: Boolean,
)

@JsonClass(generateAdapter = true)
data class EditSubtaskRequest(
    val content: String? = null,
    val is_done: Boolean? = null,
)

interface NetworkTaskDataSource {
    @GET("tasks/")
    suspend fun getAllTasks(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("sort_by") sortBy: String?,
        @Query("date") date: String?,
        @Query("category_id") categoryId: Int? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
    ): List<TaskResponse>

    @POST("tasks/")
    suspend fun createTask(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Body request: AddTaskRequest,
    ): TaskResponse

    @DELETE("tasks/{taskId}/")
    suspend fun deleteTask(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Path("taskId") taskId: Int,
    )

    @PATCH("tasks/{taskId}/")
    suspend fun editTask(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Path("taskId") taskId: Int,
    ): TaskResponse

    @POST("subtasks/")
    suspend fun addSubtask(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Body request: AddSubtaskRequest,
    ): SubtaskResponse

    @PATCH("subtasks/{subtaskId}/")
    suspend fun editSubtask(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Path("subtaskId") subtaskId: Int,
        @Body request: EditSubtaskRequest,
    ): SubtaskResponse

    @DELETE("subtasks/{subtaskId}/")
    suspend fun deleteSubtask(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Path("subtaskId") subtaskId: Int,
    )
}
