package cs.vsu.taskbench.ui.component.dialog.edit

import androidx.compose.runtime.Stable
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import java.time.LocalDateTime

@Stable
interface TaskEditDialogStateHolder {
    var taskInput: String
    var deadline: LocalDateTime?
    var isHighPriority: Boolean
    var isDeadlineSetManually: Boolean

    var subtaskInput: String
    var subtasks: List<Subtask>

    var suggestions: List<String>?

    var categories: List<Category>
    var selectedCategory: Category?
    var categoryInput: String

    var showDeadlineDialog: Boolean
    var showCategoryDialog: Boolean

    var editTask: Task?

    fun onSubmitTask()
    fun onAddSuggestion(suggestion: String)

    fun onAddCategory()
    fun onCategoryClick(category: Category)

    fun onAddSubtask()
    fun onEditSubtask(subtask: Subtask, newText: String)
    fun onRemoveSubtask(subtask: Subtask)
    fun canSaveSubtask(text: String): Boolean

    fun onDeadlineChipClick()

    fun onPriorityChipClick()
    fun onCategoryChipClick()
}
