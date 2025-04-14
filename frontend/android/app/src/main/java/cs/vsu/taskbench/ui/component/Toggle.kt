package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

@Composable
fun Toggle(
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    val color = if (active) AccentYellow else Beige
    Box(
        modifier = modifier
            .clickable { onActiveChange(!active) }
            .background(color, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
        )
    }
}

@Composable
@Preview
private fun Preview() {
    TaskbenchTheme {
        var active by remember { mutableStateOf(false) }
        val text = if (active) "active text" else "inactive text"
        Toggle(active, { active = it }, text)
    }
}
