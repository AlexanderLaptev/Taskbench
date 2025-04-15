package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

@Composable
fun Subtask(
    text: String,
    selected: Boolean,
    buttonIcon: Painter,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) AccentYellow else White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = Black,
        )

        IconButton(
            onClick = onButtonClick,
        ) {
            Icon(buttonIcon, "")
        }
    }
}

@Composable
@Preview
private fun Preview() {
    TaskbenchTheme {
        Column {
            Subtask(
                text = "Пройтись по темам созвона",
                selected = true,
                buttonIcon = painterResource(R.drawable.icon_save),
                onButtonClick = {},
            )

            Subtask(
                text = "Подготовить текст",
                selected = false,
                buttonIcon = painterResource(R.drawable.icon_add),
                onButtonClick = {},
            )
        }
    }
}