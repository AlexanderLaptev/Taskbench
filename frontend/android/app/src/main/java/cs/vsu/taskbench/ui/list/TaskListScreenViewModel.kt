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

    private var _sortByMode by mutableStateOf(SortByMode.Priority)
    var sortByMode: SortByMode
        get() = _sortByMode
        set(value) {
            _sortByMode = value
            refreshTasks()
        }

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
            val changed = subtask.copy(isDone = isChecked)
            taskRepository.saveSubtask(changed)
            Log.d(TAG, "setSubtaskChecked: saved task")
            refreshTasks()
        }
    }

    private fun refreshTasks() {
        viewModelScope.launch {
            _tasks.update {
                var result = taskRepository.getTasks(
                    categoryFilterState,
                    sortByMode,
                    selectedDate,
                )
                (_categoryFilterState as? CategoryFilterState.Enabled)?.let { state ->
                    if (state.category == null) {
                        result = result.filter { it.categoryId == 0 }
                    }
                }
                result
            }
        }
        Log.d(TAG, "refreshTasks: success")
    }

    private fun refreshCategories() {
        viewModelScope.launch {
            _categories.update { categoryRepository.getAllCategories(_categorySearchQuery) }
        }
        Log.d(TAG, "refreshCategories: success")
    }
}
