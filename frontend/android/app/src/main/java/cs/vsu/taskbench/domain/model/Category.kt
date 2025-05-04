package cs.vsu.taskbench.domain.model

import androidx.compose.runtime.Immutable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Immutable
data class Category(
    val id: Int?,
    val name: String,
)
