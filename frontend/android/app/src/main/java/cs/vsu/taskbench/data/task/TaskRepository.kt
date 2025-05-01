package cs.vsu.taskbench.data.task

import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.domain.model.Task
import java.time.LocalDate

interface TaskRepository : PreloadRepository {
    enum class SortByMode {
        Priority,
        Deadline,
    }

    suspend fun getTasksInCategory(
        date: LocalDate? = null,
        categoryId: Int? = null,
        sortBy: SortByMode = SortByMode.Priority,
    ): List<Task>

    suspend fun getTasks(date: LocalDate?, sortBy: SortByMode): List<Task>
    suspend fun saveTask(task: Task): Task?
    suspend fun deleteTask(task: Task)
}
