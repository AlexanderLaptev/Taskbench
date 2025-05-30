package cs.vsu.taskbench.ui.settings

import android.content.pm.PackageManager.NameNotFoundException
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.generated.destinations.PasswordChangeScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.analytics.AnalyticsFacade
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.ui.component.dialog.ConfirmationDialog
import cs.vsu.taskbench.ui.theme.DarkGray
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
@Destination<SettingsGraph>(start = true, style = ScreenTransitions::class)
fun SettingsMainMenu(
    globalNavigator: DestinationsNavigator,
    settingsNavigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        AnalyticsFacade.logScreen("SettingsMainMenu")
    }

    val scope = rememberCoroutineScope()
    val authService = koinInject<AuthService>()

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    if (showLogoutConfirmDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.dialog_logout_text),
            onComplete = { confirmed ->
                showLogoutConfirmDialog = false
                if (confirmed) {
                    scope.launch {
                        authService.logout()
                        globalNavigator.popBackStack()
                        globalNavigator.navigate(LoginScreenDestination) {
                            launchSingleTop = true
                        }
                    }
                }
            },
        )
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_full_dark),
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        val context = LocalContext.current
        var versionName = ""
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = if (packageInfo.versionName != null) "v" + packageInfo.versionName else ""
        } catch (_: NameNotFoundException) {
            // ignore
        }

        Text(
            text = versionName,
            color = DarkGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(
                    top = 4.dp,
                    end = 8.dp,
                    bottom = 32.dp,
                )
                .align(Alignment.End),
        )

        SettingsMenuOption(
            text = stringResource(R.string.menu_settings_change_password),
            icon = painterResource(R.drawable.ic_edit),
            onClick = { settingsNavigator.navigate(PasswordChangeScreenDestination) },
        )
        HorizontalDivider()
        SettingsMenuOption(
            text = stringResource(R.string.menu_settings_subscription),
            icon = painterResource(R.drawable.ic_gear),
            onClick = {},
        )
        HorizontalDivider()
        SettingsMenuOption(
            text = stringResource(R.string.menu_settings_categories),
            icon = painterResource(R.drawable.ic_list),
            onClick = {},
        )
        HorizontalDivider()
        SettingsMenuOption(
            text = stringResource(R.string.menu_settings_logout),
            icon = painterResource(R.drawable.ic_exit),
            onClick = { showLogoutConfirmDialog = true },
        )
    }
}
