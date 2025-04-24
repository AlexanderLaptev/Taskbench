package cs.vsu.taskbench.data.task

import androidx.collection.mutableIntObjectMapOf
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.random.Random

class FakeTaskRepository(
    private val categoryRepository: CategoryRepository,
) : TaskRepository {
    companion object {
        private const val TASK_CONTENT =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas faucibus tellus eget mi iaculis, quis luctus nisl ornare."
        private const val SUBTASK_CONTENT = "Vivamus a dolor ac risus consectetur"
    }

    private val random = Random(4242)

    private val index = mutableIntObjectMapOf<Task>()

    private var categories = listOf<Category>()

    private var taskId = 1
    private var subtaskId = 1

    override suspend fun preload(): Boolean {
        categories = categoryRepository.getAllCategories()

        val first = LocalDate.now().minusDays(7)
        val totalDays = 2 * 7

        for (day in 0..<totalDays) {
            val today = first.plusDays(day.toLong())
            val taskCount = random.nextInt(1, 5)
            repeat(taskCount) {
                val task = generateTask(today)
                saveTask(task)
            }
        }

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
                content = SUBTASK_CONTENT,
                isDone = random.nextBoolean(),
            )
        }

        val category = if (random.nextBoolean()) {
            categories.randomOrNull(random)
        } else null

        return Task(
            id = null,
            content = TASK_CONTENT,
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
            .toList()
    }

    override suspend fun saveTask(task: Task): Task {
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
        check(task.id != null) { "Attempted to delete a non-existent task" }
        index.remove(task.id)
    }
}
