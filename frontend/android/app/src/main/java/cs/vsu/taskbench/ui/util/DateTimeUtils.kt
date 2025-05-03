package cs.vsu.taskbench.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import cs.vsu.taskbench.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun formatDeadline(deadline: LocalDateTime?): String {
    if (deadline == null) return stringResource(R.string.chip_deadline)

    val datePattern = stringResource(R.string.pattern_date)
    val timePattern = stringResource(R.string.pattern_time)
    val dateFormatter = remember { DateTimeFormatter.ofPattern(datePattern) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern(timePattern) }

    val today = LocalDate.now()
    val date = when (deadline.toLocalDate()) {
        today -> stringResource(R.string.label_today)
        today.plusDays(1) -> stringResource(R.string.label_tomorrow)
        else -> dateFormatter.format(deadline)
    }
    val time = timeFormatter.format(deadline)
    return "$date, $time"
}
