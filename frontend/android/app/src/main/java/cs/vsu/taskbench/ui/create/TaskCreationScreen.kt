package cs.vsu.taskbench.ui.create

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.MockTaskEditDialogStateHolder
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.TaskEditDialog

@Composable
@Destination<RootGraph>(style = ScreenTransitions::class)
fun TaskCreationScreen(navController: NavController) {
    Scaffold(
        bottomBar = { NavigationBar(navController) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { scaffoldPadding ->
        TaskEditDialog(
            stateHolder = MockTaskEditDialogStateHolder,
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
