package cs.vsu.taskbench.ui.create

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cs.vsu.taskbench.ui.navigation.TaskbenchNavigation

fun NavGraphBuilder.taskCreationDestination() {
    composable<TaskbenchNavigation.TaskCreationScreen> {
        TaskCreationScreen()
    }
}

fun NavController.navigateToTaskCreation() {
    navigate(TaskbenchNavigation.TaskCreationScreen) {
        launchSingleTop = true
        popUpTo(0)
    }
}
