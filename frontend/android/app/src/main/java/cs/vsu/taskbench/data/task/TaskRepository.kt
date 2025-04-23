package cs.vsu.taskbench.data.task

import cs.vsu.taskbench.model.Task
import java.time.LocalDate

interface TaskRepository {
    enum class SortByMode {
        Priority,
        Deadline,
    }

    suspend fun getTasks(date: LocalDate, categoryId: Int, sortBy: SortByMode): List<Task>

    suspend fun saveTask(task: Task)
}
