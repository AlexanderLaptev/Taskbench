package cs.vsu.taskbench.data.task.network

import android.util.Log
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.withAuth
import cs.vsu.taskbench.data.task.CategoryFilterState
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// TODO: reduce the number of updates

class NetworkTaskRepository(
    private val authService: AuthService,
    private val dataSource: NetworkTaskDataSource,
) : TaskRepository {
    companion object {
        private val TAG = NetworkTaskRepository::class.simpleName
    }

    override suspend fun getTasks(
        categoryFilter: CategoryFilterState,
        sortByMode: TaskRepository.SortByMode,
        deadline: LocalDate?,
    ): List<Task> {
        Log.d(TAG, "getTasks: enter")
        authService.withAuth { access ->
            val response = dataSource.getAllTasks(
                access,
                offset = 0,
                limit = 1000,
                date = deadline?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                categoryId = when (categoryFilter) {
                    CategoryFilterState.Disabled -> null
                    is CategoryFilterState.Enabled -> categoryFilter.category?.id
                },
                sortBy = when (sortByMode) {
                    TaskRepository.SortByMode.Priority -> "priority"
                    TaskRepository.SortByMode.Deadline -> "deadline"
                },
            )
            return response.map { task ->
                Task(
                    id = task.id,
                    content = task.content,
                    deadline = task.dpc.deadline?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    },
                    isHighPriority = when (task.dpc.priority) {
                        null -> false
                        0 -> false
                        else -> true
                    },
                    subtasks = task.subtasks
                        .asSequence()
                        .map { subtask ->
                            Subtask(
                                id = subtask.id,
                                content = subtask.content,
                                isDone = subtask.is_done,
                            )
                        }
                        .sortedBy { it.id }
                        .toList(),
                    categoryId = task.dpc.category_id,
                )
            }
        }
        error("")
    }

    override suspend fun saveTask(task: Task): Task {
        return if (task.id == null) createTask(task) else updateTask(task)
    }

    private suspend fun createTask(task: Task): Task {
        authService.withAuth { access ->
            val response = dataSource.createTask(
                access,
                AddTaskRequest(
                    content = task.content,
                    dpc = task.toDpc(),
                    subtasks = task.subtasks.map { SubtaskInput(it.content) }
                )
            )
            val result = response.toModel()
            Log.d(TAG, "createTask: success")
            return result
        }
        error("")
    }

    override suspend fun createSubtask(owner: Task, subtask: Subtask): Subtask {
        authService.withAuth { access ->
            val request = AddSubtaskRequest(
                content = subtask.content,
                is_done = subtask.isDone,
            )
            val response = dataSource.addSubtask(access, owner.id!!, request)
            return Subtask(
                id = response.id,
                content = response.content,
                isDone = response.is_done,
            )
        }
        error("")
    }

    override suspend fun updateSubtask(subtask: Subtask): Subtask {
        authService.withAuth { access ->
            val request = EditSubtaskRequest(subtask.content, subtask.isDone)
            val response = dataSource.editSubtask(access, subtask.id!!, request)
            return Subtask(
                id = response.id,
                content = response.content,
                isDone = response.is_done,
            )
        }
        error("")
    }

    override suspend fun deleteSubtask(subtask: Subtask) {
        authService.withAuth { access ->
            dataSource.deleteSubtask(access, subtask.id!!)
        }
    }

    private suspend fun updateTask(task: Task): Task {
        authService.withAuth { access ->
            val taskRequest = EditTaskRequest(
                text = task.content,
                dpc = task.toDpc(),
            )
            val response = dataSource.editTask(access, task.id!!, taskRequest)
            return response.toModel()
        }
        error("")
    }

    override suspend fun deleteTask(task: Task) {
        authService.withAuth { access ->
            dataSource.deleteTask(access, task.id!!)
        }
        Log.d(TAG, "deleteTask: delete complete, updating cache")
    }

    private fun TaskResponse.toModel(): Task = Task(
        id = id,
        content = content,
        deadline = dpc.deadline?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        },
        isHighPriority = when (dpc.priority) {
            null -> false
            0 -> false
            else -> true
        },
        subtasks = subtasks.map { subtask ->
            Subtask(
                id = subtask.id,
                content = subtask.content,
                isDone = subtask.is_done,
            )
        },
        categoryId = dpc.category_id,
    )

    private fun Task.toDpc(): TaskDpc = TaskDpc(
        deadline = deadline?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        priority = if (isHighPriority) 1 else 0,
        category_id = this.categoryId,
    )
}
