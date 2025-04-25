package cs.vsu.taskbench.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.domain.model.Statistics
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.WeekStatistics
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
        // TODO!
        val statsRepository = koinInject<StatisticsRepository>()
        var statistics by remember { mutableStateOf<Statistics?>(null) }

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

            Text(
                text = "Done today: ${statistics.doneToday}",
                fontSize = 20.sp,
                color = DarkGray,
            )

            Text(
                text = "All time high: ${statistics.doneAllTimeHigh}",
                fontSize = 20.sp,
                color = DarkGray,
            )

            val graphData = FloatArray(7) { statistics.graphData[it] }
            WeekStatistics(
                levels = graphData,
                modifier = Modifier
                    .background(color = White, shape = RoundedCornerShape(10.dp))
                    .padding(16.dp),
            )
        }
    }
}
