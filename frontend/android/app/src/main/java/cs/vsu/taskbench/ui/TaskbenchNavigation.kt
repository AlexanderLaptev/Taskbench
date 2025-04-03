package cs.vsu.taskbench.ui

import androidx.annotation.StringRes
import cs.vsu.taskbench.R
import kotlinx.serialization.Serializable

sealed interface TaskbenchNavigation {
    @Serializable
    data object TaskCreationScreen : TaskbenchNavigation

    @Serializable
    data object TaskListScreen : TaskbenchNavigation

    @Serializable
    data object StatisticsScreen : TaskbenchNavigation
}

data class TopLevelDestination(
    // TODO: add icons
    @StringRes val label: Int,
    val route: TaskbenchNavigation,
)

val topLevelDestinations = listOf(
    TopLevelDestination(
        R.string.screen_add_task,
        TaskbenchNavigation.TaskCreationScreen,
    ),
    TopLevelDestination(
        R.string.screen_task_list,
        TaskbenchNavigation.TaskListScreen,
    ),
    TopLevelDestination(
        R.string.screen_statistics,
        TaskbenchNavigation.StatisticsScreen,
    ),
)
