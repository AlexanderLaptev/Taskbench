package cs.vsu.taskbench.model

import java.time.LocalDateTime

data class Task(
    val id: Int?,
    val content: String,
    val deadline: LocalDateTime,
    val isHighPriority: Boolean,
    val subtasks: List<Subtask>,
    val categoryId: Long,
)

data class Subtask(
    val id: Int?,
    val content: String,
    val isDone: Boolean,
)
