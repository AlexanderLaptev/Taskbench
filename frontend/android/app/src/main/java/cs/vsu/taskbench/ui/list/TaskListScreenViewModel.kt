package cs.vsu.taskbench.ui.list

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.task.CategoryFilterState
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.data.task.TaskRepository.SortByMode
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskListScreenViewModel(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    companion object {
        private val TAG = TaskListScreenViewModel::class.simpleName
    }

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private var _selectedDate by mutableStateOf<LocalDate?>(LocalDate.now())
    var selectedDate: LocalDate?
        get() = _selectedDate
        set(value) {
            _selectedDate = value
            refreshTasks()
        }

    private var _categoryFilterState by mutableStateOf<CategoryFilterState>(CategoryFilterState.Disabled)
    var categoryFilterState: CategoryFilterState
        get() = _categoryFilterState
        set(value) {
            _categoryFilterState = value
            refreshTasks()
        }

    private var _categorySearchQuery by mutableStateOf("")
    var categorySearchQuery: String
        get() = _categorySearchQuery
        set(value) {
            _categorySearchQuery = value
            refreshCategories()
        }

    var sortByMode by mutableStateOf(SortByMode.Priority)

    init {
        viewModelScope.launch {
            refreshCategories()
            refreshTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            refreshTasks()
        }
    }

    fun setSubtaskChecked(task: Task, subtask: Subtask, isChecked: Boolean) {
        Log.d(TAG, "setSubtaskChecked: task=${task.id}; subtask=${subtask.id}; checked=$isChecked")
        viewModelScope.launch {
            val subtasks = task.subtasks.toMutableList()
            subtasks[subtasks.indexOf(subtask)] = subtask.copy(isDone = isChecked)
            taskRepository.saveTask(task.copy(subtasks = subtasks))
            Log.d(TAG, "setSubtaskChecked: saved task")
            refreshTasks()
        }
    }

    private fun refreshTasks() {
        Log.d(TAG, "refreshTasks: enter")
        viewModelScope.launch {
            _tasks.update {
                taskRepository.getTasks(
                    categoryFilterState,
                    sortByMode,
                    selectedDate,
                )
            }
        }
    }

    private fun refreshCategories() {
        Log.d(TAG, "refreshCategories: enter")
        viewModelScope.launch {
            _categories.update { categoryRepository.getAllCategories(_categorySearchQuery) }
        }
    }
}
