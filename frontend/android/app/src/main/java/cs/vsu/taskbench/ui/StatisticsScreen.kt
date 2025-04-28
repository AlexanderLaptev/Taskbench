package cs.vsu.taskbench.ui

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
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
import java.time.LocalDate

@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
) {
    Scaffold(
        bottomBar = { NavigationBar(navController) }
    ) { padding ->
        val statsRepository = koinInject<StatisticsRepository>()
        var statistics by remember { mutableStateOf<Statistics?>(null) }
        val userRepo = koinInject<UserRepository>()
        val user = userRepo.user!!

        LaunchedEffect(Unit) {
            statistics = statsRepository.getStatistics(LocalDate.now())
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
                    text = stringResource(R.string.text_stats_for_today),
                    dailyStats = statistics.doneToday,
                    modifier = Modifier.weight(1f),
                )
                TitleDailyStats(
                    text = stringResource(R.string.text_max_stats_for_all_time),
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
                                .size(80.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                buildAnnotatedString {
                                    append(stringResource(R.string.text_status_premium))
                                    withStyle(
                                        style = SpanStyle(
                                            color = Active,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append(stringResource(R.string.text_active))
                                    }
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = Black,
                            )
                            Text(
                                text = "Подписка истекает: ${user.status.activeUntil}",
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
                        fontWeight = FontWeight.Black
                    ),
                    onClick = {}
                )
            }

        }
    }
}
