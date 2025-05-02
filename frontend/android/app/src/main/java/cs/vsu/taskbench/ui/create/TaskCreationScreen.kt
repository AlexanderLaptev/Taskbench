package cs.vsu.taskbench.ui.create

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.AddedSubtask
import cs.vsu.taskbench.ui.component.BoxEdit
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.Chip
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.SubtaskCreationField
import cs.vsu.taskbench.ui.component.SuggestedSubtask
import cs.vsu.taskbench.ui.component.TextField
import cs.vsu.taskbench.ui.create.TaskCreationScreenViewModel.Error
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.ExtraLightGray
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.Red
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
@Destination<RootGraph>(style = ScreenTransitions::class)
fun TaskCreationScreen(navController: NavController) {
    TaskCreationScreenContent(
        navController = navController,
    )
}

@Composable
private fun TaskCreationScreenContent(
    navController: NavController = rememberNavController(),
) {
    Scaffold(
        bottomBar = { NavigationBar(navController) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .imePadding()
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(R.drawable.logo_full_dark),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .padding(
                    top = 8.dp,
                    bottom = scaffoldPadding.calculateBottomPadding(),
                )
                .consumeWindowInsets(scaffoldPadding)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                )
        ) {
            SubtaskArea(
                Modifier
                    .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding())
                    .weight(1.0f),
            )
            EditArea(Modifier.imePadding())
        }
    }
}

@Composable
fun SubtaskArea(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    Column(modifier = modifier) {
        SubtaskCreationField(
            text = text,
            onTextChange = { text = it },
            placeholder = stringResource(R.string.placeholder_enter_subtask),
            onAddButtonClick = {},
        )
    }
}

@Composable
private fun EditArea(modifier: Modifier = Modifier) {
    var value by remember { mutableStateOf("") }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        EditAreaChips(
            deadline = null,
            onDeadlineClick = {},
            highPriority = false,
            onPriorityClick = {},
            category = null,
            onCategoryClick = {},
        )

        BoxEdit(
            value = value,
            onValueChange = { value = it },
            buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
            inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
            placeholder = stringResource(R.string.placeholder_enter_task),
            onClick = {},
        )
    }
}

