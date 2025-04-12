package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
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

private val BAR_WIDTH = 24.dp
private val SPACING = 8.dp

@Composable
fun WeekStatistics(
    levels: FloatArray,
    modifier: Modifier = Modifier,
) {
    require(levels.size == 7) { "levels array must contain exactly 7 floats" }

    Column(modifier = modifier) {
        // Labels
        Row(
            horizontalArrangement = Arrangement.spacedBy(SPACING),
        ) {
            Spacer(Modifier.width(BAR_WIDTH))
            for (id in DAYS_OF_WEEK_IDS) {
                val asText = stringResource(id)
                Text(
                    modifier = Modifier.width(BAR_WIDTH),
                    text = asText,
                    color = Black,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Bars
        Row(
            horizontalArrangement = Arrangement.spacedBy(SPACING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "50",
                color = LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(BAR_WIDTH),
            )

            Box {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SPACING),
                ) {
                    repeat(7) {
                        val level = levels[it]
                        VerticalBar(level)
                    }
                }

                // TODO: replace hardcoded size with proper measurements
                Canvas(
                    Modifier
                        .fillMaxHeight()
                        .width(BAR_WIDTH * 7 + SPACING * 6),
                ) {
                    val center = size.height / 2
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.0f, 10.0f))
                    drawLine(
                        color = LightGray,
                        start = Offset(0.0f, center),
                        end = Offset(size.width, center),
                        strokeWidth = 2.0f,
                        pathEffect = pathEffect,
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalBar(
    percent: Float,
    modifier: Modifier = Modifier,
) {
    // TODO: proper clipping
    require(percent in 0.0f..1.0f) { "percent must be in range 0.0-1.0 (inclusive)" }

    Canvas(
        modifier
            .fillMaxHeight()
            .width(BAR_WIDTH)
    ) {
        val radius = 10.dp.toPx()
        val cornerRadius = CornerRadius(radius, radius)
        val fillTop = size.height * (1.0f - percent)

        drawRoundRect(
            color = Beige,
            cornerRadius = cornerRadius,
        )
        drawRoundRect(
            color = AccentYellow,
            topLeft = Offset(0.0f, fillTop),
            size = Size(size.width, size.height - fillTop),
            cornerRadius = cornerRadius,
        )
    }
}

@Composable
@Preview(device = "spec:width=512px,height=512px,dpi=440")
private fun PreviewVerticalBar() {
    TaskbenchTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            VerticalBar(0.0f)
            VerticalBar(0.5f)
            VerticalBar(1.0f)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    TaskbenchTheme {
        WeekStatistics(
            levels = floatArrayOf(0.1f, 0.4f, 0.0f, 0.6f, 0.3f, 1.0f, 0.5f),
            modifier = Modifier.height(250.dp),
        )
    }
}
