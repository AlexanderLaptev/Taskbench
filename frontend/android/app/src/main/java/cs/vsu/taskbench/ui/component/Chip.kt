package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

private val shape = RoundedCornerShape(10.dp)

@Composable
fun Chip(
    text: String,
    color: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconTint: Color = Color.Unspecified,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .background(color = color, shape = shape)
            .requiredHeight(32.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(20.dp)
                    .aspectRatio(1.0f),
            )
        }

        Text(
            text = text,
            fontSize = 16.sp,
            color = textColor,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TaskbenchTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Chip("lorem ipsum", White, Black, {})
            Chip("lorem ipsum", White, LightGray, {})
            Chip("lorem ipsum", AccentYellow, Black, {})
            Chip("lorem ipsum", White, Black, {}, icon = painterResource(R.drawable.ic_clock))
        }
    }
}

@Preview
@Composable
private fun PreviewScrollableRow() {
    TaskbenchTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            Chip("tomorrow, 17:30", White, Black, {}, icon = painterResource(R.drawable.ic_clock))
            Chip("high priority", AccentYellow, Black, {})
            Chip("select category", White, LightGray, {})
        }
    }
}
