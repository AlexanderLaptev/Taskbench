package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import java.time.LocalDate

interface TaskEditDialogStateHolder {
    var taskInput: String
    var subtaskInput: String
    val subtasks: List<Subtask>
    val suggestions: List<String>
    var deadline: LocalDate?
    var isHighPriority: Boolean
    var category: Category?

    fun onSubmitTask()
    fun onAddSubtask()
    fun onDeadlineChipClick()
    fun onPriorityChipClick()
    fun onCategoryChipClick()
}

@Composable
fun TaskEditDialog(
    stateHolder: TaskEditDialogStateHolder,
    drawLogo: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier,
    ) {
        SubtaskArea(stateHolder = stateHolder)

        if (drawLogo) {
            Image(
                painter = painterResource(R.drawable.logo_full_dark),
                contentDescription = null,
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .wrapContentSize(),
            )
        } else Spacer(Modifier.weight(1.0f))

        EditArea(
            stateHolder = stateHolder,
            modifier = Modifier.imePadding(),
        )
    }
}

@Composable
private fun SubtaskArea(
    stateHolder: TaskEditDialogStateHolder,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SubtaskCreationField(
            text = stateHolder.subtaskInput,
            onTextChange = { stateHolder.subtaskInput = it },
            placeholder = stringResource(R.string.placeholder_enter_subtask),
            onAddButtonClick = stateHolder::onAddSubtask,
        )
    }
}

@Composable
private fun EditArea(
    stateHolder: TaskEditDialogStateHolder,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        EditAreaChips(stateHolder = stateHolder)
        BoxEdit(
            value = stateHolder.taskInput,
            onValueChange = { stateHolder.taskInput = it },
            buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
            inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
            placeholder = stringResource(R.string.placeholder_enter_task),
            onClick = stateHolder::onSubmitTask,
        )
    }
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
        Subtask(null, "Lorem ipsum dolor sit amet", false),
        Subtask(null, "Lorem ipsum dolor sit amet", false),
        Subtask(null, "Lorem ipsum dolor sit amet", false),
    )

    override val suggestions: List<String> = listOf(
        "Lorem ipsum dolor sit amet",
        "Lorem ipsum dolor sit amet",
        "Lorem ipsum dolor sit amet",
    )

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

    override fun onSubmitTask() = Unit
    override fun onAddSubtask() = Unit
    override fun onDeadlineChipClick() = Unit
    override fun onPriorityChipClick() {
        _isHighPriority = !_isHighPriority
    }

    override fun onCategoryChipClick() = Unit
}

@Preview
@Composable
private fun PreviewNoLogo() {
    TaskbenchTheme {
        TaskEditDialog(MockTaskEditDialogStateHolder, false)
    }
}

@Preview
@Composable
private fun PreviewWithLogo() {
    TaskbenchTheme {
        TaskEditDialog(MockTaskEditDialogStateHolder, true)
    }
}
