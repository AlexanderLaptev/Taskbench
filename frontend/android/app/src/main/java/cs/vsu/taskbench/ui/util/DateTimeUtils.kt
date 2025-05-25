package cs.vsu.taskbench.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import cs.vsu.taskbench.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
private fun getFormattedDeadlineStringInternal(
    deadline: LocalDateTime,
    dateFormatter: DateTimeFormatter,
    timeFormatter: DateTimeFormatter,
    checkOverdue: Boolean,
    currentDateTime: LocalDateTime
): String {
    val deadlineDate = deadline.toLocalDate()
    val today = currentDateTime.toLocalDate()

    if (checkOverdue && deadline.isBefore(currentDateTime)) {
        return stringResource(R.string.label_overdue)
    }

    val dateText = when (deadlineDate) {
        today -> stringResource(R.string.label_today)
        today.plusDays(1) -> stringResource(R.string.label_tomorrow)
        else -> dateFormatter.format(deadline)
    }
    val timeText = timeFormatter.format(deadline)

    return "$dateText, $timeText"
}

@Composable
private fun formatDeadline(deadline: LocalDateTime?, checkForOverdue: Boolean = false): String {
    if (deadline == null) return stringResource(R.string.label_deadline_missing)

    val datePattern = stringResource(R.string.pattern_date)
    val timePattern = stringResource(R.string.pattern_time)

    val dateFormatter = remember(datePattern) { DateTimeFormatter.ofPattern(datePattern) }
    val timeFormatter = remember(timePattern) { DateTimeFormatter.ofPattern(timePattern) }

    val currentDateTime = remember { LocalDateTime.now() }

    return getFormattedDeadlineStringInternal(
        deadline = deadline,
        dateFormatter = dateFormatter,
        timeFormatter = timeFormatter,
        checkOverdue = checkForOverdue,
        currentDateTime = currentDateTime
    )
}

@Composable
fun formatDeadlineOriginal(deadline: LocalDateTime?): String {
    return formatDeadline(deadline, checkForOverdue = false)
}

@Composable
fun formatDeadlineForTaskCard(deadline: LocalDateTime?): String {
    return formatDeadline(deadline, checkForOverdue = true)
}