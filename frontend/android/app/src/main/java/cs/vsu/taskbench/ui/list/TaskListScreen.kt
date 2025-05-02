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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.task.CategoryFilterState
import cs.vsu.taskbench.data.task.TaskRepository.SortByMode
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.DropdownOptions
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.TaskCard
import cs.vsu.taskbench.ui.component.TextField
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "TaskListScreen"

private val SORT_OPTIONS = listOf(
    R.string.label_sort_by_deadline,
    R.string.label_sort_by_priority,
)

@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun TaskListScreen(
    navController: NavController,
) {
    val viewModel = koinViewModel<TaskListScreenViewModel>()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = { NavigationBar(navController) }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(padding),
        ) {
            Spacer(Modifier.height(4.dp))

            SortModeRow(
                viewModel = viewModel,
                modifier = Modifier
                    .zIndex(2.0f)
                    .padding(horizontal = 16.dp),
            )

            val listState = rememberLazyListState()
            DateRow(
                selectedDate = viewModel.selectedDate,
                onDateSelected = {
                    viewModel.selectedDate = if (viewModel.selectedDate == it) null else it
                    listState.requestScrollToItem(0)
                },
                modifier = Modifier
                    .zIndex(2.0f)
                    .padding(horizontal = 16.dp),
            )

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clipToBounds(),
            ) {
                items(tasks, key = { it.id!! }) { task ->
                    val deadline = formatDeadline(task.deadline)
                    TaskCard(
                        deadlineText = deadline,
                        bodyText = task.content,
                        subtasks = task.subtasks,
                        onClick = {},
                        onDismiss = { viewModel.deleteTask(task) },
                        swipeEnabled = !listState.isScrollInProgress,

                        modifier = Modifier
                            .animateItem()
                            .fillParentMaxWidth()
                            .padding(horizontal = 16.dp),

                        onSubtaskCheckedChange = { subtask, checked ->
                            viewModel.setSubtaskChecked(
                                task,
                                subtask,
                                checked,
                            )
                        },
                    )
                }
                item { Spacer(Modifier) }
            }
        }
    }
}

@Composable
private fun SortModeRow(
    viewModel: TaskListScreenViewModel,
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
            onClick = { categoriesExpanded = true },
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
                        viewModel.sortByMode = SortByMode.Priority
                        sortModesExpanded = false
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
                        viewModel.sortByMode = SortByMode.Deadline
                        sortModesExpanded = false
                    },
                )
            }
        }
    }

    if (sheetState.isVisible || categoriesExpanded) {
        CategoryDialog(
            sheetState = sheetState,
            onVisibleChange = { categoriesExpanded = it },
            categories = categories,
            query = viewModel.categorySearchQuery,
            onQueryChange = { viewModel.categorySearchQuery = it },

            onDisableFilter = {
                viewModel.categoryFilterState = CategoryFilterState.Disabled
                categoriesExpanded = false
            },

            onCategorySelect = {
                viewModel.categoryFilterState = CategoryFilterState.Enabled(it)
                categoriesExpanded = false
            },
        )
    }
}

@Composable
private fun DateRow(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val todayIndex = Int.MAX_VALUE / 2
    val listState = rememberLazyListState(todayIndex)
    val today = LocalDate.now()

    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMM") }
    val dayFormatter = remember { DateTimeFormatter.ofPattern("d") }
    val weekdayFormatter = remember { DateTimeFormatter.ofPattern("EE") }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(Int.MAX_VALUE) {
            val offset = it - todayIndex
            val date = today.plusDays(offset.toLong())
            DateTile(
                topLabel = monthFormatter.format(date),
                middleLabel = dayFormatter.format(date),
                bottomLabel = weekdayFormatter.format(date),
                selected = date == selectedDate,
                onClick = { onDateSelected(date) },
            )
        }
    }
}

private val DATE_TILE_SHAPE = RoundedCornerShape(10.dp)

@Composable
private fun CategoryDialog(
    onVisibleChange: (Boolean) -> Unit,
    categories: List<Category>,
    query: String,
    onQueryChange: (String) -> Unit,
    onDisableFilter: () -> Unit,
    onCategorySelect: (Category?) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
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
            }

            LazyColumn {
                item {
                    Text(
                        text = stringResource(R.string.label_no_category),
                        fontSize = 16.sp,
                        color = LightGray,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier
                            .clickable { onCategorySelect(null) }
                            .padding(start = 16.dp)
                            .defaultMinSize(minHeight = 48.dp)
                            .wrapContentHeight()
                            .fillMaxWidth(),
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                }

                item {
                    Text(
                        text = stringResource(R.string.label_filter_category_all),
                        fontSize = 16.sp,
                        color = Black,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier
                            .clickable(onClick = onDisableFilter)
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

@Composable
private fun DateTile(
    topLabel: String,
    middleLabel: String,
    bottomLabel: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(DATE_TILE_SHAPE)
            .clickable(onClick = onClick)
            .background(
                color = if (selected) AccentYellow else White,
                shape = DATE_TILE_SHAPE,
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .width(40.dp),
    ) {
        Text(
            text = topLabel,
            fontSize = 16.sp,
        )
        Text(
            text = middleLabel,
            fontSize = 24.sp,
        )
        Text(
            text = bottomLabel,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun formatDeadline(deadline: LocalDateTime?): String {
    val pattern = stringResource(R.string.pattern_deadline)
    val formatter = remember { DateTimeFormatter.ofPattern(pattern) }
    return deadline?.let { formatter.format(deadline) } ?: ""
}
