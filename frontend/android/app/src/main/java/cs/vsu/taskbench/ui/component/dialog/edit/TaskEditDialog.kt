@file:OptIn(ExperimentalMaterial3Api::class)

package cs.vsu.taskbench.ui.component.dialog.edit

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.ui.Locales
import cs.vsu.taskbench.ui.component.AddedSubtask
import cs.vsu.taskbench.ui.component.BoxEdit
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.Chip
import cs.vsu.taskbench.ui.component.SubtaskCreationField
import cs.vsu.taskbench.ui.component.SuggestedSubtask
import cs.vsu.taskbench.ui.component.dialog.BottomSheetCategoryDialog
import cs.vsu.taskbench.ui.component.dialog.CategoryDialogActions
import cs.vsu.taskbench.ui.component.dialog.CategoryDialogMode
import cs.vsu.taskbench.ui.component.dialog.DatePickerDialog
import cs.vsu.taskbench.ui.component.dialog.TimePickerDialog
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.ExtraLightGray
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.LightYellow
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import cs.vsu.taskbench.ui.util.formatDeadline
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TaskEditDialog(
    stateHolder: TaskEditDialogStateHolder,
    modifier: Modifier = Modifier,
    sectionLabelColor: Color = DarkGray,
    submitButtonIcon: Painter = painterResource(R.drawable.ic_add_circle_filled),
    inactiveSubmitButtonIcon: Painter = painterResource(R.drawable.ic_add_circle_outline),
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

                    override fun onAdd() {
                        stateHolder.onAddCategory()
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
            onAdd = stateHolder::onAddSubtask,
            canAdd = stateHolder::canSaveSubtask,
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
                item {
                    SubtaskSection(
                        stringResource(R.string.label_subtask_list),
                        color = sectionLabelColor,
                    )
                }
                items(subtasks, key = { it.id ?: it.content }) { subtask ->
                    AddedSubtask(
                        text = subtask.content,
                        onRemove = { stateHolder.onRemoveSubtask(subtask) },
                        onEditConfirm = { stateHolder.onEditSubtask(subtask, it) },
                        canEdit = stateHolder::canSaveSubtask,
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            item {
                AnimatedVisibility(suggestions == null || suggestions.isNotEmpty()) {
                    SubtaskSection(
                        stringResource(R.string.label_suggestion_list),
                        color = sectionLabelColor,
                    )
                }
            }

            if (suggestions != null) {
                items(suggestions, key = { it }) { suggestion ->
                    SuggestedSubtask(
                        text = suggestion,
                        onAdd = { stateHolder.onAddSuggestion(suggestion) },
                        modifier = Modifier.animateItem(),
                    )
                }
            } else {
                item {
                    CircularProgressIndicator(
                        color = AccentYellow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize()
                            .size(32.dp),
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
            buttonIcon = submitButtonIcon,
            inactiveButtonIcon = inactiveSubmitButtonIcon,
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
    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    var newDeadline by remember { mutableStateOf(stateHolder.deadline) }
    LaunchedEffect(stateHolder.deadline) { newDeadline = stateHolder.deadline }

    var cleared by remember { mutableStateOf(false) }

    if (showDateDialog) {
        DatePickerDialog(
            onComplete = {
                val date = LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                newDeadline = LocalDateTime.of(
                    date,
                    newDeadline?.toLocalTime() ?: LocalTime.now().plusHours(1),
                )
                showDateDialog = false
            },
            onDismiss = { showDateDialog = false },
        )
    }

    if (showTimeDialog) {
        TimePickerDialog(
            initialTime = newDeadline?.toLocalTime() ?: LocalTime.now(),
            onComplete = { hour, minute ->
                val time = LocalTime.of(hour, minute)
                newDeadline = LocalDateTime.of(
                    newDeadline?.toLocalDate() ?: LocalDate.now(),
                    time,
                )
                showTimeDialog = false
            },
            onDismiss = { showTimeDialog = false },
        )
    }

    val datePattern = stringResource(R.string.pattern_date)
    val timePattern = stringResource(R.string.pattern_time)
    val dateFormatter = remember { DateTimeFormatter.ofPattern(datePattern, Locales.RU) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern(timePattern, Locales.RU) }

    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = White,
        onDismissRequest = { stateHolder.showDeadlineDialog = false },
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val hasDeadline = newDeadline != null
                val dateText = if (hasDeadline) {
                    dateFormatter.format(newDeadline)
                } else stringResource(R.string.label_not_set)

                DeadlineDialogButton(
                    iconId = R.drawable.ic_calendar,
                    text = dateText,
                    hasDeadline = hasDeadline,
                    onClick = { showDateDialog = true },
                    modifier = Modifier.weight(1.0f),
                )

                val timeText = if (hasDeadline) {
                    timeFormatter.format(newDeadline)
                } else stringResource(R.string.time_not_set)
                DeadlineDialogButton(
                    iconId = R.drawable.ic_clock,
                    text = timeText,
                    hasDeadline = hasDeadline,
                    onClick = { showTimeDialog = true },
                )
            }

            Button(
                text = stringResource(R.string.button_confirm),
                color = AccentYellow,
                onClick = {
                    stateHolder.isDeadlineSetManually = !cleared
                    stateHolder.deadline = newDeadline
                    stateHolder.showDeadlineDialog = false
                },
            )
            Button(
                text = stringResource(R.string.button_clear),
                color = ExtraLightGray,
                onClick = {
                    newDeadline = null
                    cleared = true
                },
            )
        }
    }
}

@Composable
private fun DeadlineDialogButton(
    @DrawableRes iconId: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasDeadline: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(LightYellow)
            .clickable(onClick = onClick)
            .width(IntrinsicSize.Min)
            .defaultMinSize(minHeight = 52.dp)
            .padding(16.dp),
    ) {
        val color = if (hasDeadline) Black else DarkGray
        Icon(
            painter = painterResource(iconId),
            contentDescription = null,
            modifier = Modifier.requiredSize(24.dp),
            tint = color,
        )
        Text(
            text = text,
            color = color,
            fontStyle = if (hasDeadline) FontStyle.Normal else FontStyle.Italic,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1.0f),
        )
    }
}

@Composable
@NonRestartableComposable
private fun SubtaskSection(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
) {
    Text(
        text = text,
        fontWeight = FontWeight.Medium,
        color = color,
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
            text = formatDeadline(stateHolder.deadline),
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

@Preview
@Composable
private fun Preview() {
    TaskbenchTheme {
        TaskEditDialog(MockTaskEditDialogStateHolder)
    }
}
