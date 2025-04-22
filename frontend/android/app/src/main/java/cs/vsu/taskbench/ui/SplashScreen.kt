package cs.vsu.taskbench.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TaskCreationScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.SettingsRepository
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Destination<RootGraph>(start = true)
@Composable
fun SplashScreen(navigator: DestinationsNavigator) {
    val settingsRepository = koinInject<SettingsRepository>()
    LaunchedEffect(Unit) {
        delay(700)
        val settings = settingsRepository.flow.first()
        navigator.popBackStack()
        navigator.navigate(
            if (settings.isLoggedIn) {
                TaskCreationScreenDestination
            } else LoginScreenDestination
        )
    }
    SplashScreenContent()
}

@Composable
private fun SplashScreenContent(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.logo_full_dark),
        contentDescription = "",
        modifier = modifier
            .offset(y = (-60).dp)
            .fillMaxSize()
            .wrapContentSize()
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFF3DC)
private fun Preview() {
    TaskbenchTheme {
        SplashScreenContent()
    }
}
