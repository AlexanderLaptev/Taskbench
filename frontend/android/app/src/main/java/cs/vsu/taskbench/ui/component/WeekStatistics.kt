package cs.vsu.taskbench.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.LightGray
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

private val GRAPH_BAR_WIDTH = 32.dp
private val GRAPH_BAR_HEIGHT = 200.dp
private val ARRANGEMENT: Arrangement.Horizontal = Arrangement.spacedBy(8.dp)
private val GRAPH_BAR_SHAPE = RoundedCornerShape(10.dp)

@Composable
fun WeekStatistics(
    levels: FloatArray,
    modifier: Modifier = Modifier,
) {
    require(levels.size == 7) { "Levels array must contain exactly 7 floats" }
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = ARRANGEMENT,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            for (resId in DAYS_OF_WEEK_IDS) {
                Text(
                    text = stringResource(resId),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = DarkGray,
                    modifier = Modifier.width(GRAPH_BAR_WIDTH)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Box(contentAlignment = Alignment.Center) {
            Row(
                horizontalArrangement = ARRANGEMENT,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                repeat(7) {
                    GraphBar(levels[it])
                }
            }
            Canvas(Modifier.matchParentSize()) {
                val dashCount = 14.0f
                val dash = size.width / (2.0f * dashCount - 1.0f)
                val half = size.height / 2.0f
                val start = Offset(x = 0.0f, y = half)
                val end = Offset(x = size.width, y = half)
                drawLine(
                    color = LightGray,
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
                )
            }
        }
    }
}

@Composable
private fun GraphBar(level: Float, modifier: Modifier = Modifier) {
    val animatedLevel = remember { Animatable(0.0f) }
    LaunchedEffect(level) { animatedLevel.animateTo(level, animationSpec = tween(800)) }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .background(color = Beige, shape = GRAPH_BAR_SHAPE)
            .clip(GRAPH_BAR_SHAPE)
            .size(width = GRAPH_BAR_WIDTH, height = GRAPH_BAR_HEIGHT),
    ) {
        Box(
            Modifier
                .background(color = AccentYellow)
                .width(GRAPH_BAR_WIDTH)
                .fillMaxHeight(animatedLevel.value)
        )
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
