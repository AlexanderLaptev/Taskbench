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
import cs.vsu.taskbench.util.mutableEventFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.time.LocalDate

class TaskListScreenViewModel(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    companion object {
        private val TAG = TaskListScreenViewModel::class.simpleName
    }

    enum class Error {
        CouldNotConnect,
        Unknown,
    }

    private val _errorFlow = mutableEventFlow<Error>()
    val errorFlow = _errorFlow.asSharedFlow()

    private val _tasks = MutableStateFlow<List<Task>?>(null)
    val tasks = _tasks.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private var _selectedDate by mutableStateOf<LocalDate?>(null)
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
        catchErrorsAsync {
            refresh()
            Log.d(TAG, "init: success")
        }
    }

    fun deleteTask(task: Task) {
        catchErrorsAsync {
            taskRepository.deleteTask(task)
            Log.d(TAG, "deleteTask: success")
            refreshTasks()
        }
    }

    fun setSubtaskChecked(subtask: Subtask, isChecked: Boolean) {
        catchErrorsAsync {
            val changed = subtask.copy(isDone = isChecked)
            taskRepository.updateSubtask(changed)
            Log.d(TAG, "setSubtaskChecked: saved task")
            refreshTasks()
        }
    }

    fun refresh() {
        refreshCategories()
        refreshTasks()
    }

    private fun refreshTasks() {
        _tasks.update { null }
        catchErrorsAsync {
            var result: List<Task>
            try {
                result = taskRepository.getTasks(
                    categoryFilterState,
                    sortByMode,
                    selectedDate,
                )
            } catch (e: Exception) {
                _tasks.update { emptyList() }
                throw e
            }

            (_categoryFilterState as? CategoryFilterState.Enabled)?.let { state ->
                if (state.category == null) {
                    result = result.filter { it.categoryId == 0 }
                }
            }

            Log.d(TAG, "refreshTasks: success")
            _tasks.update { result }
        }
    }

    private fun refreshCategories() {
        catchErrorsAsync {
            _categories.update { categoryRepository.getAllCategories(_categorySearchQuery) }
            Log.d(TAG, "refreshCategories: success")
        }
    }

    private inline fun catchErrorsAsync(crossinline block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: ConnectException) {
                Log.e(TAG, "catchErrors: connection error", e)
                _errorFlow.tryEmit(Error.CouldNotConnect)
            } catch (e: Exception) {
                Log.e(TAG, "catchErrors: unknown error", e)
                _errorFlow.tryEmit(Error.Unknown)
            }
        }
    }
}
