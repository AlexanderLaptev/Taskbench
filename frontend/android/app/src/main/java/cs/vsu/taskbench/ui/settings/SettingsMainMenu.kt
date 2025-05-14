package cs.vsu.taskbench.ui.settings

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
@Destination<SettingsGraph>(start = true)
fun SettingsMainMenu(navigator: DestinationsNavigator, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val authService = koinInject<AuthService>()

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
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
        Image(
            painter = painterResource(R.drawable.logo_full_dark),
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            text = stringResource(R.string.version_number),
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

        val context = LocalContext.current
        var toast: Toast? = null
        val placeholder = remember {
            {
                toast?.cancel()
                toast = Toast.makeText(context, "Скоро будет :)", Toast.LENGTH_SHORT)
                    .apply { show() }
            }
        }
        SettingsMenuOption(
            text = stringResource(R.string.menu_settings_change_password),
            icon = painterResource(R.drawable.ic_edit),
            onClick = placeholder,
        )
        HorizontalDivider()
        SettingsMenuOption(
            text = stringResource(R.string.menu_settings_subscription),
            icon = painterResource(R.drawable.ic_gear),
            onClick = placeholder,
        )
        HorizontalDivider()
        SettingsMenuOption(
            text = stringResource(R.string.menu_settings_logout),
            icon = painterResource(R.drawable.ic_exit),
            onClick = {
                scope.launch {
                    authService.logout()
                    navigator.navigate(LoginScreenDestination)
                }
            },
        )
    }
}
