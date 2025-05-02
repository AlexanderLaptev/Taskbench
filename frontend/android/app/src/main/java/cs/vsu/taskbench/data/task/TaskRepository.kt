package cs.vsu.taskbench.data.task

import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Task
import java.time.LocalDate

sealed interface CategoryFilterState {
    data object Disabled : CategoryFilterState
    data class Enabled(val category: Category?) : CategoryFilterState
}

interface TaskRepository {
    enum class SortByMode {
        Priority,
        Deadline,
    }

    suspend fun getTasks(
        categoryFilter: CategoryFilterState,
        sortByMode: SortByMode,
        deadline: LocalDate?,
    ): List<Task>

    suspend fun saveTask(task: Task): Task?
    suspend fun deleteTask(task: Task)
}
