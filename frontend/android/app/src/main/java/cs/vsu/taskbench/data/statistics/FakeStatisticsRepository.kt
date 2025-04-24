package cs.vsu.taskbench.data.statistics

import android.util.Log
import androidx.collection.mutableFloatListOf
import cs.vsu.taskbench.domain.model.Statistics
import java.time.LocalDate
import kotlin.random.Random

object FakeStatisticsRepository : StatisticsRepository {
    private val TAG = FakeStatisticsRepository::class.simpleName

    private var cached: Statistics? = null

    override suspend fun preload(): Boolean {
        Log.d(TAG, "preloading statistics")
        generate()
        return true
    }

    override suspend fun getStatistics(date: LocalDate): Statistics {
        Log.d(TAG, "requested statistics")
        return cached ?: let {
            generate()
            cached!!
        }
    }

    private fun generate() {
        Log.d(TAG, "generating statistics")
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
