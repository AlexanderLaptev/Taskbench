package cs.vsu.taskbench.data.task.network

import android.util.Log
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.withAuth
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NetworkTaskRepository(
    private val authService: AuthService,
    private val dataSource: NetworkTaskDataSource,
) : TaskRepository {
    companion object {
        private val TAG = NetworkTaskRepository::class.simpleName
    }

    private var cache = emptyList<Task>()

    override suspend fun preload() {
        getTasksInCategory()
    }

    override suspend fun getTasksInCategory(
        date: LocalDate?,
        categoryId: Int?,
        sortBy: TaskRepository.SortByMode,
    ): List<Task> {
        Log.d(TAG, "getTasksInCategory: enter")
        updateCache(date, sortBy, categoryId)
        return cache
    }

    private suspend fun updateCache(
        date: LocalDate? = null,
        sortBy: TaskRepository.SortByMode = TaskRepository.SortByMode.Priority,
        categoryId: Int? = null,
    ) {
        Log.d(TAG, "updateCache: date='$date'")
        Log.d(TAG, "updateCache: categoryId='$categoryId'")
        Log.d(TAG, "updateCache: sortBy='$sortBy'")
        authService.withAuth { access ->
            val response = dataSource.getAllTasks(
                access,
                offset = 0,
                limit = 1000,
                date = date?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                categoryId = categoryId,
                sortBy = when (sortBy) {
                    TaskRepository.SortByMode.Priority -> "priority"
                    TaskRepository.SortByMode.Deadline -> "deadline"
                },
            )
            cache = response.map { task ->
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
    }

    override suspend fun getTasks(date: LocalDate?, sortBy: TaskRepository.SortByMode): List<Task> {
        Log.d(TAG, "getTasks: enter")
        updateCache(date, sortBy, null)
        return cache
    }

    override suspend fun saveTask(task: Task): Task? {
        Log.d(TAG, "saveTask: task='$task'")
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
            updateCache()
            Log.d(TAG, "saveTask: success: '$task'")
            return result
        }
        Log.d(TAG, "saveTask: failure")
        return null
    }

    override suspend fun deleteTask(task: Task) {
        Log.d(TAG, "deleteTask: task='$task'")
        authService.withAuth { access ->
            dataSource.deleteTask(access, task.id!!)
        }
        Log.d(TAG, "deleteTask: delete complete, updating cache")
        updateCache()
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
