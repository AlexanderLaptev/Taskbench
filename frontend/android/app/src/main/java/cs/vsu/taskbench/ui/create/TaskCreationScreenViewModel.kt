package cs.vsu.taskbench.ui.create

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.task.SuggestionRepository
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TaskCreationScreenViewModel(
    private val taskRepository: TaskRepository,
    private val suggestionRepository: SuggestionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    companion object {
        private val TAG = TaskCreationScreenViewModel::class.simpleName
    }

    var subtaskInput by mutableStateOf("")
    var contentInput by mutableStateOf("")

    private var selectedCategoryId by mutableStateOf<Int?>(null)
    private var taskDeadline by mutableStateOf<LocalDateTime?>(null)
    private var isTaskHighPriority by mutableStateOf(false)

    private val _category = MutableStateFlow<Category?>(null)
    val category = _category.asStateFlow()

    private val _categorySearchResults = MutableStateFlow<List<Category>>(emptyList())
    val categorySearchResults = _categorySearchResults.asStateFlow()

    private val _deadline = MutableStateFlow<LocalDateTime?>(null)
    val deadline = _deadline.asStateFlow()

    private val _highPriority = MutableStateFlow(false)
    val highPriority = _highPriority.asStateFlow()

    private val _subtasks = MutableStateFlow<List<Subtask>>(emptyList())
    val subtasks = _subtasks.asStateFlow()

    private val _suggestions = MutableStateFlow<List<Subtask>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    fun saveTask() {
        viewModelScope.launch {
            taskRepository.saveTask(
                Task(
                    id = null,
                    content = contentInput,
                    deadline = taskDeadline!!, // TODO: handle missing deadline
                    isHighPriority = isTaskHighPriority,
                    subtasks = _subtasks.value,
                    categoryId = selectedCategoryId,
                )
            )
        }
        clearInput()
    }

    fun updateDpc(category: Category, deadline: LocalDateTime, isHighPriority: Boolean) {
        selectedCategoryId = category.id
        taskDeadline = deadline
        isTaskHighPriority = isHighPriority
    }

    fun updateCategories(query: String = "") {
        if (query.isEmpty()) {
            Log.d(TAG, "updateCategories: empty query")
            viewModelScope.launch {
                val categories = categoryRepository.getAllCategories()
                Log.d(TAG, "updateCategories: returned ${categories.size} categories")
                _categorySearchResults.update { categories }
            }
        }
    }

    private fun clearInput() {
        subtaskInput = ""
        contentInput = ""
        selectedCategoryId = null
        taskDeadline = null
        _subtasks.update { emptyList() }
        _suggestions.update { emptyList() }
    }

    fun addSubtask() {
        val subtask = Subtask(id = null, content = subtaskInput, isDone = false)
        _subtasks.update { it + subtask }
    }

    fun addSuggestion(subtask: Subtask) {
        _subtasks.update { it + subtask }
        _suggestions.update { it - subtask }
    }

    fun removeSubtask(subtask: Subtask) {
        _subtasks.update { it - subtask }
    }

    fun updateSuggestions(it: String) {
        viewModelScope.launch {
            // TODO: add delay after last change before sending
            _suggestions.update { emptyList() }
            val newSuggestions = suggestionRepository.getSuggestions(it)
            _suggestions.update {
                newSuggestions.subtasks.map { content ->
                    Subtask(id = null, content = content, isDone = false)
                }
            }
        }
    }
}
