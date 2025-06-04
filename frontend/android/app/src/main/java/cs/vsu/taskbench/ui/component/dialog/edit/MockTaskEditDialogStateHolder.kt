package cs.vsu.taskbench.ui.component.dialog.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import java.time.LocalDateTime

object MockTaskEditDialogStateHolder : TaskEditDialogStateHolder {
    private var _taskInput by mutableStateOf("")
    override var taskInput: String
        get() = _taskInput
        set(value) {
            _taskInput = value
        }

    private var _subtaskInput by mutableStateOf("")
    override var subtaskInput: String
        get() = _subtaskInput
        set(value) {
            _subtaskInput = value
        }

    private var _categoryInput by mutableStateOf("")
    override var categoryInput: String
        get() = _categoryInput
        set(value) {
            _categoryInput = value
        }

    override var subtasks: List<Subtask> = listOf(
        Subtask(null, "Lorem ipsum dolor sit amet 1", false),
        Subtask(null, "Lorem ipsum dolor sit amet 2", false),
        Subtask(null, "Lorem ipsum dolor sit amet 3", false),
        Subtask(null, "Lorem ipsum dolor sit amet 4", false),
        Subtask(null, "Lorem ipsum dolor sit amet 5", false),
        Subtask(null, "Lorem ipsum dolor sit amet 6", false),
    )

    private var _suggestions = mutableListOf(
        "Lorem ipsum dolor sit amet 7",
        "Lorem ipsum dolor sit amet 8",
        "Lorem ipsum dolor sit amet 9",
        "Lorem ipsum dolor sit amet 10",
        "Lorem ipsum dolor sit amet 11",
        "Lorem ipsum dolor sit amet 12",
    )
    override var suggestions: List<String>? = _suggestions

    private var _deadline by mutableStateOf<LocalDateTime?>(null)
    override var deadline: LocalDateTime?
        get() = _deadline
        set(value) {
            _deadline = value
        }

    override var isDeadlineSetManually: Boolean = true

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

    override var categories: List<Category> = listOf(
        Category(1, "Lorem"),
        Category(2, "Ipsum"),
        Category(3, "Dolor"),
        Category(4, "Consectetur"),
        Category(5, "Adipiscing"),
    )

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

    override var editTask: Task? = null

    override fun onPriorityChipClick() {
        _isHighPriority = !_isHighPriority
    }

    override fun onDeadlineChipClick() {
        _showDeadlineDialog = true
    }

    override fun onCategoryChipClick() {
        _categoryInput = ""
        _showCategoryDialog = true
    }

    override fun onCategoryClick(category: Category) {
        _selectedCategory = category
        _showCategoryDialog = false
    }

    override fun onSubmitTask() = Unit
    override fun onAddSubtask() = Unit
    override fun onEditSubtask(subtask: Subtask, newText: String) = Unit
    override fun onAddSuggestion(suggestion: String) = Unit
    override fun onRemoveSubtask(subtask: Subtask) = Unit
    override fun canSaveSubtask(text: String): Boolean = true
    override fun onAddCategory() = Unit
}
