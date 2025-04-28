package cs.vsu.taskbench.ui.create

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import cs.vsu.taskbench.util.mutableEventFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    enum class Error {
        BlankCategory,
    }

    private val _errorFlow = mutableEventFlow<Error>()
    val errorFlow = _errorFlow.asSharedFlow()

    private var _isCategorySelectionDialogVisible by mutableStateOf(false)
    var isCategorySelectionDialogVisible: Boolean
        get() = _isCategorySelectionDialogVisible
        set(value) {
            _isCategorySelectionDialogVisible = value
            if (value) {
                _categorySearchQuery = ""
                updateCategories(_categorySearchQuery)
            }
        }

    private var _contentInput by mutableStateOf("")
    var contentInput: String
        get() = _contentInput
        set(value) {
            _contentInput = value
            updateSuggestions(value)
        }

    var subtaskInput by mutableStateOf("")

    var deadline by mutableStateOf<LocalDateTime?>(null)

    var selectedCategory by mutableStateOf<Category?>(null)
    var isHighPriority by mutableStateOf(false)

    val subtasks = mutableStateListOf<Subtask>()
    var suggestedSubtasks by mutableStateOf<List<Subtask>>(emptyList())

    private var _categorySearchQuery by mutableStateOf("")
    var categorySearchQuery: String
        get() = _categorySearchQuery
        set(value) {
            _categorySearchQuery = value
            updateCategories(value)
        }

    var categorySearchResults by mutableStateOf<List<Category>>(emptyList())

    fun saveTask() {
        viewModelScope.launch {
            taskRepository.saveTask(
                Task(
                    id = null,
                    content = contentInput,
                    deadline = deadline,
                    isHighPriority = isHighPriority,
                    subtasks = subtasks,
                    categoryId = selectedCategory?.id,
                )
            )
        }
        clearInput()
    }

    fun updateCategories(query: String = "") {
        viewModelScope.launch {
            val categories = categoryRepository.getAllCategories(query)
            Log.d(TAG, "updateCategories: returned ${categories.size} categories")
            categorySearchResults = categories.sortedWith(
                compareBy<Category> { it.name }.thenBy { it.id }
            )
        }
    }

    private fun clearInput() {
        subtaskInput = ""
        contentInput = ""
        selectedCategory = null
        deadline = null
        subtasks.clear()
        suggestedSubtasks = emptyList()
    }

    fun addSubtask() {
        val subtask = Subtask(id = null, content = subtaskInput, isDone = false)
        subtasks += subtask
    }

    fun addSuggestion(suggestion: Subtask) {
        subtasks += suggestion
        suggestedSubtasks -= suggestion
    }

    fun removeSubtask(subtask: Subtask) {
        subtasks -= subtask
    }

    private fun updateSuggestions(prompt: String) {
        Log.d(TAG, "updateSuggestions: prompt=$prompt")
        viewModelScope.launch {
            // TODO: add delay after last change before sending
            suggestedSubtasks = emptyList()
            val newSuggestions = suggestionRepository.getSuggestions(prompt)
            suggestedSubtasks = newSuggestions.subtasks.map { content ->
                Subtask(id = null, content = content, isDone = false)
            }
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) {
            _errorFlow.tryEmit(Error.BlankCategory)
            return
        }
        viewModelScope.launch {
            val category = categoryRepository.saveCategory(Category(id = null, name = name))
            selectedCategory = category
            _categorySearchQuery = ""
            isCategorySelectionDialogVisible = false
        }
    }
}
