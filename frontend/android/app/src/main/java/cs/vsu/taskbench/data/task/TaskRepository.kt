package cs.vsu.taskbench.data.task

import cs.vsu.taskbench.model.Task

interface TaskRepository {
    suspend fun getAllTasks(): List<Task>

    suspend fun getAllTasksByCategoryId(id: Int): List<Task>
}
