package cs.vsu.taskbench.ui.create

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.AddedSubtask
import cs.vsu.taskbench.ui.component.BoxEdit
import cs.vsu.taskbench.ui.component.Chip
import cs.vsu.taskbench.ui.component.CreateSubtaskField
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.Suggestion
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun TaskCreationScreen(navController: NavController) {
    val viewModel = koinViewModel<TaskCreationScreenViewModel>()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val subtasks by viewModel.subtasks.collectAsStateWithLifecycle()

    val deadline by viewModel.deadline.collectAsStateWithLifecycle()
    val highPriority by viewModel.highPriority.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(navController)
        }
    ) { padding ->
        val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        TaskCreationContent(
            contentInput = viewModel.contentInput,
            subtaskInput = viewModel.subtaskInput,
            onSubtaskInputChange = { viewModel.subtaskInput = it },
            deadline = deadline,
            highPriority = highPriority,
            category = category?.name ?: "",
            subtasks = subtasks,
            suggestions = suggestions,
            onDeadlineClick = {},
            onPriorityClick = {},
            onCategoryClick = {},
            onSaveTask = { viewModel.saveTask() },
            onRemoveSubtask = { subtask -> viewModel.removeSubtask(subtask) },
            onAddSubtask = { subtask -> viewModel.addSuggestion(subtask) },
            modifier = Modifier.padding(
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                bottom = if (imePadding > 0.dp) imePadding else padding.calculateBottomPadding()
            ),

            onContentInputChange = {
                viewModel.contentInput = it
                viewModel.updateSuggestions(it)
            },

            onCreateSubtaskClick = {
                viewModel.addSubtask()
                viewModel.subtaskInput = ""
            },
        )
    }
}

@Composable
private fun TaskCreationContent(
    contentInput: String,
    onContentInputChange: (String) -> Unit,
    subtaskInput: String,
    onSubtaskInputChange: (String) -> Unit,
    onCreateSubtaskClick: () -> Unit,
    highPriority: Boolean,
    deadline: LocalDateTime?,
    category: String,
    subtasks: List<Subtask>,
    suggestions: List<Subtask>,
    onDeadlineClick: () -> Unit,
    onPriorityClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onSaveTask: () -> Unit,
    onRemoveSubtask: (Subtask) -> Unit,
    onAddSubtask: (Subtask) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Beige)
    ) {
        if (subtasks.isEmpty() && suggestions.isEmpty()) {
            Icon(
                painter = painterResource(R.drawable.logo_full_dark),
                contentDescription = "",
                tint = Color.Unspecified,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            CreateSubtaskField(
                text = subtaskInput,
                onTextChange = onSubtaskInputChange,
                placeholder = stringResource(R.string.label_subtask),
                onAddButtonClick = onCreateSubtaskClick,
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1.0f)
                    .padding(top = 8.dp, bottom = 48.dp)
            ) {
                // TODO: make keys unique
                if (subtasks.isNotEmpty()) {
                    item(key = 0) {
                        Text(
                            text = stringResource(R.string.list_subtasks),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGray,
                        )
                    }
                    items(subtasks, key = { it.content }) { subtask ->
                        AddedSubtask(
                            text = subtask.content,
                            onTextChange = { subtask.content },
                            onRemove = { onRemoveSubtask(subtask) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                if (suggestions.isNotEmpty()) {
                    item(key = 1) {
                        Text(
                            text = stringResource(R.string.list_suggestions),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DarkGray,
                        )
                    }
                    items(suggestions, key = { it.content }) { suggestion ->
                        Suggestion(
                            text = suggestion.content,
                            onAdd = { onAddSubtask(suggestion) },
                            modifier = Modifier.animateItem(),
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
                    Chip(
                        text = if (deadline != null) {
                            ""
                        } else stringResource(R.string.label_deadline), // TODO: format date

                        icon = painterResource(R.drawable.ic_clock),
                        textColor = if (deadline != null) Black else LightGray,
                        color = White,
                        onClick = onDeadlineClick,
                    )
                    Chip(
                        text = stringResource(
                            if (highPriority) {
                                R.string.priority_high
                            } else R.string.priority_low
                        ),

                        color = if (highPriority) AccentYellow else White,
                        textColor = Black,
                        onClick = onPriorityClick,
                    )
                    Chip(
                        text = category.ifEmpty { stringResource(R.string.label_category) },
                        color = White,
                        textColor = if (category.isEmpty()) LightGray else Black,
                        onClick = onCategoryClick,
                    )
                }

                BoxEdit(
                    value = contentInput,
                    onValueChange = onContentInputChange,
                    buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
                    inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
                    placeholder = stringResource(R.string.label_task),
                    onClick = onSaveTask,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    var task by remember { mutableStateOf("") }
    var newSubtask by remember { mutableStateOf("") }
    val highPriority by remember { mutableStateOf(false) }
    val deadline by remember { mutableStateOf(LocalDateTime.now()) }
    val category by remember { mutableStateOf("") }
    val subtasks: List<Subtask> by remember { mutableStateOf(emptyList()) }
    val suggestions: List<Subtask> by remember { mutableStateOf(emptyList()) }

    TaskbenchTheme {
        TaskCreationContent(
            contentInput = task,
            onContentInputChange = { task = it },
            subtaskInput = newSubtask,
            onSubtaskInputChange = { newSubtask = it },
            highPriority = highPriority,
            deadline = deadline,
            category = category,
            subtasks = subtasks,
            suggestions = suggestions,
            onDeadlineClick = {},
            onPriorityClick = {},
            onCategoryClick = {},
            onCreateSubtaskClick = {},
            onSaveTask = {},
            onRemoveSubtask = {},
            onAddSubtask = {},
        )
    }
}
