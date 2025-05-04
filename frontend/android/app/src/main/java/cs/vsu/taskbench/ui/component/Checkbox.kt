package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
        modifier = modifier,
        onClick = { onCheckedChange(!checked) },
        interactionSource = interactionSource,
    ) {
        val iconResId = if (checked) R.drawable.checkbox_on else R.drawable.checkbox_off
        Image(
            painter = painterResource(iconResId),
            contentDescription = null,
        )
    }
}

@Composable
@Preview
private fun Preview() {
    TaskbenchTheme {
        var checked by remember { mutableStateOf(false) }
        Checkbox(checked, { checked = it })
    }
}
