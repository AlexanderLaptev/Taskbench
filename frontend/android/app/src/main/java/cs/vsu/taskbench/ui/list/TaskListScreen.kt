package cs.vsu.taskbench.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.DropdownOptions
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.TaskCard
import cs.vsu.taskbench.ui.theme.White
import org.koin.androidx.compose.koinViewModel
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
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { // TODO: replace Unit with selected date
        viewModel.selectedDate = null
    }

    Scaffold(
        bottomBar = { NavigationBar(navController) }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(padding),
        ) {
            SortModeRow(Modifier.padding(horizontal = 16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding),
            ) {
                items(tasks, key = { it.id!! }) { task ->
                    val deadline = formatDeadline(task.deadline)

                    TaskCard(
                        deadlineText = deadline,
                        bodyText = task.content,
                        subtasks = task.subtasks,
                        onClick = {},
                        onDismiss = { viewModel.deleteTask(task) },

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
            }
        }
    }
}

@Composable
private fun SortModeRow(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        SortButton(
            title = "categories",
            options = listOf("abc", "def", "xyz"),
            onOptionClick = {},
        )

        SortButton(
            title = "sort mode",
            options = listOf("abc", "def", "xyz"),
            onOptionClick = {},
        )
    }
}

@Composable
private fun RowScope.SortButton(
    title: String,
    options: List<String>,
    onOptionClick: (Int) -> Unit,
) {
    Column(Modifier.weight(1.0f)) {
        var expanded by remember { mutableStateOf(false) }
        DropdownOptions(
            title = title,
            onClick = { expanded = true },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = White,
        ) {
            for ((index, option) in options.withIndex()) {
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionClick(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateRow(modifier: Modifier = Modifier) {
}

@Composable
private fun formatDeadline(deadline: LocalDateTime?): String {
    val pattern = stringResource(R.string.pattern_deadline)
    val formatter = remember { DateTimeFormatter.ofPattern(pattern) }

    return deadline?.let { formatter.format(deadline) } ?: ""
}
