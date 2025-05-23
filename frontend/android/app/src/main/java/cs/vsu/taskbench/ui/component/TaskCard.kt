package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.White

@Composable
fun LazyItemScope.TaskCard(
    deadlineText: String,
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
        // TODO: re-enable ripple once we have card expansion
        CompositionLocalProvider(
            LocalRippleConfiguration provides null,
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
                        )
                        Text(
                            text = deadlineText.ifBlank {
                                stringResource(R.string.label_deadline_missing)
                            },
                            fontSize = 16.sp,
                            color = if (deadlineText.isBlank()) LightGray else Black,
                        )
                    }

                    Text(
                        text = bodyText,
                        fontSize = 20.sp,
                    )

                    if (subtasks.isNotEmpty()) {
                        HorizontalDivider(
                            color = LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }

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
