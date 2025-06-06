package cs.vsu.taskbench.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
data class Task(
    val id: Int?,
    val content: String,
    val deadline: LocalDateTime?,
    val isHighPriority: Boolean,
    val subtasks: List<Subtask>,
    val categoryId: Int?,
)

@Immutable
data class Subtask(
    val id: Int?,
    val content: String,
    val isDone: Boolean,
)
