package cs.vsu.taskbench.ui.create

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastDistinctBy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.data.task.suggestions.SuggestionRepository
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import cs.vsu.taskbench.ui.component.dialog.TaskEditDialogStateHolder
import cs.vsu.taskbench.util.mutableEventFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TaskCreationScreenViewModel(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val suggestionRepository: SuggestionRepository,
) : ViewModel(), TaskEditDialogStateHolder {
    companion object {
        private val TAG = TaskCreationScreenViewModel::class.simpleName
    }

    enum class Error {
        CouldNotConnect,
        Unknown,
    }

    private val _errorFlow = mutableEventFlow<Error>()
    val errorFlow = _errorFlow.asSharedFlow()

    private var _taskInput by mutableStateOf("")
    override var taskInput
        get() = _taskInput
        set(value) {
            _taskInput = value
            updateSuggestions()
        }

    // TODO: replace with strings
    override var subtasks: List<Subtask> by mutableStateOf(emptyList())
    override var subtaskInput by mutableStateOf("")

    override var categories: List<Category> by mutableStateOf(emptyList())

    private var _categoryInput by mutableStateOf("")
    override var categoryInput: String
        get() = _categoryInput
        set(value) {
            _categoryInput = value
            if (showCategoryDialog) updateCategories()
        }

    private var _selectedCategory by mutableStateOf<Category?>(null)
    override var selectedCategory: Category?
        get() = _selectedCategory
        set(value) {
            _selectedCategory = value
        }

    init {
        viewModelScope.launch { updateCategories() }
    }

    override var suggestions: List<String> by mutableStateOf(emptyList())

    private var _deadline by mutableStateOf<LocalDateTime?>(null)
    override var deadline: LocalDateTime?
        get() = _deadline
        set(value) {
            _deadline = value
        }

    private var _isHighPriority by mutableStateOf(false)
    override var isHighPriority: Boolean
        get() = _isHighPriority
        set(value) {
            _isHighPriority = value
        }

    private var _showDeadlineDialog by mutableStateOf(false)
    override var showDeadlineDialog: Boolean
        get() = _showDeadlineDialog
        set(value) {
            _showDeadlineDialog = value
        }

    private var _showCategoryDialog by mutableStateOf(false)
    override var showCategoryDialog: Boolean
        get() = _showCategoryDialog
        set(value) {
            _showCategoryDialog = value
        }

    override fun onPriorityChipClick() {
        _isHighPriority = !_isHighPriority
    }

    override fun onDeadlineChipClick() {
        _showDeadlineDialog = true
    }

    override fun onCategoryChipClick() {
        categoryInput = ""
        _showCategoryDialog = true
        updateCategories()
    }

    override fun onCategoryClick(category: Category) {
        _selectedCategory = category
        _showCategoryDialog = false
    }

    override fun onSetDeadlineDate(epochMilli: Long) {
        val instant = Instant.ofEpochMilli(epochMilli)
        val date = LocalDate.ofInstant(instant, ZoneId.systemDefault())
        val time = _deadline?.toLocalTime() ?: LocalTime.now()
        val newDeadline = LocalDateTime.of(date, time).let {
            if (_deadline == null) it.plusHours(1) else it
        }
        _deadline = newDeadline
    }

    override fun onSetDeadlineTime(hour: Int, minute: Int) {
        val date = _deadline?.toLocalDate() ?: LocalDate.now()
        _deadline = LocalDateTime.of(date, LocalTime.of(hour, minute))
    }

    override fun onClearDeadline() {
        _deadline = null
    }

    override fun onSubmitTask() {
        catchErrorsAsync {
            val task = Task(
                id = null,
                content = taskInput,
                deadline = deadline,
                isHighPriority = isHighPriority,
                subtasks = subtasks,
                categoryId = selectedCategory?.id,
            )
            taskRepository.saveTask(task)
            clearInput()
            Log.d(TAG, "onSubmitTask: success")
        }
    }

    private fun clearInput() {
        _taskInput = ""
        _deadline = null
        _isHighPriority = false
        _selectedCategory = null

        subtaskInput = ""
        subtasks = emptyList()
        suggestions = emptyList()
    }

    override fun onEditSubtask(subtask: Subtask, newText: String) {
        val result = subtasks.toMutableList()
        val index = subtasks.indexOf(subtask)
        result[index] = subtask.copy(content = newText)
        subtasks = result
        Log.d(TAG, "onEditSubtask: success")
    }

    override fun canSaveSubtask(text: String): Boolean {
        val strings = subtasks.map { it.content }
        return text !in strings
    }

    override fun onRemoveSubtask(subtask: Subtask) {
        subtasks = subtasks - subtask
        Log.d(TAG, "onRemoveSubtask: success")
    }

    override fun onAddSubtask() {
        if (addSubtask(subtaskInput)) {
            subtaskInput = ""
            Log.d(TAG, "onAddSubtask: success")
        }
    }

    override fun onAddSuggestion(suggestion: String) {
        suggestions = suggestions - suggestion
        addSubtask(suggestion)
        Log.d(TAG, "onAddSuggestion: success")
    }

    override fun onAddCategory() {
        catchErrorsAsync {
            val category = Category(id = null, name = _categoryInput)
            _selectedCategory = categoryRepository.saveCategory(category)
            showCategoryDialog = false
            Log.d(TAG, "onAddCategory: success")
        }
    }

    private fun addSubtask(text: String): Boolean {
        val subtask = Subtask(
            id = null,
            content = text,
            isDone = false,
        )
        subtasks = subtasks + subtask
        return true
    }

    private fun updateCategories() {
        catchErrorsAsync {
            categories = categoryRepository.getAllCategories(_categoryInput).sortedBy { it.name }
            Log.d(TAG, "updateCategories: success")
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

    private var pendingUpdate: Job? = null

    private fun updateSuggestions() {
        suggestions = emptyList()
        if (_taskInput.length < 8) return
        pendingUpdate?.cancel()
        pendingUpdate = viewModelScope.launch {
            delay(1200)
            try {
                if (_taskInput.length < 8) return@launch
                val response = suggestionRepository.getSuggestions(
                    prompt = taskInput,
                    deadline = _deadline,
                    isHighPriority = _isHighPriority,
                    category = _selectedCategory,
                )

                // TODO: refactor
                val contents = subtasks.map { it.content }
                val newSuggestions = response.subtasks.fastDistinctBy { it }.toMutableList()
                newSuggestions.removeAll { it in contents }

                suggestions = newSuggestions
                if (_deadline == null) _deadline = response.deadline
                if (_selectedCategory == null) _selectedCategory = response.category
                Log.d(TAG, "updateSuggestions: success")
            } catch (e: ConnectException) {
                Log.e(TAG, "updateSuggestions: connection error", e)
                _errorFlow.tryEmit(Error.CouldNotConnect)
            } catch (e: Exception) {
                Log.e(TAG, "updateSuggestions: error during fetch", e)
                _errorFlow.tryEmit(Error.Unknown)
            }
        }
    }
}
