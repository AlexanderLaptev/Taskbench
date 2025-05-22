package cs.vsu.taskbench.ui.component.dialog.edit

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
import cs.vsu.taskbench.util.mutableEventFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.time.LocalDateTime

class TaskEditDialogViewModel(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val suggestionRepository: SuggestionRepository,
) : ViewModel(), TaskEditDialogStateHolder {
    companion object {
        private val TAG = TaskEditDialogViewModel::class.simpleName
    }

    enum class Error {
        CouldNotConnect,
        Unknown,
        Timeout,
    }

    private val _errorFlow = mutableEventFlow<Error>()
    val errorFlow = _errorFlow.asSharedFlow()

    private val _submitEventFlow = mutableEventFlow<Unit>()
    val submitEventFlow = _submitEventFlow.asSharedFlow()

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

    override var isDeadlineSetManually: Boolean = false

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

    override var editTask: Task? = null
        set(task) {
            field = task

            _taskInput = task?.content ?: ""
            _deadline = task?.deadline
            _isHighPriority = task?.isHighPriority ?: false

            _selectedCategory = if (task == null) null
            else categories.find { it.id == task.categoryId }

            subtasks = task?.subtasks ?: emptyList()
            suggestions = emptyList()
            subtaskInput = ""
            isDeadlineSetManually = false
        }

    override fun onSubmitTask() {
        catchErrorsAsync {
            val task = Task(
                id = editTask?.id,
                content = taskInput,
                deadline = deadline,
                isHighPriority = isHighPriority,
                subtasks = subtasks,
                categoryId = selectedCategory?.id,
            )
            taskRepository.saveTask(task)
            if (editTask == null) editTask = null // clear input
            _submitEventFlow.tryEmit(Unit)
            Log.d(TAG, "onSubmitTask: success")
        }
    }

    override fun onEditSubtask(subtask: Subtask, newText: String) {
        val result = subtasks.toMutableList()
        val index = subtasks.indexOf(subtask)
        result[index] = subtask.copy(content = newText)
        subtasks = result

        catchErrorsAsync {
            if (subtask.id != null) taskRepository.updateSubtask(subtask)
        }

        Log.d(TAG, "onEditSubtask: success")
    }

    override fun canSaveSubtask(text: String): Boolean {
        val strings = subtasks.map { it.content }
        return text !in strings
    }

    override fun onRemoveSubtask(subtask: Subtask) {
        subtasks = subtasks - subtask
        catchErrorsAsync {
            if (subtask.id != null) taskRepository.deleteSubtask(subtask)
        }
        Log.d(TAG, "onRemoveSubtask: success")
    }

    override fun onAddSubtask() {
        addSubtask(subtaskInput)
        subtaskInput = ""
        Log.d(TAG, "onAddSubtask: success")
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

    private fun addSubtask(text: String) {
        catchErrorsAsync {
            var subtask = Subtask(
                id = null,
                content = text,
                isDone = false,
            )
            if (editTask != null) subtask = taskRepository.createSubtask(editTask!!, subtask)
            subtasks = subtasks + subtask
        }
    }

    private fun updateCategories() {
        catchErrorsAsync {
            categories = categoryRepository.getAllCategories(_categoryInput).sortedBy { it.name }
            Log.d(TAG, "updateCategories: success")
        }
    }

    private inline fun catchErrorsAsync(crossinline block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                // TODO: figure out the root cause
                // ignoring for now
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "catchErrorsAsync: socket timeout", e)
                _errorFlow.tryEmit(Error.CouldNotConnect)
            } catch (e: ConnectException) {
                Log.e(TAG, "catchErrors: connection error", e)
                _errorFlow.tryEmit(Error.Timeout)
            } catch (e: Exception) {
                Log.e(TAG, "catchErrors: unknown error", e)
                _errorFlow.tryEmit(Error.Unknown)
            }
        }
    }

    private var pendingUpdate: Job? = null

    private fun updateSuggestions() {
        suggestions = emptyList() // clear the current suggestions
        pendingUpdate?.cancel() // cancel the previous request
        pendingUpdate = catchErrorsAsync {
            delay(1200)
            Log.d(TAG, "updateSuggestions: updating suggestions")
            // Do not send empty requests (the server will return HTTP 400 anyway).
            if (_taskInput.length < 8) return@catchErrorsAsync
            val response = suggestionRepository.getSuggestions(
                prompt = taskInput,
                deadline = if (isDeadlineSetManually) _deadline else null,
                isHighPriority = _isHighPriority,
                category = _selectedCategory,
            )

            // Remove duplicates as those will crash the UI.
            val contents = subtasks.map { it.content }
            val newSuggestions = response.subtasks.fastDistinctBy { it }.toMutableList()
            newSuggestions.removeAll { it in contents }

            // Update the suggestions and DPC.
            suggestions = newSuggestions
            if (!isDeadlineSetManually) {
                Log.d(TAG, "updateSuggestions: updating deadline")
                _deadline = response.deadline
            }
            if (_selectedCategory == null) _selectedCategory = response.category
        }
    }
}
