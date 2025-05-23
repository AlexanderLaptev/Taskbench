package cs.vsu.taskbench.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.user.UserRepository
import cs.vsu.taskbench.domain.model.User
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.theme.ExtraLightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
@Destination<SettingsGraph>(style = ScreenTransitions::class)
fun PremiumManagementScreen(navigator: DestinationsNavigator, modifier: Modifier = Modifier) {
    val userRepository = koinInject<UserRepository>()
    val user = userRepository.user!!
    Content(
        userStatus = user.status,
        onBack = { navigator.navigateUp() },
        modifier = modifier,
    )
}

@Composable
private fun Content(
    userStatus: User.Status,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically),
        modifier = modifier,
    ) {
        when (userStatus) {
            is User.Status.Premium -> {
                Text(text = "Premium until ${DateTimeFormatter.ISO_LOCAL_DATE.format(userStatus.activeUntil)}")
            }

            User.Status.Unpaid -> {
                Text(text = "Unpaid")
            }
        }

        Button(
            text = stringResource(R.string.button_back),
            color = ExtraLightGray,
            onClick = onBack,
        )
    }
}

@Preview
@Composable
private fun PreviewUnpaid() {
    TaskbenchTheme {
        Content(
            userStatus = User.Status.Unpaid,
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPremium() {
    TaskbenchTheme {
        Content(
            userStatus = User.Status.Premium(LocalDate.now().plusDays(7)),
            onBack = {},
        )
    }
}
