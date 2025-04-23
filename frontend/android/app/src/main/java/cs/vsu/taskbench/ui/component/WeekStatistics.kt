package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

private val DAYS_OF_WEEK_IDS = listOf(
    R.string.monday_short,
    R.string.tuesday_short,
    R.string.wednesday_short,
    R.string.thursday_short,
    R.string.friday_short,
    R.string.saturday_short,
    R.string.sunday_short,
)

@Composable
fun WeekStatistics(
    levels: FloatArray,
    modifier: Modifier = Modifier,
) {
    require(levels.size == 7) { "Levels array must contain exactly 7 floats" }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        for (i in DAYS_OF_WEEK_IDS.indices) {
            val resId = DAYS_OF_WEEK_IDS[i]

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(resId),
                    color = Black,
                    fontSize = 20.sp,
                )

                val shape = RoundedCornerShape(10.dp)
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .background(color = Beige, shape = shape)
                        .clip(shape)
                        .size(width = 24.dp, height = 200.dp),
                ) {
                    Box(
                        modifier
                            .background(color = AccentYellow)
                            .width(24.dp)
                            .fillMaxHeight(levels[i])
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    TaskbenchTheme {
        WeekStatistics(
            levels = FloatArray(7) { it / 6.0f }
        )
    }
}
