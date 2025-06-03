package cs.vsu.taskbench.ui.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.analytics.AnalyticsFacade
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.TextField
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.ExtraLightGray
import cs.vsu.taskbench.ui.util.replaceMessage
import cs.vsu.taskbench.util.HttpStatusCodes
import cs.vsu.taskbench.util.mutableEventFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

private const val TAG = "PasswordChangeScreen"

@Composable
@Destination<SettingsGraph>(style = ScreenTransitions::class)
fun PasswordChangeScreen(
    navigator: DestinationsNavigator,
    snackState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val authService = koinInject<AuthService>()
    val errorFlow = remember { mutableEventFlow<String>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        errorFlow.collect { snackState.replaceMessage(it) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically),
        modifier = modifier,
    ) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        val context = LocalContext.current
        val resources = context.resources

        TextField(
            value = oldPassword,
            onValueChange = { oldPassword = it },
            placeholder = stringResource(R.string.placeholder_old_password),
            password = true,
        )
        TextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            placeholder = stringResource(R.string.placeholder_password),
            password = true,
        )
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = stringResource(R.string.placeholder_confirm_password),
            password = true,
        )
        Button(
            text = stringResource(R.string.button_confirm),
            color = AccentYellow,
            onClick = {
                if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                    Log.e(TAG, "password is empty")
                    errorFlow.tryEmit(resources.getString(R.string.error_empty_password))
                    return@Button
                } else if (
                    newPassword.length < AuthService.MIN_PASSWORD_LENGTH
                    || confirmPassword.length < AuthService.MIN_PASSWORD_LENGTH
                    || oldPassword.length < AuthService.MIN_PASSWORD_LENGTH
                ) {
                    Log.e(TAG, "password too short")
                    errorFlow.tryEmit(resources.getString(R.string.error_password_too_short))
                    return@Button
                } else if (newPassword != confirmPassword) {
                    Log.e(TAG, "passwords don't match")
                    errorFlow.tryEmit(resources.getString(R.string.error_passwords_do_not_match))
                    return@Button
                }

                scope.launch {
                    try {
                        authService.changePassword(oldPassword, newPassword)
                        AnalyticsFacade.logEvent("password_changed")

                        Toast.makeText(
                            context,
                            R.string.message_password_changed,
                            Toast.LENGTH_SHORT,
                        ).show()

                        navigator.navigateUp()
                        Log.d(TAG, "success!")
                    } catch (e: HttpException) {
                        if (e.code() == HttpStatusCodes.BAD_REQUEST) {
                            Log.e(TAG, "bad request", e)
                            errorFlow.tryEmit(resources.getString(R.string.error_change_password_failed))
                            return@launch
                        }

                        Log.e(TAG, "HTTP error", e)
                        AnalyticsFacade.logError("unknown", e)
                        errorFlow.tryEmit(resources.getString(R.string.error_unknown))
                    } catch (e: Exception) {
                        when (e) {
                            is ConnectException, is SocketTimeoutException -> {
                                Log.e(TAG, "connection error", e)
                                AnalyticsFacade.logError("connect", e)
                                errorFlow.tryEmit(resources.getString(R.string.error_could_not_connect))
                                return@launch
                            }
                        }

                        Log.e(TAG, "unknown error", e)
                        AnalyticsFacade.logError("unknown", e)
                        errorFlow.tryEmit(resources.getString(R.string.error_unknown))
                    }
                }
            },
        )
        Button(
            text = stringResource(R.string.button_back),
            color = ExtraLightGray,
            onClick = { navigator.navigateUp() },
        )
    }
}
