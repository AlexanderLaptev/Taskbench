package cs.vsu.taskbench.ui.list

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cs.vsu.taskbench.ui.TaskbenchNavigation

fun NavGraphBuilder.taskListDestination() {
    composable<TaskbenchNavigation.TaskListScreen> {
        TaskListScreen()
    }
}

fun NavController.navigateToTaskList() {
    navigate(TaskbenchNavigation.TaskListScreen)
}
