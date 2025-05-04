package cs.vsu.taskbench.ui.create

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.dialog.TaskEditDialog
import cs.vsu.taskbench.ui.create.TaskCreationScreenViewModel.Error
import org.koin.androidx.compose.koinViewModel

@Composable
@Destination<RootGraph>(style = ScreenTransitions::class)
fun TaskCreationScreen(navController: NavController) {
    val viewModel = koinViewModel<TaskCreationScreenViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources

    LaunchedEffect(Unit) {
        viewModel.errorFlow.collect {
            val message = when (it) {
                Error.CouldNotConnect -> R.string.error_could_not_connect
                Error.Unknown -> R.string.error_unknown
            }
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = resources.getString(message),
                withDismissAction = true,
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { NavigationBar(navController) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { scaffoldPadding ->
        TaskEditDialog(
            stateHolder = viewModel,
            modifier = Modifier
                .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding())
                .padding(
                    top = 8.dp,
                    bottom = scaffoldPadding.calculateBottomPadding(),
                )
                .consumeWindowInsets(scaffoldPadding)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                )
        )
    }
}
