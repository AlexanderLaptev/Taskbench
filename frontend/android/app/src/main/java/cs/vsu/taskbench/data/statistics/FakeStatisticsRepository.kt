package cs.vsu.taskbench.data.statistics

import androidx.collection.mutableFloatListOf
import cs.vsu.taskbench.domain.model.Statistics
import java.time.LocalDate
import kotlin.random.Random

object FakeStatisticsRepository : StatisticsRepository {
    private var cached: Statistics? = null

    override suspend fun preload(): Boolean {
        generate()
        return true
    }

    override suspend fun getStatistics(date: LocalDate): Statistics {
        return cached ?: let {
            generate()
            cached!!
        }
    }

    private fun generate() {
        val values = mutableFloatListOf()
        repeat(7) {
            values += Random.nextFloat()
        }
        cached = Statistics(
            doneToday = Random.nextInt(0, 12),
            doneAllTimeHigh = Random.nextInt(8, 22),
            graphData = values,
        )
    }
}
