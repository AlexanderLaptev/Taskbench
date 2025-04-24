package cs.vsu.taskbench.ui.list

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.domain.model.Task
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.TaskCard
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "TaskListScreen"

@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun TaskListScreen(
    navController: NavController,
) {
    Scaffold(
        bottomBar = { NavigationBar(navController) }
    ) { padding ->
        // TODO!
        var tasks by remember { mutableStateOf(listOf<Task>()) }
        val taskRepository = koinInject<TaskRepository>()

        LaunchedEffect(Unit) {
            tasks = taskRepository.getTasks(LocalDate.now(), TaskRepository.SortByMode.Priority)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxHeight()
                .padding(padding),
        ) {
            items(
                items = tasks,
                key = { it.id!! },
            ) {
                TaskCard(
                    deadlineText = DateTimeFormatter.ISO_DATE_TIME.format(it.deadline),
                    bodyText = "[${it.id}, P=${it.isHighPriority}] ${it.content}",
                    subtasks = it.subtasks,
                    onClick = { Log.d(TAG, "clicked task with id=${it.id}") },
                    onSubtaskCheckedChange = { subtask, selected ->
                        val verb = if (selected) "selected" else "deselected"
                        Log.d(TAG, "$verb subtask with id=${subtask.id}")
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