@Composable
private fun EditAreaChips(
    deadline: LocalDate?,
    onDeadlineClick: () -> Unit,
    highPriority: Boolean,
    onPriorityClick: () -> Unit,
    category: Category?,
    onCategoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.horizontalScroll(rememberScrollState()),
    ) {
        // Deadline chip
        val deadlineContentColor = if (deadline == null) LightGray else Black
        Chip(
            text = stringResource(R.string.chip_deadline),
            icon = painterResource(R.drawable.ic_clock),
            textColor = deadlineContentColor,
            iconTint = deadlineContentColor,
            color = White,
            onClick = onDeadlineClick,
        )

        // Priority chip
        val priorityColor = if (highPriority) AccentYellow else White
        val priorityResId = if (highPriority) {
            R.string.priority_high
        } else R.string.priority_normal
        Chip(
            text = stringResource(priorityResId),
            color = priorityColor,
            textColor = Black,
            onClick = onPriorityClick,
        )

        // Category chip
        val categoryTextColor = if (category == null) LightGray else Black
        val categoryText = category?.name ?: stringResource(R.string.label_no_category)
        Chip(
            text = categoryText,
            color = White,
            textColor = categoryTextColor,
            onClick = onCategoryClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ShowToast")
@Composable
private fun _TaskCreationScreen(navController: NavController) {
    val viewModel = koinViewModel<TaskCreationScreenViewModel>()
    val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.updateCategories()

        var toast: Toast? = null
        viewModel.errorFlow.collect { error ->
            val messageId = when (error) {
                Error.BlankCategory -> R.string.error_blank_category
                Error.Unknown -> R.string.error_unknown
                Error.CategoryTooLong -> R.string.error_category_too_long
                Error.CategoryAlreadyExists -> R.string.error_category_already_exists
                Error.BlankDeadline -> R.string.error_blank_deadline
            }

            toast?.cancel()
            toast = Toast.makeText(
                context, context.getString(messageId), Toast.LENGTH_SHORT
            ).apply {
                show()
            }
            delay(2200)
            toast?.cancel()
        }
    }

    val categorySheetState = rememberModalBottomSheetState()
    LaunchedEffect(viewModel.isCategorySelectionDialogVisible) {
        if (viewModel.isCategorySelectionDialogVisible) categorySheetState.show()
        else categorySheetState.hide()
    }

    val deadlineSheetState = rememberModalBottomSheetState()
    LaunchedEffect(viewModel.isDeadlineDialogVisible) {
        if (viewModel.isDeadlineDialogVisible) deadlineSheetState.show()
        else deadlineSheetState.hide()
    }

    Scaffold(
        bottomBar = { NavigationBar(navController) },
    ) { padding ->
        if (categorySheetState.isVisible || viewModel.isCategorySelectionDialogVisible) {
            CategoryDialog(
                sheetState = categorySheetState,
                onVisibleChange = { viewModel.isCategorySelectionDialogVisible = it },
                categories = viewModel.categorySearchResults,
                onCategoryAdd = { viewModel.addCategory(it) },
                query = viewModel.categorySearchQuery,
                onQueryChange = { viewModel.categorySearchQuery = it },

                onCategorySelect = {
                    viewModel.selectedCategory = it
                    viewModel.isCategorySelectionDialogVisible = false
                },
            )
        }
        if (viewModel.isDeadlineDialogVisible || deadlineSheetState.isVisible) {
            DeadlineDialog(
                onDeadlineSelect = { dateMillis, hour, minute ->
                    viewModel.saveDeadline(dateMillis, hour, minute)
                },
                sheetState = deadlineSheetState,
                onVisibleChange = { viewModel.isDeadlineDialogVisible = it },
            )
        }

        val bottomScaffoldPadding = padding.calculateBottomPadding()
        Box(
            Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                    bottom = if (imePadding > bottomScaffoldPadding) {
                        imePadding
                    } else bottomScaffoldPadding
                )
        ) {
            if (viewModel.subtasks.isEmpty() && viewModel.suggestedSubtasks.isEmpty()) {
                Image(
                    painter = painterResource(R.drawable.logo_full_dark),
                    contentDescription = "",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
            ) {
                SubtaskCreationField(
                    text = viewModel.subtaskInput,
                    onTextChange = { viewModel.subtaskInput = it },
                    placeholder = stringResource(R.string.placeholder_enter_subtask),
                    onAddButtonClick = viewModel::addSubtask,
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(top = 8.dp, bottom = 48.dp)
                ) {
                    // TODO: add keys and animations with Modifier.animateItem()
                    if (viewModel.subtasks.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.list_subtasks),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGray,
                            )
                        }
                        items(viewModel.subtasks) { subtask ->
                            AddedSubtask(
                                text = subtask.content,
                                onTextChange = { /* TODO */ },
                                onRemove = { viewModel.removeSubtask(subtask) },
                            )
                        }
                    }

                    if (viewModel.suggestedSubtasks.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.list_suggestions),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DarkGray,
                            )
                        }
                        items(viewModel.suggestedSubtasks) { suggestion ->
                            SuggestedSubtask(
                                text = suggestion.content,
                                onAdd = { viewModel.addSuggestion(suggestion) },
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        val pattern = stringResource(R.string.pattern_deadline)
                        val formatter = remember { DateTimeFormatter.ofPattern(pattern) }
                        Chip(
                            text = if (viewModel.deadline != null) {
                                formatter.format(viewModel.deadline)
                            } else stringResource(R.string.chip_deadline),

                            icon = painterResource(R.drawable.ic_clock),
                            textColor = if (viewModel.deadline != null) Black else LightGray,
                            color = White,
                            onClick = { viewModel.isDeadlineDialogVisible = true },
                        )
                        Chip(
                            text = stringResource(
                                if (viewModel.isHighPriority) {
                                    R.string.priority_high
                                } else R.string.priority_normal
                            ),

                            color = if (viewModel.isHighPriority) AccentYellow else White,
                            textColor = Black,
                            onClick = { viewModel.isHighPriority = !viewModel.isHighPriority },
                        )
                        Chip(
                            text = viewModel.selectedCategory?.name
                                ?: stringResource(R.string.label_no_category),
                            color = White,
                            textColor = if (viewModel.selectedCategory == null) LightGray else Black,
                            onClick = { viewModel.isCategorySelectionDialogVisible = true },
                        )
                    }

                    BoxEdit(
                        value = viewModel.contentInput,
                        onValueChange = { viewModel.contentInput = it },
                        buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
                        inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
                        placeholder = stringResource(R.string.placeholder_enter_task),
                        onClick = viewModel::saveTask,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDialog(
    sheetState: SheetState,
    onVisibleChange: (Boolean) -> Unit,
    categories: List<Category>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (Category?) -> Unit,
    onCategoryAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onVisibleChange(false) },
        containerColor = White,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .defaultMinSize(minHeight = 80.dp)
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
            ) {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = stringResource(R.string.label_category),
                    modifier = Modifier.weight(1.0f),
                )
                Button(
                    onClick = { onCategoryAdd(query) },
                    color = AccentYellow,
                    fillWidth = false,
                    modifier = Modifier.size(52.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_circle_outline),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.requiredSize(24.dp),
                    )
                }
            }

            LazyColumn {
                item {
                    Text(
                        text = stringResource(R.string.label_no_category),
                        fontSize = 16.sp,
                        color = LightGray,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier
                            .animateItem()
                            .clickable { onCategorySelect(null) }
                            .padding(start = 16.dp)
                            .defaultMinSize(minHeight = 48.dp)
                            .wrapContentHeight()
                            .fillMaxWidth(),
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                }

                items(categories, key = { it.id!! }) { category ->
                    Text(
                        text = category.name,
                        fontSize = 16.sp,
                        color = Black,
                        modifier = Modifier
                            .animateItem()
                            .clickable { onCategorySelect(category) }
                            .padding(start = 16.dp)
                            .defaultMinSize(minHeight = 48.dp)
                            .wrapContentHeight()
                            .fillMaxWidth(),
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlineDialog(
    sheetState: SheetState,
    onDeadlineSelect: (Long?, Int, Int) -> Unit,
    onVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val datePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )
    var isInputMode by remember { mutableStateOf(true) }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onVisibleChange(false) },
        containerColor = White,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .defaultMinSize(minHeight = 80.dp)
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearEasing
                        )
                    )
            ) {
                if (isInputMode) {
                    TimeInput(
                        modifier = modifier
                            .align(Alignment.Center),
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            timeSelectorSelectedContainerColor = AccentYellow,
                            timeSelectorUnselectedContainerColor = Beige,
                            timeSelectorSelectedContentColor = Black,
                            timeSelectorUnselectedContentColor = Black,
                        )
                    )
                } else {
                    TimePicker(
                        modifier = modifier
                            .align(Alignment.Center),
                        state = timePickerState, colors = TimePickerDefaults.colors(
                            clockDialColor = Beige,
                            clockDialSelectedContentColor = Black,
                            selectorColor = AccentYellow,
                            periodSelectorBorderColor = AccentYellow,
                            clockDialUnselectedContentColor = Black,
                            timeSelectorSelectedContainerColor = AccentYellow,
                            timeSelectorUnselectedContainerColor = Beige,
                            timeSelectorSelectedContentColor = Black,
                            timeSelectorUnselectedContentColor = Black,
                        )
                    )
                }

                Button(
                    onClick = {
                        isInputMode = !isInputMode
                        datePickerState.displayMode =
                            if (!isInputMode) DisplayMode.Input else datePickerState.displayMode
                    },
                    fillWidth = false,
                    modifier = Modifier
                        .size(52.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 24.dp),
                ) {
                    Icon(
                        painter = painterResource(
                            if (isInputMode) R.drawable.ic_clock else R.drawable.ic_edit
                        ),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.requiredSize(24.dp),
                    )
                }
            }

            Box(
            ) {
                DatePicker(
                    state = datePickerState,
                    title = null,
                    showModeToggle = false,
                    modifier = modifier
                        .padding(top = 8.dp)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessVeryLow
                            )
                        ),
                    colors = DatePickerDefaults.colors(
                        containerColor = White,
                        headlineContentColor = DarkGray,
                        weekdayContentColor = Black,
                        subheadContentColor = AccentYellow,
                        navigationContentColor = DarkGray,
                        yearContentColor = Black,
                        disabledYearContentColor = LightGray,
                        currentYearContentColor = Black,
                        selectedYearContentColor = Black,
                        selectedYearContainerColor = AccentYellow,
                        dayContentColor = Black,
                        selectedDayContentColor = Black,
                        selectedDayContainerColor = AccentYellow,
                        todayContentColor = Black,
                        todayDateBorderColor = LightGray,
                        dividerColor = ExtraLightGray,
                        dateTextFieldColors = TextFieldColors(
                            focusedTextColor = Black,
                            unfocusedTextColor = Black,
                            disabledTextColor = Black,
                            errorTextColor = Black,
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            disabledContainerColor = White,
                            errorContainerColor = White,
                            cursorColor = Black,
                            errorCursorColor = Black,
                            textSelectionColors = TextSelectionColors(
                                handleColor = LightGray,
                                backgroundColor = White
                            ),
                            focusedIndicatorColor = AccentYellow,
                            unfocusedIndicatorColor = Beige,
                            disabledIndicatorColor = Beige,
                            errorIndicatorColor = Red,
                            focusedLeadingIconColor = Color.Transparent,
                            unfocusedLeadingIconColor = Color.Transparent,
                            disabledLeadingIconColor = Color.Transparent,
                            errorLeadingIconColor = Color.Transparent,
                            focusedTrailingIconColor = Color.Transparent,
                            unfocusedTrailingIconColor = Color.Transparent,
                            disabledTrailingIconColor = Color.Transparent,
                            errorTrailingIconColor = Color.Transparent,
                            focusedLabelColor = Black,
                            unfocusedLabelColor = Black,
                            disabledLabelColor = Black,
                            errorLabelColor = Red,
                            focusedPlaceholderColor = LightGray,
                            unfocusedPlaceholderColor = Black,
                            disabledPlaceholderColor = Black,
                            errorPlaceholderColor = Red,
                            focusedSupportingTextColor = LightGray,
                            unfocusedSupportingTextColor = LightGray,
                            disabledSupportingTextColor = LightGray,
                            errorSupportingTextColor = Red,
                            focusedPrefixColor = White,
                            unfocusedPrefixColor = White,
                            disabledPrefixColor = White,
                            errorPrefixColor = Red,
                            focusedSuffixColor = White,
                            unfocusedSuffixColor = White,
                            disabledSuffixColor = White,
                            errorSuffixColor = Red,
                        ),
                    )
                )

                IconButton(
                    onClick = {
                        datePickerState.displayMode = when (datePickerState.displayMode) {
                            DisplayMode.Picker -> DisplayMode.Input
                            DisplayMode.Input -> DisplayMode.Picker
                            else -> DisplayMode.Input
                        }
                        isInputMode =
                            datePickerState.displayMode == DisplayMode.Picker || isInputMode
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(
                            if (datePickerState.displayMode == DisplayMode.Picker)
                                R.drawable.ic_edit else R.drawable.ic_calendar
                        ),
                        contentDescription = null
                    )
                }
            }

            Button(
                onClick = {
                    onDeadlineSelect(
                        datePickerState.selectedDateMillis,
                        timePickerState.hour,
                        timePickerState.minute
                    )
                },
                color = AccentYellow,
                fillWidth = false,
                modifier = Modifier.size(52.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ok_circle_outline),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.requiredSize(28.dp),
                )
            }
        }

    }
}
