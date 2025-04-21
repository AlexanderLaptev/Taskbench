package cs.vsu.taskbench.ui.component

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import cs.vsu.taskbench.model.Subtask as SubtaskModel

// TODO: add checkbox interactions

@Composable
fun TaskCard(
    deadlineText: String,
    bodyText: String,
    subtasks: Iterable<SubtaskModel>,
    modifier: Modifier = Modifier,
) {
    // Card
    Column(
        modifier = modifier
            .background(
                color = White,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Deadline
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_clock),
                contentDescription = "",
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = deadlineText,
                color = Black,
            )
        }

        // Body
        Text(
            text = bodyText,
            fontSize = 20.sp,
        )

        // Subtasks
        Column {
            for (subtask in subtasks) Subtask(subtask.isDone, subtask.content)
        }
    }
}

@Composable
private fun Subtask(
    done: Boolean,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(done, {})
        Text(text)
    }
}

@Composable
@Preview
private fun PreviewWithSubtasks() {
    TaskbenchTheme {
        val subtasks = listOf(
            SubtaskModel(1, "Подзадача номер раз", false),
            SubtaskModel(2, "Подзадача номер два но длинная жестб", true),
            SubtaskModel(3, "Подзадача номер три", true),
            SubtaskModel(4, "Подзадача номер 4", true),
        )

        TaskCard(
            "сегодня, 15:00",
            "Очень длинная задача жесть прям даже с переносом слов вот так вот. И еще немного буков.",
            subtasks
        )
    }
}

@Composable
@Preview
private fun PreviewNoSubtasks() {
    TaskbenchTheme {
        val subtasks = listOf<SubtaskModel>()
        TaskCard(
            "сегодня, 15:00",
            "Очень длинная задача жесть прям даже с переносом слов вот так вот. И еще немного буков.",
            subtasks
        )
    }
}
