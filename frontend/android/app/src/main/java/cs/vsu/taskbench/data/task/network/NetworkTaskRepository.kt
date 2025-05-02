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
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(it) as LocalDateTime?
                    },
                    isHighPriority = when (task.dpc.priority) {
                        null -> false
                        0 -> false
                        else -> true
                    },
                    subtasks = task.subtasks.map { subtask ->
                        Subtask(
                            id = subtask.id,
                            content = subtask.content,
                            isDone = subtask.is_done,
                        )
                    },
                    categoryId = task.dpc.category_id,
                )
            }
        }
        error("Could not get tasks from the server")
    }

    override suspend fun saveTask(task: Task): Task {
        Log.d(TAG, "saveTask: saving task $task")
        return if (task.id == null) createTask(task) else updateTask(task)
    }

    private suspend fun createTask(task: Task): Task {
        Log.d(TAG, "createTask: enter")
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
            return result
        }
        Log.e(TAG, "createTask: failed to create task")
        error("Could not create task")
    }

    private suspend fun updateTask(task: Task): Task {
        TODO()
        Log.d(TAG, "updateTask: enter")
        authService.withAuth { access ->
        }
        return task
    }

    override suspend fun deleteTask(task: Task) {
        Log.d(TAG, "deleteTask: task='$task'")
        authService.withAuth { access ->
            dataSource.deleteTask(access, task.id!!)
        }
        Log.d(TAG, "deleteTask: delete complete, updating cache")
    }

    private fun TaskResponse.toModel(): Task = Task(
        id = id,
        content = content,
        deadline = dpc.deadline?.let {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(it) as LocalDateTime?
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
