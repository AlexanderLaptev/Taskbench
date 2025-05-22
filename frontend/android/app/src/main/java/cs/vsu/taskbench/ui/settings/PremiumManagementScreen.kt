package cs.vsu.taskbench.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.user.UserRepository
import cs.vsu.taskbench.domain.model.User
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.Title
import cs.vsu.taskbench.ui.component.dialog.ConfirmationDialog
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Active
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.ExtraLightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.launch
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
        modifier = modifier.background(color = Beige),
    )
}

@Composable
private fun Content(
    userStatus: User.Status,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                8.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        ) {
            when (userStatus) {
                is User.Status.Premium -> {
                    WithPremium(userStatus.activeUntil)
                }
                User.Status.Unpaid -> {
                    WithoutPremium()
                }
            }
        }
        IconButton(
            onClick = onBack,
            enabled = true,
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = null,
                colorFilter = null,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun WithPremium(
    activeUntil: LocalDate,
    ) {
    val scope = rememberCoroutineScope()
    var  showCancelPremiumConfirmDialog by remember { mutableStateOf(false) }
    if (showCancelPremiumConfirmDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.dialog_cancel_premium_text),
            onComplete = { confirmed ->
                showCancelPremiumConfirmDialog = false
                if (confirmed) {
                    scope.launch {
                        //todo: изменение статуса
                    }
                }
            },
        )
    }
    Row(
        modifier = Modifier
            .background(AccentYellow, RoundedCornerShape(10.dp))
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.logo_dark),
            tint = Color.Unspecified,
            contentDescription = null,
            modifier = Modifier
                .background(White, RoundedCornerShape(25.dp))
                .size(64.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val datePattern = stringResource(R.string.pattern_date)
            val dateFormatter =
                remember { DateTimeFormatter.ofPattern(datePattern) }

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.label_premium_status))
                    append(" ")
                    withStyle(
                        style = SpanStyle(
                            color = Active,
                            fontWeight = FontWeight.Bold,
                        )
                    ) { append(stringResource(R.string.label_premium_active)) }
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Black,
            )
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.label_premium_until))
                    append(" ")
                    withStyle(
                        style = SpanStyle(
                            color = DarkGray,
                            fontWeight = FontWeight.Bold,
                        ),
                    ) { append(dateFormatter.format(activeUntil)) }
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Black,
            )
        }
    }

    Button(
        text = "Отменить подписку на премиум",
        color = White,
        onClick = { showCancelPremiumConfirmDialog = true},
        textStyle = TextStyle(
            fontSize = 18.sp,
        )
    )
}

@Composable
private fun WithoutPremium() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Title(
            text = "Автоматическое выделение подзадач",
            icon = R.drawable.img_task,
        )
        Title(
            text = "Автоматическое определение приоритетов",
            icon = R.drawable.img_priority,
        )
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Title(
            text = "Умное назначение  дедлайнов",
            icon = R.drawable.img_deadline,
        )
        Title(
            text = "Автоматическое выделение категорий",
            icon = R.drawable.img_priority,
        )
    }

    Button(
        text = stringResource(R.string.button_buy_premium),
        color = AccentYellow,
        textStyle = TextStyle(
            fontSize = 18.sp,
            color = DarkGray,
            fontWeight = FontWeight.Black,
        ),
        onClick = {},
    )
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
