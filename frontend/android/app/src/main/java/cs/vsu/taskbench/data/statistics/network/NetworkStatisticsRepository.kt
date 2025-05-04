package cs.vsu.taskbench.data.statistics.network

import android.util.Log
import androidx.collection.buildFloatList
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.withAuth
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.domain.model.Statistics
import java.time.LocalDate

class NetworkStatisticsRepository(
    private val authService: AuthService,
    private val dataSource: NetworkStatisticsDataSource,
) : StatisticsRepository {
    companion object {
        private val TAG = NetworkStatisticsRepository::class.simpleName
    }

    private var cache: Statistics? = null

    override suspend fun preload() {
        authService.withAuth {
            val response = dataSource.getStatistics(it)
            val graphData = buildFloatList(7) {
                for (i in response.weekly.indices) this += response.weekly[i]
            }
            cache = Statistics(
                doneToday = response.done_today,
                doneAllTimeHigh = response.max_done,
                graphData = graphData,
            )
        }
        Log.d(TAG, "preload: loaded statistics $cache")
    }

    override suspend fun getStatistics(date: LocalDate): Statistics {
        return cache!!
    }
}
