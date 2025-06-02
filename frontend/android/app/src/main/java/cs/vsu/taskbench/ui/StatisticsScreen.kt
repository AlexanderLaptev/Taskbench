package cs.vsu.taskbench.ui

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.BuyPremiumScreenDestination
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.analytics.AnalyticsFacade
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.data.subscription.SubscriptionManager
import cs.vsu.taskbench.domain.model.Statistics
import cs.vsu.taskbench.domain.model.UserStatus
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.WeekStatistics
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import cs.vsu.taskbench.ui.util.replaceMessage
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "StatisticsScreen"

@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
) {
    val snackState = remember { SnackbarHostState() }
    val destinationsNavigator = navController.rememberDestinationsNavigator()

    val statisticsRepository = koinInject<StatisticsRepository>()
    var statistics by remember { mutableStateOf<Statistics?>(null) }

    val subscriptionManager = koinInject<SubscriptionManager>()
    val userStatus = runBlocking { subscriptionManager.getStatus() }

    val resources = LocalContext.current.resources
    LaunchedEffect(Unit) {
        AnalyticsFacade.logScreen("StatisticsScreen")

        try {
            statistics = statisticsRepository.getStatistics(LocalDate.now())
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is SocketTimeoutException -> {
                    Log.e(TAG, "connect exception", e)
                    AnalyticsFacade.logError("connect", e)
                    snackState.replaceMessage(resources.getString(R.string.error_could_not_connect))
                }

                else -> {
                    Log.e(TAG, null, e)
                    AnalyticsFacade.logError("unknown", e)
                    snackState.replaceMessage(resources.getString(R.string.error_unknown))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackState) },
        bottomBar = { NavigationBar(navController) },
    ) { scaffoldPadding ->
        AnimatedContent(statistics) { stats ->
            when (stats) {
                null -> ContentLoading(modifier = Modifier.padding(scaffoldPadding))
                else -> {
                    val graphData = FloatArray(7) { stats.graphData[it] }
                    Content(
                        graphLevels = graphData,
                        doneToday = stats.doneToday,
                        allTimeHigh = stats.doneAllTimeHigh,
                        userStatus = userStatus,
                        onBuy = {
                            AnalyticsFacade.logEvent("buy_premium_ad_clicked")
                            destinationsNavigator.navigate(BuyPremiumScreenDestination())
                        },
                        modifier = Modifier.padding(scaffoldPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun Content(
    graphLevels: FloatArray,
    doneToday: Int,
    allTimeHigh: Int,
    userStatus: UserStatus,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = White, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.label_week_statistics),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            WeekStatistics(
                levels = graphLevels,
            )
            if (graphLevels.all { it > 0.5f }) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.label_good_job),
                    color = DarkGray,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                )
                Spacer(Modifier.height(8.dp))
            } else {
                Spacer(Modifier.height(16.dp))
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(IntrinsicSize.Max),
        ) {
            StatsTile(
                title = stringResource(R.string.label_done_today),
                value = doneToday.toString(),
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight(),
            )
            StatsTile(
                title = stringResource(R.string.label_done_all_time_high),
                value = allTimeHigh.toString(),
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight(),
            )
        }

        when (userStatus) {
            is UserStatus.Premium -> WhenPaid(userStatus = userStatus)
            is UserStatus.Unpaid -> WhenUnpaid(onBuy = onBuy)
        }
    }
}

@Composable
private fun ContentLoading(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(16.dp)
            .background(color = White, shape = RoundedCornerShape(10.dp))
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Загрузка...",
            fontSize = 20.sp,
        )
        CircularProgressIndicator(
            color = AccentYellow,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun StatsTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(color = White, shape = RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth(),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(),
        ) {
            val density = LocalDensity.current
            Text(
                text = value,
                color = DarkGray,
                fontSize = with(density) { 56.dp.toSp() },
                modifier = Modifier
            )
            Text(
                text = stringResource(R.string.label_stats_tile_tasks),
                color = DarkGray,
                fontSize = 20.sp,
                modifier = Modifier.offset(y = (-8).dp),
            )
        }
    }
}

@Composable
private fun WhenUnpaid(
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(color = AccentYellow, shape = RoundedCornerShape(10.dp))
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.text_stats_premium_part1),
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.text_stats_premium_part2))
                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                    append(stringResource(R.string.text_subscription_cost))
                }
                append(stringResource(R.string.text_stats_premium_part3))
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            text = stringResource(R.string.button_stats_premium),
            onClick = onBuy,
            color = White,
        )
    }
}

@Composable
fun WhenPaid(userStatus: UserStatus.Premium, modifier: Modifier = Modifier) {
    if (!userStatus.isActive) return

    val pattern = stringResource(R.string.pattern_date)
    val dateFormatter = remember { DateTimeFormatter.ofPattern(pattern) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(color = White, shape = RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Text(
            text = stringResource(R.string.label_premium_active),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(
                R.string.label_premium_until,
                dateFormatter.format(userStatus.nextPayment)
            ),
            color = DarkGray,
            fontSize = 16.sp,
        )
    }
}

private val previewLevels = floatArrayOf(0.83f, 0.59f, 0.76f, 1.0f, 0.57f, 0.91f, 0.73f)
private const val PREVIEW_DONE_TODAY = 18
private const val PREVIEW_DONE_MAX = 29

@Preview
@Composable
private fun PreviewUnpaid() {
    TaskbenchTheme {
        Content(
            graphLevels = previewLevels,
            doneToday = PREVIEW_DONE_TODAY,
            allTimeHigh = PREVIEW_DONE_MAX,
            userStatus = UserStatus.Premium(0, LocalDateTime.now(), true, 0),
            onBuy = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPremium() {
    TaskbenchTheme {
        Content(
            graphLevels = previewLevels,
            doneToday = PREVIEW_DONE_TODAY,
            allTimeHigh = PREVIEW_DONE_MAX,
            userStatus = UserStatus.Unpaid(0),
            onBuy = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    TaskbenchTheme {
        ContentLoading()
    }
}
