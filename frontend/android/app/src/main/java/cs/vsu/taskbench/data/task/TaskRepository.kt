package cs.vsu.taskbench.data.task

import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.domain.model.Task
import java.time.LocalDate

interface TaskRepository : PreloadRepository {
    enum class SortByMode {
        Priority,
        Deadline,
    }

    suspend fun getTasks(date: LocalDate, categoryId: Int, sortBy: SortByMode): List<Task>
    suspend fun saveTask(task: Task): Boolean
    suspend fun deleteTask(task: Task)
}
