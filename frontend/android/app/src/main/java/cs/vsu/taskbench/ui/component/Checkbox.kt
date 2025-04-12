package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    IconButton(
        modifier = modifier.size(48.dp),
        onClick = { onCheckedChange(!checked) },
        interactionSource = interactionSource,
    ) {
        val iconResId = if (checked) R.drawable.checkbox_on else R.drawable.checkbox_off
        Icon(painterResource(iconResId), "", tint = Color.Unspecified)
    }
}

@Composable
@Preview
private fun PreviewCheckbox() {
    TaskbenchTheme {
        Row {
            Checkbox(false, {})
            Checkbox(true, {})
        }
    }
}

@Composable
@Preview
private fun PreviewCheckboxInteractive() {
    TaskbenchTheme {
        var checked by remember { mutableStateOf(false) }
        Checkbox(checked, { checked = it })
    }
}
