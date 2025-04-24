package cs.vsu.taskbench.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TaskCreationScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.usecase.BootstrapUseCase
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Destination<RootGraph>(start = true, style = ScreenTransitions::class)
@Composable
fun SplashScreen(navigator: DestinationsNavigator) {
    val bootstrapUseCase = koinInject<BootstrapUseCase>()

    LaunchedEffect(Unit) {
        delay(300) // a little delay so the splash screen doesn't disappear instantly

        val result = bootstrapUseCase()
        val direction = when (result) {
            BootstrapUseCase.Result.Success -> TaskCreationScreenDestination
            BootstrapUseCase.Result.LoginRequired -> LoginScreenDestination
        }

        with(navigator) {
            popBackStack()
            navigate(direction)
        }
    }

    SplashScreenContent()
}

// Content extracted into a separate function to be easier to preview.
@Composable
private fun SplashScreenContent(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.logo_full_dark),
        contentDescription = "",
        modifier = modifier
            .background(Beige)
            .fillMaxSize()
            .wrapContentSize(),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF3DC)
@Composable
private fun Preview() {
    TaskbenchTheme {
        SplashScreenContent()
    }
}
