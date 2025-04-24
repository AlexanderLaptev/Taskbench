package cs.vsu.taskbench.domain.model

import androidx.collection.FloatList
import androidx.compose.runtime.Immutable

@Immutable
data class Statistics(
    val doneToday: Int,
    val doneAllTimeHigh: Int,
    val graphData: FloatList,
)
