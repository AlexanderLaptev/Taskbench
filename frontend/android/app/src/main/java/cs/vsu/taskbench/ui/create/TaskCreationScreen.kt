package cs.vsu.taskbench.ui.create

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun TaskCreationScreen(
        navController: NavController,
) {
    val viewModel = koinViewModel<TaskCreationScreenViewModel>()
    val suggestions by viewModel.suggestions.collectAsState()
    val subtasks by viewModel.subtasks.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(navController)
        }
    ) { padding ->

        TaskCreationContent(
            task = viewModel.content,
            onTaskChange = {
                viewModel.content = it
                viewModel.updateSuggestions(it)},
            newSubtask = viewModel.newSubtask,
            onNewSubtaskChange = { viewModel.newSubtask = it },
            onCreateSubtaskClick = {
                viewModel.addSubtask()
                viewModel.newSubtask = ""
                             },
            priority = viewModel.priority,
            deadline = viewModel.deadline,
            category = viewModel.category,
            subtasks = subtasks,
            suggestions = suggestions,
            onDeadline = { },
            onPriority = { },
            onCategory = {},
            onAddTask = { viewModel.saveSubtask() },
            onRemoveSubtask = { subtask -> viewModel.removeSubtask(subtask)},
            onAddSubtask = { subtask -> viewModel.addSuggestion(subtask) },
            )
    }
}

@Composable
private fun TaskCreationContent(
    task: String,
    onTaskChange: (String) -> Unit,
    newSubtask: String,
    onNewSubtaskChange: (String) -> Unit,
    onCreateSubtaskClick: () -> Unit,
    priority: String,
    deadline: String,
    category: String,
    subtasks: List<Subtask>,
    suggestions: List<Subtask>,
    onDeadline: () -> Unit,
    onPriority: () -> Unit,
    onCategory: () -> Unit,
    onAddTask: () -> Unit,
    onRemoveSubtask: (Subtask) -> Unit,
    onAddSubtask: (Subtask) -> Unit,
){
    val visibilitySubtask = if (subtasks.isEmpty()) false else true
    val visibilitySuggestion = if (suggestions.isEmpty()) false else true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Beige)
    ){
        if(!(visibilitySubtask || visibilitySuggestion)) {
            Icon(
                painter = painterResource(R.drawable.logo_full_dark),
                contentDescription = "",
                tint = Color.Unspecified,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 48.dp,
                    bottom = 96.dp
                ),
        ) {
            CreateSubtaskField(
                text = newSubtask,
                onTextChange = onNewSubtaskChange,
                placeholder = stringResource(R.string.label_subtask),
                onAddButtonClick = onCreateSubtaskClick,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .height(389.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp, bottom = 48.dp)
            ){
                if(visibilitySubtask) {
                    Text(
                        text = stringResource(R.string.list_subtasks),
                        fontSize = 14.sp,
                        color = DarkGray
                    )
                    for (subtask in subtasks) {
                        AddedSubtask(
                            text = subtask.content,
                            onTextChange = { subtask.content },
                            onRemove = {onRemoveSubtask(subtask)},
                        )
                    }
                }
                if(visibilitySuggestion) {
                    Text(
                        text = stringResource(R.string.list_suggestions),
                        fontSize = 14.sp,
                        color = DarkGray,
                    )
                    for (suggestion in suggestions) {
                        Suggestion(
                            text = suggestion.content,
                            onAdd = {onAddSubtask(suggestion)},
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    Chip(
                        icon = painterResource(R.drawable.ic_clock),
                        text = "",
                        color = White,
                        textColor = Black,
                        onClick = {},
                        placeholder = stringResource(R.string.label_deadline)
                    )
                    Chip(
                        text = "",
                        color = White,
                        textColor = Black,
                        onClick = {},
                        placeholder = stringResource(R.string.label_priority)
                    )
                    Chip(
                        text = "",
                        color = White,
                        textColor = Black,
                        onClick = {},
                        placeholder = stringResource(R.string.label_category)
                    )
                }
                BoxEdit(
                    value = task,
                    onValueChange = onTaskChange,
                    buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
                    inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
                    placeholder = stringResource(R.string.label_task),
                    onClick = onAddTask
                )
            }

        }
    }
}


@Preview
@Composable
private fun Preview() {
    var task by remember { mutableStateOf("")}
    var newSubtask by remember { mutableStateOf("")}
    var priority by remember { mutableStateOf("")}
    var deadline by remember { mutableStateOf("")}
    var category by remember { mutableStateOf("")}
    var subtasks: List<Subtask> by remember { mutableStateOf(emptyList())}
    var suggestions: List<Subtask> by remember { mutableStateOf(emptyList())}

    TaskbenchTheme {
        TaskCreationContent(
            task = task,
            onTaskChange = { task = it },
            newSubtask = newSubtask,
            onNewSubtaskChange = { newSubtask = it },
            priority = priority,
            deadline = deadline,
            category = category,
            subtasks = subtasks,
            suggestions = suggestions,
            onDeadline = { },
            onPriority = { },
            onCategory = {},
            onCreateSubtaskClick = {},
            onAddTask = {},
            onRemoveSubtask = {},
            onAddSubtask = {},
        )
    }
}
