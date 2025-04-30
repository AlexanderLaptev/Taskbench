@file:Suppress("PropertyName")

package cs.vsu.taskbench.data.task.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskDpc(
    val deadline: String? = null,
    val priority: Int? = null,
    val category_id: Int? = null,
    val category_name: String? = null,
)
