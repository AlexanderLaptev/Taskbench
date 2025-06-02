package cs.vsu.taskbench.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TaskCreationScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.usecase.BootstrapUseCase
import cs.vsu.taskbench.domain.usecase.BootstrapUseCase.Result
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private const val MIN_DURATION = 300L

@Destination<RootGraph>(start = true, style = ScreenTransitions::class)
@Composable
fun SplashScreen(navigator: DestinationsNavigator) {
    val bootstrapUseCase = koinInject<BootstrapUseCase>()
    var result by remember { mutableStateOf<Result?>(null) }

    LaunchedEffect(Unit) {
        val start = System.currentTimeMillis()
        result = bootstrapUseCase()
        val end = System.currentTimeMillis()

        val duration = end - start
        if (duration < MIN_DURATION) delay(MIN_DURATION - duration)
    }

    when (result!!) {
        Result.Success -> {
            with(navigator) {
                popBackStack()
                navigate(TaskCreationScreenDestination)
            }
        }

        Result.LoginRequired -> {
            with(navigator) {
                popBackStack()
                navigate(LoginScreenDestination)
            }
        }

        Result.CouldNotConnect -> ErrorMessage(stringResource(R.string.error_could_not_connect))
        Result.NoInternet -> ErrorMessage(stringResource(R.string.error_no_internet))
        is Result.UnknownError -> ErrorMessage(stringResource(R.string.error_unknown))
    }

    SplashScreenContent()
}

@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    val activity = LocalActivity.current
    AlertDialog(
        onDismissRequest = { activity!!.finishAffinity() },
        containerColor = Beige,
        textContentColor = DarkGray,
        titleContentColor = Black,
        modifier = modifier,
        title = { Text(text = stringResource(R.string.dialog_title_error)) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = { activity!!.finishAffinity() }
            ) { Text(text = "OK", color = Black) }
        },
    )
}

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
