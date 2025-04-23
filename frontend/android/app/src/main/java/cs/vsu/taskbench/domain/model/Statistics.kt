package cs.vsu.taskbench.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Statistics(
    val doneToday: Int,
    val doneAllTimeHigh: Int,
    val graphData: List<Float>,
)
