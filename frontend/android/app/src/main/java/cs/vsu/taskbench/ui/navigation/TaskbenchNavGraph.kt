package cs.vsu.taskbench.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cs.vsu.taskbench.ui.create.taskCreationDestination
import cs.vsu.taskbench.ui.list.taskListDestination

@Composable
fun TaskbenchNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = TaskbenchNavigation.TaskCreationScreen,
    ) {
        taskCreationDestination()
        taskListDestination()
    }
}
