package cs.vsu.taskbench.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.navgraphs.SettingsNavGraph
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.NavigationBar

@SuppressLint("ShowToast")
@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun SettingsScreen(
    navController: NavController,
) {
    Scaffold(
        bottomBar = { NavigationBar(navController) }
    ) { scaffoldPadding ->
        DestinationsNavHost(
            navGraph = SettingsNavGraph,
            modifier = Modifier.padding(scaffoldPadding),
        )
    }
}
