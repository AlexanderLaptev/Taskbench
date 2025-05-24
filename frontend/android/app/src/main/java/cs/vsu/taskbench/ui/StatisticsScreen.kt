package cs.vsu.taskbench.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.PremiumManagementScreenDestination
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.data.user.UserRepository
import cs.vsu.taskbench.domain.model.Statistics
import cs.vsu.taskbench.domain.model.User
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.TitleDailyStats
import cs.vsu.taskbench.ui.component.WeekStatistics
import cs.vsu.taskbench.ui.settings.WithPremium
import cs.vsu.taskbench.ui.settings.WithoutPremium
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.net.ConnectException
import java.time.LocalDate

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
    val destinationsNavigator = navController.rememberDestinationsNavigator()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { NavigationBar(navController) },
    ) { padding ->
        val resources = LocalContext.current.resources
        LaunchedEffect(Unit) {
            try {
                statistics = statisticsRepository.getCached()
                launch {
                    statistics = statisticsRepository.getActual(LocalDate.now())
                }
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
                    WithPremium(
                        user.status.activeUntil,
                        onClick = {
                            destinationsNavigator.navigate(
                                PremiumManagementScreenDestination
                            )
                        }
                    )
                }

                User.Status.Unpaid -> {
                    WithoutPremium(
                        onClick = {
                            destinationsNavigator.navigate(
                                PremiumManagementScreenDestination
                            )
                        }
                    )
                }
            }
        }

    }
}
