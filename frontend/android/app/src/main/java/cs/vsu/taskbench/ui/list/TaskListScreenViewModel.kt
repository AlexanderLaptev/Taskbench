package cs.vsu.taskbench.ui.list

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {
    companion object {
        private val TAG = TaskListScreenViewModel::class.simpleName
    }

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private var _selectedDate by mutableStateOf<LocalDate?>(null)
    var selectedDate: LocalDate?
        get() = _selectedDate
        set(value) {
            _selectedDate = value
            viewModelScope.launch { refreshTasks() }
        }

    private var _selectedCategory by mutableStateOf<Category?>(null)
    var selectedCategory: Category?
        get() = _selectedCategory
        set(value) {
            _selectedCategory = value
            viewModelScope.launch { refreshTasks() }
        }

    var sortByMode by mutableStateOf(SortByMode.Priority)

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            refreshTasks()
        }
    }

    fun setSubtaskChecked(task: Task, subtask: Subtask, isChecked: Boolean) {
        Log.d(TAG, "setSubtaskChecked: task=${task.id}; subtask=${subtask.id}; checked=$isChecked")
        viewModelScope.launch {
            taskRepository.saveTask(task)
            Log.d(TAG, "setSubtaskChecked: saved task")
            refreshTasks()
        }
    }

    private suspend fun refreshTasks() {
        Log.d(TAG, "refreshTasks: enter")
        _tasks.update {
            taskRepository.getTasks(_selectedDate, SortByMode.Priority)
        }
    }
}
