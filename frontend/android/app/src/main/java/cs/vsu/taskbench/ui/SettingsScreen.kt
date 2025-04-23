package cs.vsu.taskbench.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import cs.vsu.taskbench.data.SettingsRepository
import cs.vsu.taskbench.data.user.UserRepository
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.theme.LightGray
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun SettingsScreen(
    navController: NavController,
) {
    Scaffold(
        bottomBar = { NavigationBar(navController) }
    ) { padding ->
        // TODO!
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .padding(16.dp)
                .padding(padding),
        ) {
            val userRepository = koinInject<UserRepository>()
            val settingsRepository = koinInject<SettingsRepository>()

            val scope = rememberCoroutineScope()
            val navigator = navController.rememberDestinationsNavigator()

            Text(
                text = "Settings Screen",
                fontSize = 28.sp,
                color = LightGray,
            )

            Button(
                text = "Logout",
                onClick = {
                    scope.launch {
                        userRepository.logout()
                        settingsRepository.setJwtToken("")
                        navigator.popBackStack()
                        navigator.navigate(LoginScreenDestination)
                    }
                },
            )
        }
    }
}
