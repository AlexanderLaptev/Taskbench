@file:OptIn(ExperimentalMaterial3Api::class)

package cs.vsu.taskbench.ui.component.dialog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.ui.component.AddedSubtask
import cs.vsu.taskbench.ui.component.BoxEdit
import cs.vsu.taskbench.ui.component.Chip
import cs.vsu.taskbench.ui.component.SubtaskCreationField
import cs.vsu.taskbench.ui.component.SuggestedSubtask
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.launch
import java.time.LocalDate

interface TaskEditDialogStateHolder {
    var taskInput: String
    var subtaskInput: String
    val subtasks: List<Subtask>
    val suggestions: List<String>
    var deadline: LocalDate?
    var isHighPriority: Boolean

    var categories: List<Category>
    var selectedCategory: Category?
    var categoryInput: String

    var showDeadlineDialog: Boolean
    var showCategoryDialog: Boolean

    fun onSubmitTask()
    fun onAddSuggestion(suggestion: String)

    fun onAddCategory()
    fun onCategoryClick(category: Category)

    fun onAddSubtask()
    fun onEditSubtask(text: String)
    fun onRemoveSubtask()

    fun onDeadlineChipClick()
    fun onPriorityChipClick()
    fun onCategoryChipClick()
}

@Composable
fun TaskEditDialog(
    stateHolder: TaskEditDialogStateHolder,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    if (stateHolder.showDeadlineDialog) {
        DeadlineDialog(stateHolder)
    }

    val categorySheetState = rememberModalBottomSheetState()
    LaunchedEffect(stateHolder.showCategoryDialog) {
        scope.launch {
            if (stateHolder.showCategoryDialog) {
                categorySheetState.show()
            } else categorySheetState.hide()
        }
    }
    if (stateHolder.showCategoryDialog || categorySheetState.isVisible) {
        BottomSheetCategoryDialog(
            sheetState = categorySheetState,
            mode = CategoryDialogMode.Select,
            categories = stateHolder.categories,
            input = stateHolder.categoryInput,

            actions = remember {
                object : CategoryDialogActions {
                    override fun onInputChange(input: String) {
                        stateHolder.categoryInput = input
                    }

                    override fun onSelect(category: Category) {
                        stateHolder.selectedCategory = category
                        onDismiss()
                    }

                    override fun onDismiss() {
                        stateHolder.showCategoryDialog = false
                    }

                    override fun onDeselect() {
                        stateHolder.selectedCategory = null
                        onDismiss()
                    }
                }
            },
        )
    }

    Column(
        modifier = modifier,
    ) {
        SubtaskCreationField(
            text = stateHolder.subtaskInput,
            onTextChange = { stateHolder.subtaskInput = it },
            placeholder = stringResource(R.string.placeholder_enter_subtask),
            onAddButtonClick = stateHolder::onAddSubtask,
        )

        Spacer(Modifier.height(8.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth(),
        ) {
            val subtasks = stateHolder.subtasks
            val suggestions = stateHolder.suggestions

            if (subtasks.isNotEmpty()) {
                item { SubtaskSection(stringResource(R.string.label_subtask_list)) }
                items(subtasks, key = { it.content }) { subtask ->
                    AddedSubtask(
                        text = subtask.content,
                        onTextChange = stateHolder::onEditSubtask,
                        onRemove = stateHolder::onRemoveSubtask,
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            if (suggestions.isNotEmpty()) {
                item { SubtaskSection(stringResource(R.string.label_suggestion_list)) }
                items(suggestions, key = { it }) { suggestion ->
                    SuggestedSubtask(
                        text = suggestion,
                        onAdd = { stateHolder.onAddSuggestion(suggestion) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        EditAreaChips(stateHolder = stateHolder)

        Spacer(Modifier.height(8.dp))
        BoxEdit(
            value = stateHolder.taskInput,
            onValueChange = { stateHolder.taskInput = it },
            buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
            inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
            placeholder = stringResource(R.string.placeholder_enter_task),
            onClick = stateHolder::onSubmitTask,
            modifier = Modifier.imePadding(),
        )
    }
}

@Composable
private fun DeadlineDialog(
    stateHolder: TaskEditDialogStateHolder,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = White,
        onDismissRequest = { stateHolder.showDeadlineDialog = false },
        modifier = modifier,
    ) {}
}


@Composable
@NonRestartableComposable
private fun SubtaskSection(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontWeight = FontWeight.Medium,
        color = DarkGray,
        modifier = modifier,
    )
}

@Composable
private fun EditArea(
    stateHolder: TaskEditDialogStateHolder,
) {
    EditAreaChips(stateHolder = stateHolder)
    Spacer(Modifier.height(8.dp))
    BoxEdit(
        value = stateHolder.taskInput,
        onValueChange = { stateHolder.taskInput = it },
        buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
        inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
        placeholder = stringResource(R.string.placeholder_enter_task),
        onClick = stateHolder::onSubmitTask,
        modifier = Modifier.imePadding(),
    )
}

@Composable
private fun EditAreaChips(
    stateHolder: TaskEditDialogStateHolder,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.horizontalScroll(rememberScrollState()),
    ) {
        // Deadline chip
        val deadlineContentColor = if (stateHolder.deadline == null) LightGray else Black
        Chip(
            text = stringResource(R.string.chip_deadline),
            icon = painterResource(R.drawable.ic_clock),
            textColor = deadlineContentColor,
            iconTint = deadlineContentColor,
            color = White,
            onClick = stateHolder::onDeadlineChipClick,
        )

        // Priority chip
        val priorityColor = if (stateHolder.isHighPriority) AccentYellow else White
        val priorityResId = if (stateHolder.isHighPriority) {
            R.string.priority_high
        } else R.string.priority_normal
        Chip(
            text = stringResource(priorityResId),
            color = priorityColor,
            textColor = Black,
            onClick = stateHolder::onPriorityChipClick,
        )

        // Category chip
        val categoryTextColor = if (stateHolder.selectedCategory == null) LightGray else Black
        val categoryText =
            stateHolder.selectedCategory?.name ?: stringResource(R.string.label_no_category)
        Chip(
            text = categoryText,
            color = White,
            textColor = categoryTextColor,
            onClick = stateHolder::onCategoryChipClick,
        )
    }
}

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

    override val subtasks: List<Subtask> = listOf(
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
    override val suggestions: List<String> = _suggestions

    private var _deadline by mutableStateOf<LocalDate?>(null)
    override var deadline: LocalDate?
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
    override fun onEditSubtask(text: String) = Unit
    override fun onAddSuggestion(suggestion: String) = Unit
    override fun onRemoveSubtask() = Unit
    override fun onAddCategory() = Unit
}

@Preview
@Composable
private fun Preview() {
    TaskbenchTheme {
        TaskEditDialog(MockTaskEditDialogStateHolder)
    }
}
