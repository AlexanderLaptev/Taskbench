package cs.vsu.taskbench.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.analytics.AnalyticsFacade
import cs.vsu.taskbench.data.subscription.SubscriptionManager
import cs.vsu.taskbench.domain.model.UserStatus
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.dialog.ConfirmationDialog
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.ExtraLightGray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import java.time.format.DateTimeFormatter

private const val TAG = "ManageSubscriptionScreen"

@Composable
@Destination<SettingsGraph>(style = ScreenTransitions::class)
fun ManageSubscriptionScreen(
    navigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
) {
    val subscriptionManager = koinInject<SubscriptionManager>()
    val scope = rememberCoroutineScope()
    var userStatus by remember {
        mutableStateOf(
            runBlocking {
                subscriptionManager.getStatus() as UserStatus.Premium
            }
        )
    }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    if (showConfirmationDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.dialog_confirm_subscription_cancel),
            onComplete = { confirm ->
                if (confirm) {
                    scope.handleErrors {
                        AnalyticsFacade.logEvent("premium_cancelled")
                        subscriptionManager.deactivate()
                        userStatus = subscriptionManager.updateStatus() as UserStatus.Premium
                    }
                }
                showConfirmationDialog = false
            },
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(
                if (userStatus.isActive) {
                    R.string.label_premium_active
                } else R.string.label_premium_inactive
            ),
            fontSize = 24.sp,
        )

        val pattern = stringResource(R.string.pattern_date)
        val formatter = remember { DateTimeFormatter.ofPattern(pattern) }
        val label = stringResource(
            if (userStatus.isActive) {
                R.string.label_premium_next_payment
            } else R.string.label_premium_until,
            formatter.format(userStatus.nextPayment)
        )

        Text(
            text = label,
            fontSize = 18.sp,
        )

        if (userStatus.isActive) {
            Button(
                text = stringResource(R.string.button_cancel_premium),
                color = ExtraLightGray,
                onClick = {
                    AnalyticsFacade.logEvent("premium_cancel_clicked")
                    showConfirmationDialog = true
                },
            )
        } else {
            Button(
                text = stringResource(R.string.button_reactivate_premium),
                color = AccentYellow,
                onClick = {
                    scope.handleErrors {
                        subscriptionManager.activate()
                        AnalyticsFacade.logEvent("premium_reactivated")
                        userStatus = subscriptionManager.updateStatus() as UserStatus.Premium
                    }
                },
            )
        }

        Button(
            text = stringResource(R.string.button_back),
            color = ExtraLightGray,
            onClick = { navigator.navigateUp() },
        )
    }
}

private inline fun CoroutineScope.handleErrors(crossinline block: suspend () -> Unit) {
    launch {
        try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, null, e)
        }
    }
}
