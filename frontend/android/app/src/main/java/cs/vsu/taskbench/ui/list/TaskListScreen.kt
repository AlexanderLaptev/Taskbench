package cs.vsu.taskbench.ui.list

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import retrofit2.HttpException
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

        val context = LocalContext.current
        LaunchedEffect(Unit) {
            try {
                tasks = taskRepository.getTasks(null, TaskRepository.SortByMode.Priority)
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error", e)
                Toast.makeText(context, "HTTP error ${e.code()}", Toast.LENGTH_SHORT).show()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(padding),
        ) {
            items(
                count = tasks.size,
                key = { tasks[it].id!! },
            ) {
                val task = tasks[it]
                val debug = "${task.id}, P=${task.isHighPriority}, C=${task.categoryId}"
                val visible = remember { MutableTransitionState(true) }

                with(visible) {
                    LaunchedEffect(isIdle) {
                        if (isIdle && !currentState) {
                            taskRepository.deleteTask(task)
                            Log.d(TAG, "TaskListScreen: delete complete")
                            tasks = taskRepository.getTasks(
                                null,
                                TaskRepository.SortByMode.Priority
                            )
                            Log.d(TAG, "TaskListScreen: refresh after delete complete")
                        }
                    }
                }

                AnimatedVisibility(visibleState = visible) {
                    Column {
                        if (it != 0) Spacer(Modifier.height(8.dp))
                        TaskCard(
                            deadlineText = task.deadline?.let {
                                DateTimeFormatter.ISO_DATE_TIME.format(task.deadline)
                            } ?: "N/A",
                            bodyText = "[$debug] ${task.content}",
                            subtasks = task.subtasks,
                            onClick = { Log.d(TAG, "clicked task with id=${task.id}") },
                            modifier = Modifier
                                .animateItem()
                                .padding(horizontal = 16.dp),

                            onDismiss = {
                                visible.targetState = false
                                Log.d(TAG, "dismissed task with id=${task.id}")
                            },

                            onSubtaskCheckedChange = { subtask, selected ->
                                val verb = if (selected) "selected" else "deselected"
                                Log.d(TAG, "$verb subtask with id=${subtask.id}")
                            },
                        )
                    }
                }
            }
        }
    }
}
