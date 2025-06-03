@file:OptIn(ExperimentalMaterial3Api::class)

package cs.vsu.taskbench.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.analytics.AnalyticsFacade
import cs.vsu.taskbench.data.task.CategoryFilterState
import cs.vsu.taskbench.data.task.TaskRepository.SortByMode
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.ui.Locales
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.DropdownOptions
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.TaskCard
import cs.vsu.taskbench.ui.component.dialog.BottomSheetCategoryDialog
import cs.vsu.taskbench.ui.component.dialog.CategoryDialogActions
import cs.vsu.taskbench.ui.component.dialog.CategoryDialogMode
import cs.vsu.taskbench.ui.component.dialog.edit.TaskEditDialog
import cs.vsu.taskbench.ui.component.dialog.edit.TaskEditDialogViewModel
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightYellow
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "TaskListScreen"

private val SORT_OPTIONS = listOf(
    R.string.label_sort_by_deadline,
    R.string.label_sort_by_priority,
)

private const val TODAY_INDEX = Int.MAX_VALUE / 2

@OptIn(DelicateCoroutinesApi::class)
@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun TaskListScreen(
    navController: NavController,
) {
    val screenViewModel = koinViewModel<TaskListScreenViewModel>()
    val dialogViewModel = koinViewModel<TaskEditDialogViewModel>()

    val snackState = remember { SnackbarHostState() }
    val tasks by screenViewModel.tasks.collectAsStateWithLifecycle()
    val resources = LocalContext.current.resources

    var showEditDialog by remember { mutableStateOf(false) }
    LaunchedEffect(showEditDialog) {
        if (!showEditDialog) screenViewModel.refresh(reload = false)
    }

    LaunchedEffect(Unit) {
        AnalyticsFacade.logScreen("TaskList")

        launch {
            screenViewModel.errorFlow.collect {
                val message = when (it) {
                    TaskListScreenViewModel.Error.CouldNotConnect -> R.string.error_could_not_connect
                    TaskListScreenViewModel.Error.Unknown -> R.string.error_unknown
                }
                snackState.currentSnackbarData?.dismiss()
                snackState.showSnackbar(
                    message = resources.getString(message),
                    withDismissAction = true,
                )
            }
        }

        launch {
            dialogViewModel.errorFlow.collect {
                val message = when (it) {
                    TaskEditDialogViewModel.Error.CouldNotConnect -> R.string.error_could_not_connect
                    TaskEditDialogViewModel.Error.Unknown -> R.string.error_unknown
                }
                showEditDialog = false
                snackState.currentSnackbarData?.dismiss()
                snackState.showSnackbar(
                    message = resources.getString(message),
                    withDismissAction = true,
                )
            }
        }

        launch {
            dialogViewModel.submitEventFlow.collect {
                showEditDialog = false
            }
        }
    }

    LaunchedEffect(screenViewModel.deletedTask) {
        if (screenViewModel.deletedTask == null) return@LaunchedEffect
        val deleted = screenViewModel.deletedTask!!

        snackState.currentSnackbarData?.dismiss()
        val result = snackState.showSnackbar(
            message = resources.getString(R.string.label_task_deleted),
            actionLabel = resources.getString(R.string.button_undo),
            duration = SnackbarDuration.Long,
            withDismissAction = true,
        )

        when (result) {
            SnackbarResult.Dismissed -> {
                screenViewModel.confirmTaskDeletion()
            }

            SnackbarResult.ActionPerformed -> {
                screenViewModel.undoTaskDeletion()
            }
        }
    }

    val taskListState = rememberLazyListState()
    val dateRowListState = rememberLazyListState(TODAY_INDEX)
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            // The coroutine must not be cancelled when navigating between screens.
            GlobalScope.launch { screenViewModel.confirmTaskDeletion() }
            // TODO: remove when we start keeping VMs between screens
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackState,
                snackbar = {
                    Snackbar(
                        snackbarData = it,
                        actionColor = AccentYellow,
                    )
                }
            )
        },

        bottomBar = {
            NavigationBar(
                navController = navController,
                onReset = {
                    scope.launch { taskListState.animateScrollToItem(0) }
                    scope.launch { dateRowListState.animateScrollToItem(TODAY_INDEX) }
                    AnalyticsFacade.logEvent("tasklist_reset_scroll")
                    true // prevent default action (navigation)
                }
            )
        }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(padding),
        ) {
            Spacer(Modifier.height(4.dp))
            SortModeRow(
                viewModel = screenViewModel,
                listState = taskListState,
                modifier = Modifier
                    .zIndex(2.0f)
                    .padding(horizontal = 16.dp),
            )

            DateRow(
                selectedDate = screenViewModel.selectedDate,
                onDateSelected = {
                    screenViewModel.selectedDate =
                        if (screenViewModel.selectedDate == it) null else it
                    scope.launch { taskListState.animateScrollToItem(0) }
                },
                listState = dateRowListState,
                modifier = Modifier
                    .zIndex(2.0f)
                    .padding(horizontal = 16.dp),
            )

            LazyColumn(
                state = taskListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clipToBounds(),
            ) {
                @Suppress("NAME_SHADOWING") val tasks = tasks
                if (tasks != null) {
                    items(tasks, key = { it.id!! }) { task ->
                        TaskCard(
                            deadline = task.deadline,
                            bodyText = task.content,
                            subtasks = task.subtasks,
                            onDismiss = {
                                AnalyticsFacade.logEvent("task_deleted")
                                screenViewModel.deleteTask(task)
                            },
                            swipeEnabled = !taskListState.isScrollInProgress,

                            onClick = {
                                AnalyticsFacade.logEvent("task_edit")
                                dialogViewModel.editTask = task
                                showEditDialog = true
                            },

                            onSubtaskCheckedChange = { subtask, checked ->
                                AnalyticsFacade.logEvent("subtask_done_toggled")
                                screenViewModel.setSubtaskChecked(subtask, checked)
                            },

                            modifier = Modifier
                                .animateItem()
                                .fillParentMaxWidth()
                                .padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    item {
                        CircularProgressIndicator(
                            color = AccentYellow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentSize()
                                .size(48.dp),
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        Dialog(
            onDismissRequest = { showEditDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
        ) {
            TaskEditDialog(
                stateHolder = dialogViewModel,
                sectionLabelColor = White,
                submitButtonIcon = painterResource(R.drawable.ic_ok_circle_filled),
                inactiveSubmitButtonIcon = painterResource(R.drawable.ic_ok_circle_outline),
                modifier = Modifier
                    .systemBarsPadding()
                    .padding(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                    )
            )
        }
    }
}

@Composable
private fun SortModeRow(
    viewModel: TaskListScreenViewModel,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var categoriesExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(categoriesExpanded) {
        scope.launch {
            if (categoriesExpanded) sheetState.show() else sheetState.hide()
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        DropdownOptions(
            title = when (val state = viewModel.categoryFilterState) {
                CategoryFilterState.Disabled -> stringResource(R.string.label_filter_category_all)
                is CategoryFilterState.Enabled -> {
                    state.category?.name ?: stringResource(R.string.label_no_category)
                }
            },

            titleColor = Black,
            onClick = {
                viewModel.categorySearchQuery = ""
                categoriesExpanded = true
            },
            modifier = Modifier.weight(1.0f),
        )

        Column(
            modifier = Modifier.weight(1.0f),
        ) {
            var sortModesExpanded by remember { mutableStateOf(false) }
            DropdownOptions(
                title = when (viewModel.sortByMode) {
                    SortByMode.Priority -> stringResource(R.string.label_sort_by_priority)
                    SortByMode.Deadline -> stringResource(R.string.label_sort_by_deadline)
                },
                onClick = { sortModesExpanded = true },
            )

            DropdownMenu(
                expanded = sortModesExpanded,
                onDismissRequest = { sortModesExpanded = false },
                containerColor = White,
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.label_sort_by_priority),
                            style = TextStyle(fontStyle = FontStyle.Normal),
                        )
                    },
                    onClick = {
                        sortModesExpanded = false
                        viewModel.sortByMode = SortByMode.Priority
                        AnalyticsFacade.logEvent("sort_mode_changed", mapOf("mode" to "priority"))
                        scope.launch { listState.animateScrollToItem(0) }
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.label_sort_by_deadline),
                            style = TextStyle(fontStyle = FontStyle.Normal),
                        )
                    },
                    onClick = {
                        sortModesExpanded = false
                        viewModel.sortByMode = SortByMode.Deadline
                        AnalyticsFacade.logEvent("sort_mode_changed", mapOf("mode" to "deadline"))
                        scope.launch { listState.animateScrollToItem(0) }
                    },
                )
            }
        }
    }

    if (sheetState.isVisible || categoriesExpanded) {
        BottomSheetCategoryDialog(
            CategoryDialogMode.Filter,
            categories = categories,
            input = viewModel.categorySearchQuery,
            sheetState = sheetState,
            actions = remember {
                object : CategoryDialogActions {
                    override fun onInputChange(input: String) {
                        viewModel.categorySearchQuery = input
                    }

                    override fun onSelect(category: Category) {
                        AnalyticsFacade.logEvent("category_filter_enabled")
                        viewModel.categoryFilterState = CategoryFilterState.Enabled(category)
                        postSelect()
                    }

                    override fun onDismiss() {
                        categoriesExpanded = false
                    }

                    override fun onDeselect() {
                        AnalyticsFacade.logEvent("category_filter_enabled")
                        viewModel.categoryFilterState = CategoryFilterState.Enabled(null)
                        postSelect()
                    }

                    override fun onSelectAll() {
                        AnalyticsFacade.logEvent("category_filter_disabled")
                        viewModel.categoryFilterState = CategoryFilterState.Disabled
                        postSelect()
                    }

                    private fun postSelect() {
                        categoriesExpanded = false
                        scope.launch { listState.animateScrollToItem(0) }
                    }
                }
            },
        )
    }
}

@Composable
private fun DateRow(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()

    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMM", Locales.RU) }
    val dayFormatter = remember { DateTimeFormatter.ofPattern("d", Locales.RU) }
    val weekdayFormatter = remember { DateTimeFormatter.ofPattern("EE", Locales.RU) }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(Int.MAX_VALUE) {
            val difference = it - TODAY_INDEX
            val date = today.plusDays(difference.toLong())
            DateTile(
                topLabel = monthFormatter.format(date),
                middleLabel = dayFormatter.format(date),
                bottomLabel = weekdayFormatter.format(date),
                selected = date == selectedDate,
                today = difference == 0,
                onClick = { onDateSelected(date) },
            )
        }
    }
}

private val DATE_TILE_SHAPE = RoundedCornerShape(10.dp)

@Composable
private fun DateTile(
    topLabel: String,
    middleLabel: String,
    bottomLabel: String,
    selected: Boolean,
    today: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(DATE_TILE_SHAPE)
            .clickable(onClick = onClick)
            .background(
                color = if (selected) AccentYellow else if (today) LightYellow else White,
                shape = DATE_TILE_SHAPE,
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .defaultMinSize(minWidth = 40.dp),
    ) {
        val fontStyle = if (today) FontStyle.Italic else FontStyle.Normal
        Text(
            text = topLabel,
            fontSize = 16.sp,
            fontStyle = fontStyle,
        )
        Text(
            text = middleLabel,
            fontSize = 24.sp,
            fontStyle = fontStyle,
        )
        Text(
            text = bottomLabel,
            fontSize = 16.sp,
            fontStyle = fontStyle,
        )
    }
}
