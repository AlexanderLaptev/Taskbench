package cs.vsu.taskbench.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.data.user.UserRepository
import cs.vsu.taskbench.domain.model.Statistics
import cs.vsu.taskbench.domain.model.User
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.TitleDailyStats
import cs.vsu.taskbench.ui.component.WeekStatistics
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Active
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.White
import org.koin.compose.koinInject
import java.net.ConnectException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "StatisticsScreen"

@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val statisticsRepository = koinInject<StatisticsRepository>()
    var statistics by remember { mutableStateOf<Statistics?>(null) }
    val userRepo = koinInject<UserRepository>()
    val user = userRepo.user!!

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { NavigationBar(navController) },
    ) { padding ->
        val resources = LocalContext.current.resources
        LaunchedEffect(Unit) {
            try {
                statistics = statisticsRepository.getStatistics(LocalDate.now())
            } catch (e: ConnectException) {
                Log.e(TAG, "connection error", e)
                snackbarHostState.showSnackbar(resources.getString(R.string.error_could_not_connect))
            } catch (e: Exception) {
                Log.e(TAG, "unknown error", e)
                snackbarHostState.showSnackbar(resources.getString(R.string.error_unknown))
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(padding),
        ) {
            if (statistics == null) return@Scaffold
            @Suppress("NAME_SHADOWING") val statistics = statistics!!
            val graphData = FloatArray(7) { statistics.graphData[it] }

            WeekStatistics(
                levels = graphData,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(color = White, shape = RoundedCornerShape(10.dp))
                    .padding(16.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TitleDailyStats(
                    text = stringResource(R.string.label_done_today),
                    dailyStats = statistics.doneToday,
                    modifier = Modifier.weight(1f),
                )
                TitleDailyStats(
                    text = stringResource(R.string.label_done_all_time_high),
                    dailyStats = statistics.doneAllTimeHigh,
                    modifier = Modifier.weight(1f),
                )
            }

            when (user.status) {
                is User.Status.Premium -> {
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
                                    ) { append(dateFormatter.format(user.status.activeUntil)) }
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = Black,
                            )
                        }
                    }
                }

                User.Status.Unpaid -> Button(
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
}
