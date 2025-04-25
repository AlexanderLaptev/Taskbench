package cs.vsu.taskbench.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.BoxEdit
import cs.vsu.taskbench.ui.component.Chip
import cs.vsu.taskbench.ui.component.CreateSubtaskField
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

@Stable
private data class TaskCreationScreenState(
    val task: String,
    val onTaskChange: (String) -> Unit,
    val newSubtask: String,
    val onNewSubtaskChange: (String) -> Unit,
    val priority: String,
    val deadline: String,
    val category: String,
    var subtasks: List<Subtask>,
    var suggestions: List<Subtask>,
    val onDeadline: () -> Unit,
    val onPriority: () -> Unit,
    val onCategory: () -> Unit,
    val onSubtaskClick: () -> Unit,
    val onAddTask: () -> Unit,
)


@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun TaskCreationScreen(
    //    navController: NavController,
) {


//    Scaffold(
//        bottomBar = {
//            NavigationBar(navController)
//        }
//    ) { padding ->
//        // TODO!
//        val suggestionRepository = koinInject<SuggestionRepository>()
//        var suggestions by remember { mutableStateOf(listOf<String>()) }
//        val scope = rememberCoroutineScope()
//
//        Column(
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            modifier = Modifier
//                .padding(16.dp)
//                .padding(padding),
//        ) {
//            var prompt by remember { mutableStateOf("") }
//            TextField(
//                value = prompt,
//                onValueChange = {
//                    prompt = it
//                    scope.launch { suggestions = suggestionRepository.getSuggestions(it) }
//                },
//                placeholder = "prompt",
//            )
//
//            for (suggestion in suggestions) {
//                Text(
//                    text = suggestion,
//                    fontSize = 20.sp,
//                    color = DarkGray,
//                )
//            }
//        }

}

@Composable
private fun TaskCreationContent(
    stateScreen: TaskCreationScreenState
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Beige)
    ){
        Icon(
            painter = painterResource(R.drawable.logo_full_dark),
            contentDescription = "",
            tint = Color.Unspecified,
            modifier = Modifier.align(Alignment.Center)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 96.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CreateSubtaskField(
                text = stateScreen.newSubtask,
                onTextChange = stateScreen.onNewSubtaskChange,
                placeholder = "Enter subtask",
                onAddButtonClick = stateScreen.onSubtaskClick,
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    Chip("date date date", White, Black, {}, icon = painterResource(R.drawable.ic_clock))
                    Chip("high priority", AccentYellow, Black, {})
                    Chip("select category", White, LightGray, {})
                }
                BoxEdit(
                    value = stateScreen.task,
                    onValueChange = stateScreen.onTaskChange,
                    buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
                    inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
                    placeholder = "Enter text",
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

    val screenState = TaskCreationScreenState(
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
        onSubtaskClick = {},
        onAddTask = {},
    )

    TaskbenchTheme {
        TaskCreationContent(screenState)
    }
}
