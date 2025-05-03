package cs.vsu.taskbench.ui.component.dialog

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.view.HapticFeedbackConstantsCompat
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import androidx.compose.material3.DatePickerDialog as MaterialDatePickerDialog
import androidx.compose.material3.TimePickerDialog as MaterialTimePickerDialog

@Composable
@NonRestartableComposable
fun ConfirmationDialog(
    text: String,
    onComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    ignoreIndirectDismiss: Boolean = false,
) {
    AlertDialog(
        containerColor = Beige,
        modifier = modifier,

        onDismissRequest = {
            if (!ignoreIndirectDismiss) onComplete(false)
        },

        confirmButton = {
            TextButton(
                onClick = { onComplete(true) }
            ) {
                Text(
                    text = stringResource(R.string.button_yes),
                    color = Black,
                )
            }
        },

        dismissButton = {
            TextButton(
                onClick = { onComplete(false) }
            ) {
                Text(
                    text = stringResource(R.string.button_no),
                    color = Black,
                )
            }
        },

        text = {
            Text(
                text = text,
                fontSize = 16.sp,
                color = Black,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onComplete: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    val now = LocalTime.now()
    val pickerState = rememberTimePickerState(now.hour, now.minute)

    var first by remember { mutableStateOf(true) }
    val view = LocalView.current
    LaunchedEffect(pickerState.hour, pickerState.minute) {
        if (!first) {
            view.performHapticFeedback(HapticFeedbackConstantsCompat.CLOCK_TICK)
        }
        first = false
    }

    // FIXME: containerColor currently does nothing [material3:1.4.0-alpha13]
    // It's being used in Surface in modifier.background() instead
    // of in the dedicated 'color' parameter, so the default surface
    // color overrides the color specified here. I have overridden
    // our theme's 'surface' color since we specify all colors manually
    // anyway. I *could* wrap this in another MaterialTheme or code my
    // own dialog from scratch, but the former is an even greater hack
    // and the latter I can't be bothered with.

    MaterialTimePickerDialog(
        onDismissRequest = onDismiss,
        containerColor = Beige,
        modifier = modifier,

        title = {
            Text(
                text = stringResource(R.string.dialog_select_time),
                fontSize = 14.sp,
                color = Black,
            )
        },

        confirmButton = {
            TextButton(
                onClick = { onComplete(pickerState.hour, pickerState.minute) }
            ) {
                Text(
                    text = stringResource(R.string.button_select),
                    color = Black,
                )
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.button_cancel),
                    color = Black,
                )
            }
        },
    ) {
        TimePicker(
            state = pickerState,
            colors = TimePickerDefaults.colors(
                clockDialColor = White,
                clockDialSelectedContentColor = Black,
                clockDialUnselectedContentColor = Black,
                selectorColor = AccentYellow,
                timeSelectorSelectedContainerColor = AccentYellow,
                timeSelectorSelectedContentColor = Black,
                timeSelectorUnselectedContainerColor = White,
                timeSelectorUnselectedContentColor = Black,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onComplete: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentDateMillis = run {
        LocalDate
            .now()
            .atTime(LocalTime.MIDNIGHT)
            .atZone(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDateMillis,
    )
    val indicatorColor = Color(0xFFBFBFBF)
    val pickerColors = DatePickerDefaults.colors(
        containerColor = Beige,
        titleContentColor = DarkGray,
        headlineContentColor = Black,
        weekdayContentColor = DarkGray,
        subheadContentColor = DarkGray,
        navigationContentColor = DarkGray,
        yearContentColor = Black,
        currentYearContentColor = Black,
        selectedYearContentColor = Black,
        selectedYearContainerColor = AccentYellow,
        dayContentColor = DarkGray,
        selectedDayContentColor = Black,
        selectedDayContainerColor = AccentYellow,
        todayContentColor = Black,
        todayDateBorderColor = Black,
        dividerColor = Color.Transparent,
        dateTextFieldColors = TextFieldDefaults.colors(
            focusedIndicatorColor = indicatorColor,
            unfocusedIndicatorColor = indicatorColor,
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            focusedTextColor = Black,
            unfocusedTextColor = LightGray,
            focusedLabelColor = Black,
            unfocusedLabelColor = DarkGray,
            errorContainerColor = White,
            errorLabelColor = Color.Red,
            errorIndicatorColor = Color.Red,
            errorSupportingTextColor = Color.Red,
        ),
    )
    MaterialDatePickerDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        colors = pickerColors,

        confirmButton = {
            TextButton(
                onClick = onClick@{
                    val millis = pickerState.selectedDateMillis ?: run {
                        onDismiss()
                        return@onClick
                    }
                    onComplete(millis)
                }
            ) {
                Text(
                    text = stringResource(R.string.button_select),
                    color = Black,
                )
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.button_cancel),
                    color = Black,
                )
            }
        },
    ) {
        DatePicker(
            state = pickerState,
            colors = pickerColors,
        )
    }
}

@Preview
@Composable
private fun PreviewConfirmation() {
    TaskbenchTheme {
        var visible by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()
        if (visible) {
            ConfirmationDialog(
                text = "Are you sure you want to exit?",
                onComplete = {
                    Log.d("Dialogs", "[Confirmation] onComplete: $it")
                    visible = false
                    scope.launch {
                        delay(500)
                        visible = true
                    }
                },
                ignoreIndirectDismiss = true,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewTimePicker() {
    TaskbenchTheme {
        var visible by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()
        if (visible) {
            TimePickerDialog(
                onComplete = { hour, minute ->
                    Log.d("Dialogs", "[TimePicker]: $hour:$minute")
                    visible = false
                    scope.launch {
                        delay(500)
                        visible = true
                    }
                },
                onDismiss = {
                    visible = false
                    scope.launch {
                        delay(500)
                        visible = true
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDatePicker() {
    TaskbenchTheme {
        var visible by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()
        if (visible) {
            DatePickerDialog(
                onComplete = { date ->
                    Log.d("Dialogs", "[DatePicker] date: $date")
                    visible = false
                    scope.launch {
                        delay(500)
                        visible = true
                    }
                },
                onDismiss = {
                    visible = false
                    scope.launch {
                        delay(500)
                        visible = true
                    }
                },
            )
        }
    }
}
