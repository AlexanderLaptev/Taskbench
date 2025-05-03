@file:OptIn(ExperimentalMaterial3Api::class)

package cs.vsu.taskbench.ui.component

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
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
    var category: Category?

    var showDeadlineDialog: Boolean
    var showCategoryDialog: Boolean

    fun onSubmitTask()
    fun onAddSubtask()
    fun onEditSubtask(text: String)
    fun onRemoveSubtask()
    fun onAddSuggestion(suggestion: String)
    fun onDeadlineChipClick()
    fun onPriorityChipClick()
    fun onCategoryChipClick()
}

@Composable
fun TaskEditDialog(
    stateHolder: TaskEditDialogStateHolder,
    modifier: Modifier = Modifier,
) {
    if (stateHolder.showDeadlineDialog) {
        DeadlineDialog(stateHolder)
    }

    if (stateHolder.showCategoryDialog) {
        CategoryDialog(stateHolder)
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
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = White,
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion { stateHolder.showDeadlineDialog = false }
        },
        modifier = modifier,
    ) {}
}

@Composable
private fun CategoryDialog(
    stateHolder: TaskEditDialogStateHolder,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = White,
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion { stateHolder.showCategoryDialog = false }
        },
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
        val categoryTextColor = if (stateHolder.category == null) LightGray else Black
        val categoryText = stateHolder.category?.name ?: stringResource(R.string.label_no_category)
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

    private var _category by mutableStateOf<Category?>(null)
    override var category: Category?
        get() = _category
        set(value) {
            _category = value
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
        _showCategoryDialog = true
    }

    override fun onSubmitTask() = Unit
    override fun onAddSubtask() = Unit
    override fun onEditSubtask(text: String) = Unit
    override fun onAddSuggestion(suggestion: String) = Unit
    override fun onRemoveSubtask() = Unit
}

@Preview
@Composable
private fun Preview() {
    TaskbenchTheme {
        TaskEditDialog(MockTaskEditDialogStateHolder)
    }
}
