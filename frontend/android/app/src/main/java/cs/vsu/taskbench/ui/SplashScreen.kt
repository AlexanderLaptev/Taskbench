package cs.vsu.taskbench.ui

import android.util.Log
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
import java.net.ConnectException

private class NoConnectionException : RuntimeException()

@Destination<RootGraph>(start = true, style = ScreenTransitions::class)
@Composable
fun SplashScreen(navigator: DestinationsNavigator) {
    val bootstrapUseCase = koinInject<BootstrapUseCase>()
    var exception: Exception? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        delay(300) // a little delay so the splash screen doesn't disappear instantly
        try {
            val result = bootstrapUseCase()
            val direction = when (result) {
                Result.Success -> TaskCreationScreenDestination
                Result.LoginRequired -> LoginScreenDestination
                Result.NoInternet -> throw NoConnectionException()
            }
            with(navigator) {
                popBackStack()
                navigate(direction)
            }
        } catch (e: Exception) {
            Log.e("SplashScreen", "exception during bootstrap", e)
            exception = e
        }
    }

    if (exception != null) {
        val errorMessage = when (exception) {
            is ConnectException -> stringResource(R.string.error_could_not_connect)
            is NoConnectionException -> stringResource(R.string.error_no_internet)
            else -> "N/A"
        }

        val activity = LocalActivity.current
        AlertDialog(
            onDismissRequest = { activity!!.finishAffinity() },
            containerColor = Beige,
            textContentColor = DarkGray,
            titleContentColor = Black,
            title = { Text(text = stringResource(R.string.dialog_title_error)) },
            text = { Text(text = stringResource(R.string.error_bootstrap_failed, errorMessage)) },
            confirmButton = {
                TextButton(
                    onClick = { activity!!.finishAffinity() }
                ) { Text(text = "OK", color = Black) }
            },
        )
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
