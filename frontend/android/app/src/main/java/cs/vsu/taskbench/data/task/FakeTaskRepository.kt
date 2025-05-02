package cs.vsu.taskbench.data.task

import android.util.Log
import androidx.collection.MutableIntObjectMap
import androidx.collection.mutableIntObjectMapOf
import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import cs.vsu.taskbench.util.MockRandom
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FakeTaskRepository(
    private val categoryRepository: CategoryRepository,
) : TaskRepository, PreloadRepository {
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

    override suspend fun preload() {
        Log.d(TAG, "preloading fake tasks")
        categories = categoryRepository.getAllCategories()
        Log.d(TAG, "loaded ${categories.size} categories")

        dropIndex()
        index[taskId] = Task(
            id = taskId,
            content = "Помыть машину",
            deadline = LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 0)),
            isHighPriority = false,
            subtasks = listOf(
                Subtask(subtaskId++, "Закупить моющие средства", true),
                Subtask(subtaskId++, "Купить новую губку", false),
            ),
            categoryId = 3,
        )
        taskId++

        index[taskId] = Task(
            id = taskId,
            content = "Научиться рисовать к концу года",
            deadline = LocalDateTime.of(
                LocalDate.now().plusDays(-5).plusYears(1),
                LocalTime.of(12, 0)
            ),
            isHighPriority = false,
            subtasks = listOf(
                Subtask(subtaskId++, "Найти материалы для обучения", false),
                Subtask(subtaskId++, "Купить необходимые инструменты", false),
                Subtask(subtaskId++, "Создать рабочее место", false),
                Subtask(subtaskId++, "Начать выполнять упражнения", false),
            ),
            categoryId = 2,
        )
        taskId++
    }

    override suspend fun getTasks(
        categoryFilter: CategoryFilterState,
        sortByMode: TaskRepository.SortByMode,
        deadline: LocalDate?,
    ): List<Task> {
        Log.d(TAG, "getTasks: enter")
        val result = mutableListOf<Task>()
        index.forEachValue { result += it }

        val comparator: Comparator<Task> = when (sortByMode) {
            TaskRepository.SortByMode.Priority -> compareByDescending { it.isHighPriority }
            TaskRepository.SortByMode.Deadline -> compareByDescending { it.deadline }
        }

        return result
            .asSequence()
            .let { sequence ->
                if (deadline != null) sequence.filter { it.deadline?.toLocalDate() == deadline }
                else sequence
            }
            .let { sequence ->
                when (categoryFilter) {
                    CategoryFilterState.Disabled -> sequence
                    is CategoryFilterState.Enabled -> sequence.filter {
                        it.categoryId == categoryFilter.category?.id
                    }
                }
            }
            .sortedWith(comparator.thenBy { it.id })
            .toList().also {
                Log.d(TAG, "returning ${it.size} tasks")
            }
    }

    private fun getTasks(
        date: LocalDate?,
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
            .filter { it.deadline?.toLocalDate() == date }  // TODO
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
        return saveTaskInternal(task)
    }

    private fun saveTaskInternal(task: Task): Task {
        if (task.id == null) { // create
            Log.d(TAG, "saveTaskInternal: creating task $task")
            val savedSub = task.subtasks.map { it.copy(id = subtaskId++) }
            val saved = task.copy(id = taskId, subtasks = savedSub)
            index[taskId] = saved
            taskId++
            return saved
        } else { // update
            Log.d(TAG, "saveTaskInternal: updating task $task")
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
