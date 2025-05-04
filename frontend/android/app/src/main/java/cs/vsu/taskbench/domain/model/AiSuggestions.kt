package cs.vsu.taskbench.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
data class AiSuggestions(
    val subtasks: List<String> = listOf(),
    val deadline: LocalDateTime? = null,
    val isHighPriority: Boolean? = null,
    val category: Category? = null,
)
