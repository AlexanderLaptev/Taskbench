package cs.vsu.taskbench.data.task

import android.util.Log
import androidx.collection.MutableIntObjectMap
import androidx.collection.mutableIntObjectMapOf
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import cs.vsu.taskbench.util.Lipsum
import cs.vsu.taskbench.util.MockRandom
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FakeTaskRepository(
    private val categoryRepository: CategoryRepository,
) : TaskRepository {
    companion object {
        private val TAG = FakeTaskRepository::class.simpleName
    }

    private val random = MockRandom

    private lateinit var index: MutableIntObjectMap<Task>
    private var taskId = -1
    private var subtaskId = -1

    private var categories = listOf<Category>()

    init {
        dropIndex()
    }

    private fun dropIndex() {
        index = mutableIntObjectMapOf()
        taskId = 1
        subtaskId = 1
    }

    override suspend fun preload(): Boolean {
        Log.d(TAG, "preloading fake tasks")
        categories = categoryRepository.getAllCategories()
        Log.d(TAG, "loaded ${categories.size} categories")

        dropIndex()
        Log.d(TAG, "random: ${random.nextInt()}")
        val first = LocalDate.now().minusDays(7)
        val totalDays = 2 * 7

        var generatedCount = 0
        for (day in 0..<totalDays) {
            val today = first.plusDays(day.toLong())
            val taskCount = random.nextInt(10, 20)
            repeat(taskCount) {
                val task = generateTask(today)
                saveTaskInternal(task)
                generatedCount++
            }
        }
        Log.d(TAG, "generated $generatedCount fake tasks")

        return true
    }

    private fun generateTask(today: LocalDate): Task {
        val deadline = LocalDateTime.of(
            today,
            LocalTime.of(
                random.nextInt(0, 24),
                random.nextInt(0, 60),
            )
        )

        val subtasks = mutableListOf<Subtask>()
        repeat(random.nextInt(0, 5)) {
            subtasks += Subtask(
                id = subtaskId,
                content = "[$subtaskId] ${Lipsum.get()}",
                isDone = random.nextBoolean(),
            )
            subtaskId++
        }

        val category = if (random.nextBoolean()) {
            categories.randomOrNull(random)
        } else null

        return Task(
            id = null,
            content = Lipsum.get(8, 30),
            deadline = deadline,
            isHighPriority = random.nextBoolean(),
            subtasks = subtasks,
            categoryId = category?.id,
        )
    }

    override suspend fun getTasksInCategory(
        date: LocalDate,
        categoryId: Int?,
        sortBy: TaskRepository.SortByMode,
    ): List<Task> = getTasks(
        date = date,
        sortBy = sortBy,
        filterByCategory = true,
        categoryId = categoryId
    )

    override suspend fun getTasks(date: LocalDate, sortBy: TaskRepository.SortByMode): List<Task> =
        getTasks(
            date = date,
            sortBy = sortBy,
            filterByCategory = false
        )

    private fun getTasks(
        date: LocalDate,
        sortBy: TaskRepository.SortByMode,
        filterByCategory: Boolean,
        categoryId: Int? = null,
    ): List<Task> {
        Log.d(
            TAG,
            "requested tasks: date=$date, sortBy=$sortBy, filterByCategory=$filterByCategory, categoryId=$categoryId"
        )
        val result = mutableListOf<Task>()
        index.forEachValue { result += it }

        val comparator: Comparator<Task> = when (sortBy) {
            TaskRepository.SortByMode.Priority -> compareByDescending { it.isHighPriority }
            TaskRepository.SortByMode.Deadline -> compareByDescending { it.deadline }
        }

        return result
            .asSequence()
            .filter { it.deadline.toLocalDate() == date }
            .let {
                if (filterByCategory) it.filter { task ->
                    task.categoryId == categoryId
                } else it
            }
            .sortedWith(comparator.thenBy { it.id })
            .toList().also {
                Log.d(TAG, "returning ${it.size} tasks")
            }
    }

    override suspend fun saveTask(task: Task): Task {
        Log.d(TAG, "saving task $task")
        return saveTaskInternal(task)
    }

    private fun saveTaskInternal(task: Task): Task {
        if (task.id == null) { // create
            val saved = task.copy(id = taskId)
            index[taskId] = saved
            taskId++
            return saved
        } else { // update
            check(index[task.id] != null) { "Attempted to update a non-existent task" }
            index[task.id] = task
            return task
        }
    }

    override suspend fun deleteTask(task: Task) {
        Log.d(TAG, "deleting task $task")
        check(task.id != null) { "Attempted to delete a non-existent task" }
        index.remove(task.id)
    }
}
