package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

@Composable
fun TaskCard(
    deadlineText: String,
    bodyText: String,
    subtasks: List<Subtask>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onSubtaskCheckedChange: (Subtask, Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = White,
            contentColor = Black,
        ),
        shape = RoundedCornerShape(4.dp),
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
                Text(
                    text = deadlineText,
                    fontSize = 16.sp,
                    color = Black,
                )
            }

            Text(
                text = bodyText,
                fontSize = 20.sp,
            )

            HorizontalDivider(
                color = LightGray,
                thickness = 2.dp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (subtask in subtasks) {
                    var checked by remember { mutableStateOf(subtask.isDone) }
                    SubtaskComposable(
                        content = subtask.content,
                        checked = checked,
                        onCheckedChange = {
                            checked = !checked
                            onSubtaskCheckedChange(subtask, it)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SubtaskComposable(
    content: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked, onCheckedChange)
        Text(
            text = content,
            fontSize = 16.sp,
            color = Black,
        )
    }
}

@Composable
@Preview
private fun Preview() {
    val lipsum = LoremIpsum(8).values.first()
    val subtasks = listOf(
        Subtask(
            id = 1,
            content = lipsum,
            isDone = true,
        ),
        Subtask(
            id = 2,
            content = lipsum,
            isDone = true,
        ),
        Subtask(
            id = 3,
            content = lipsum,
            isDone = false,
        ),
        Subtask(
            id = 4,
            content = lipsum,
            isDone = false,
        ),
    )

    TaskbenchTheme {
        TaskCard(
            deadlineText = "tomorrow, 15:00",
            bodyText = LoremIpsum(25).values.first(),
            subtasks = subtasks,
            onClick = {},
            onSubtaskCheckedChange = { _, _ -> },
        )
    }
}
