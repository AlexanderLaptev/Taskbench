package cs.vsu.taskbench.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.SettingsMainMenuDestination
import com.ramcosta.composedestinations.generated.navgraphs.SettingsNavGraph
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import cs.vsu.taskbench.data.analytics.AnalyticsFacade
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.theme.White

@SuppressLint("ShowToast")
@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun SettingsScreen(
    navController: NavController,
) {
    LaunchedEffect(Unit) {
        AnalyticsFacade.logScreen("SettingsScreen")
    }

    Scaffold(
        bottomBar = { NavigationBar(navController) }
    ) { scaffoldPadding ->
        val settingsNavController = rememberNavController()
        DestinationsNavHost(
            navGraph = SettingsNavGraph,
            navController = settingsNavController,
            modifier = Modifier
                .padding(scaffoldPadding)
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                )
                .fillMaxSize()
                .background(color = White, shape = RoundedCornerShape(10.dp))
                .padding(16.dp),
        ) {
            composable(SettingsMainMenuDestination) {
                SettingsMainMenu(
                    globalNavigator = navController.rememberDestinationsNavigator(),
                    settingsNavigator = settingsNavController.rememberDestinationsNavigator(),
                )
            }
        }
    }
}
