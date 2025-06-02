package cs.vsu.taskbench.data.statistics

import android.util.Log
import androidx.collection.mutableFloatListOf
import cs.vsu.taskbench.domain.model.Statistics
import cs.vsu.taskbench.util.MockRandom
import kotlinx.coroutines.delay
import java.time.LocalDate

object FakeStatisticsRepository : StatisticsRepository {
    private val TAG = FakeStatisticsRepository::class.simpleName

    private var cached: Statistics? = null

    override suspend fun preload() {
        Log.d(TAG, "preloading statistics")
        generate()
    }

    override suspend fun getStatistics(date: LocalDate): Statistics {
        Log.d(TAG, "requested statistics")
        delay(400)
        return cached ?: let {
            generate()
            cached!!
        }
    }

    private fun generate() {
        Log.d(TAG, "generating statistics")
        val values = mutableFloatListOf()
        repeat(7) {
            values += MockRandom.nextFloat()
        }
        cached = Statistics(
            doneToday = MockRandom.nextInt(0, 12),
            doneAllTimeHigh = MockRandom.nextInt(8, 22),
            graphData = values,
        )
    }
}
