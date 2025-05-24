package cs.vsu.taskbench.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

private val shape = RoundedCornerShape(10.dp)

@Composable
fun TitleDailyStats(
    text: String,
    dailyStats: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(White, shape)
            .size(width = 198.dp, height = 168.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),

        ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            color = Black,
            maxLines = 3,
            minLines = 3,
        )
        Text(
            text = dailyStats.toString(),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGray,
            maxLines = 3,
            minLines = 3,
        )
    }
}

@Composable
fun Title(
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(White, shape)
            .size(198.dp)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),

        ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .size(72.dp)
            )

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Black,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
@Preview
private fun Preview() {
    TaskbenchTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TitleDailyStats(
                    text = stringResource(R.string.label_done_today),
                    dailyStats = 21,
                )
                TitleDailyStats(
                    text = stringResource(R.string.label_done_all_time_high),
                    dailyStats = 33,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Title(
                    text = "Умное назначение  дедлайнов",
                    icon = R.drawable.img_deadline,
                )
                Title(
                    text = "Автоматическое определение приоритетов",
                    icon = R.drawable.img_priority,
                )
            }
        }
    }
}
