package cs.vsu.taskbench.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.TextField
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.ExtraLightGray

@Composable
@Destination<SettingsGraph>(style = ScreenTransitions::class)
fun PasswordChangeScreen(
    navigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically),
        modifier = modifier,
    ) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        TextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
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
            onClick = {},
        )
        Button(
            text = stringResource(R.string.button_back),
            color = ExtraLightGray,
            onClick = { navigator.navigateUp() },
        )
    }
}
