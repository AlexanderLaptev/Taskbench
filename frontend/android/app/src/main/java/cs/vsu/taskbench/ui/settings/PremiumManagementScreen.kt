package cs.vsu.taskbench.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.ramcosta.composedestinations.annotation.RootGraph
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
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
@Destination<RootGraph>(style = ScreenTransitions::class)
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
    val scope = rememberCoroutineScope()
    var showCancelPremiumConfirmDialog by remember { mutableStateOf(false) }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige)
            .padding(
                start = 16.dp,
                top = 32.dp,
                end = 16.dp,
                bottom = 16.dp,
            )
            .fillMaxSize()
            .background(color = Beige, shape = RoundedCornerShape(10.dp)),
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
                    WithPremium(
                        userStatus.activeUntil,
                        onClick = {}
                    )
                    Button(
                        text = stringResource(R.string.button_cancel_premium),
                        color = White,
                        onClick = { showCancelPremiumConfirmDialog = true },
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                        )
                    )
                }

                User.Status.Unpaid -> {
                    Icon(
                        painter = painterResource(R.drawable.logo_dark),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(160.dp)
                            .background(White, shape = RoundedCornerShape(30.dp)),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WithoutPremium(
                        onClick = {}
                    )
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
fun WithPremium(
    activeUntil: LocalDate,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
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
                .background(White, RoundedCornerShape(16.dp))
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
}

@Composable
fun WithoutPremium(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Title(
            text = stringResource(R.string.label_advantage_of_premium_1),
            icon = R.drawable.img_task,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        )
        Title(
            text = stringResource(R.string.label_advantage_of_premium_2),
            icon = R.drawable.img_priority,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        )
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Title(
            text = stringResource(R.string.label_advantage_of_premium_3),
            icon = R.drawable.img_deadline,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        )
        Title(
            text = stringResource(R.string.label_advantage_of_premium_4),
            icon = R.drawable.img_category,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
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
