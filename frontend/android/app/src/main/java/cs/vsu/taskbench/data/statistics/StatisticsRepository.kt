package cs.vsu.taskbench.data.statistics

import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.domain.model.Statistics
import java.time.LocalDate

interface StatisticsRepository : PreloadRepository {
    suspend fun getStatistics(date: LocalDate): Statistics
}
