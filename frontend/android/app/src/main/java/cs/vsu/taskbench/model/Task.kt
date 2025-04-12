package cs.vsu.taskbench.model

import java.time.Instant

data class Task(
    val text: String,
    val deadline: Instant,
    val subtasks: List<Subtask>,
    val categoryId: Long,
)

data class Subtask(
    val text: String,
    val isDone: Boolean,
)
