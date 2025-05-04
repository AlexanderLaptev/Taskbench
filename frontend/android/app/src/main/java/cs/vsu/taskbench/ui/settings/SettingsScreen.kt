package cs.vsu.taskbench.ui.settings

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("ShowToast")
@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun SettingsScreen(
    navController: NavController,
) {
    val scope = rememberCoroutineScope()
    val authService = koinInject<AuthService>()
    val navigator = navController.rememberDestinationsNavigator()

    Scaffold(
        bottomBar = { NavigationBar(navController) }
    ) { scaffoldPadding ->
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                )
                .padding(scaffoldPadding)
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
            MenuOption(
                text = stringResource(R.string.menu_settings_change_password),
                icon = painterResource(R.drawable.ic_edit),
                onClick = placeholder,
            )
            HorizontalDivider()
            MenuOption(
                text = stringResource(R.string.menu_settings_subscription),
                icon = painterResource(R.drawable.ic_gear),
                onClick = placeholder,
            )
            HorizontalDivider()
            MenuOption(
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
}

@Composable
private fun MenuOption(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(onClick = onClick)
            .height(52.dp)
            .fillMaxWidth(),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = DarkGray,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = DarkGray,
        )
    }
}
