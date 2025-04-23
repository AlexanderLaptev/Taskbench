package cs.vsu.taskbench.data.statistics

import cs.vsu.taskbench.model.Statistics
import java.time.LocalDate

interface StatisticsRepository {
    suspend fun getStatistics(date: LocalDate): Statistics
}
