package cs.vsu.taskbench.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.theme.LightGray

@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun SettingsScreen(
    navController: NavController,
) {
    Scaffold(
        bottomBar = { NavigationBar(navController) }
    ) { padding ->
        // TODO!
        Text(
            text = "Settings Screen",
            fontSize = 28.sp,
            color = LightGray,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .padding(padding),
        )
    }
}
