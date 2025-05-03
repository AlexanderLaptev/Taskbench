package cs.vsu.taskbench.ui.create

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import cs.vsu.taskbench.ui.component.dialog.TaskEditDialogStateHolder
import cs.vsu.taskbench.util.mutableEventFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TaskCreationScreenViewModel(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel(), TaskEditDialogStateHolder {
    companion object {
        private val TAG = TaskCreationScreenViewModel::class.simpleName
    }

    enum class Error {
        TaskNotSaved,
        CategoryNotSaved,
        SubtaskAlreadyExists,
    }

    private val _errorFlow = mutableEventFlow<Error>()
    val errorFlow = _errorFlow.asSharedFlow()

    override var taskInput by mutableStateOf("")

    override var subtasks: List<Subtask> by mutableStateOf(emptyList())
    override var subtaskInput by mutableStateOf("")

    private var _categoryInput by mutableStateOf("")
    override var categoryInput: String
        get() = _categoryInput
        set(value) {
            _categoryInput = value
            if (showCategoryDialog) updateCategories()
        }

    private var _suggestions by mutableStateOf<List<String>>(emptyList())
    override var suggestions: List<String> = _suggestions

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

    private var _selectedCategory by mutableStateOf<Category?>(null)
    override var selectedCategory: Category?
        get() = _selectedCategory
        set(value) {
            _selectedCategory = value
        }

    override var categories: List<Category> = listOf()

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
        viewModelScope.launch {
            val task = Task(
                id = null,
                content = taskInput,
                deadline = deadline,
                isHighPriority = isHighPriority,
                subtasks = subtasks,
                categoryId = selectedCategory?.id,
            )

            try {
                taskRepository.saveTask(task)
                Log.d(TAG, "onSubmitTask: success")
            } catch (e: Exception) {
                Log.e(TAG, "onSubmitTask: error while saving task", e)
                _errorFlow.tryEmit(Error.TaskNotSaved)
            }
        }
    }

    // TODO: forbid saving duplicate subtasks
    override fun onEditSubtask(subtask: Subtask, newText: String) {
        val result = subtasks.toMutableList()
        val index = subtasks.indexOf(subtask)

        val edited = subtask.copy(content = newText)
        if (edited in subtasks) {
            Log.d(TAG, "onEditSubtask: already exists")
            _errorFlow.tryEmit(Error.SubtaskAlreadyExists)
            return
        }

        result[index] = edited
        subtasks = result
        Log.d(TAG, "onEditSubtask: success")
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
        addSubtask(suggestion)
        Log.d(TAG, "onAddSuggestion: success")
    }

    override fun onAddCategory() {
        viewModelScope.launch {
            val category = Category(id = null, name = _categoryInput)
            try {
                categoryRepository.saveCategory(category)
                Log.d(TAG, "onAddCategory: success")
            } catch (e: Exception) {
                Log.e(TAG, "onAddCategory: error while saving", e)
                _errorFlow.tryEmit(Error.CategoryNotSaved)
            }
        }
    }

    private fun addSubtask(text: String): Boolean {
        val subtask = Subtask(
            id = null,
            content = text,
            isDone = false,
        )
        if (subtask in subtasks) {
            Log.d(TAG, "addSubtask: already exists")
            _errorFlow.tryEmit(Error.SubtaskAlreadyExists)
            return false
        }
        subtasks = subtasks + subtask
        return true
    }

    private fun updateCategories() {
        viewModelScope.launch {
            try {
                categories = categoryRepository.getAllCategories(_categoryInput)
                Log.d(TAG, "updateCategories: success")
            } catch (e: Exception) {
                Log.e(TAG, "updateCategories: error while refreshing", e)
            }
        }
    }
}
