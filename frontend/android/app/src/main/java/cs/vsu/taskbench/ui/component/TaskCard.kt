package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.White
import cs.vsu.taskbench.ui.util.formatDeadline
import java.time.LocalDateTime
import java.time.chrono.ChronoLocalDateTime

@Composable
fun LazyItemScope.TaskCard(
    deadline: LocalDateTime?,
    bodyText: String,
    subtasks: List<Subtask>,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    onSubtaskCheckedChange: (Subtask, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
) {
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) onDismiss()
    }

    SwipeToDismissBox(
        state = state,
        backgroundContent = {},
        enableDismissFromStartToEnd = false,
        gesturesEnabled = swipeEnabled || state.targetValue == SwipeToDismissBoxValue.EndToStart,
        modifier = Modifier.animateItem()
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = White,
                contentColor = Black,
            ),
            shape = RoundedCornerShape(10.dp),
            onClick = onClick,
            modifier = modifier,
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 8.dp
                ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier.padding(
                        start = 24.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    val overdue = if (deadline != null) {
                        deadline <= ChronoLocalDateTime.from(LocalDateTime.now())
                    } else false

                    Icon(
                        painter = painterResource(
                            if (overdue) {
                                R.drawable.ic_clock_overdue
                            } else R.drawable.ic_clock
                        ),
                        contentDescription = null,
                        tint = Color.Unspecified,
                    )
                    Text(
                        text = formatDeadline(deadline),
                        fontSize = 16.sp,
                        color = if (deadline == null) LightGray else Black,
                    )
                }

                Text(
                    text = bodyText,
                    fontSize = 20.sp,
                    modifier = modifier.padding(
                        start = 24.dp,
                        end = 8.dp,
                    ),
                )

                Column {
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
        val color = if (checked) LightGray else Black
        val decoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
        Text(
            text = content,
            color = color,
            fontSize = 16.sp,
            textDecoration = decoration,
        )
    }
}
