package cs.vsu.taskbench.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun TaskbenchTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = lightColorScheme(
        background = Beige,
        surface = Beige,
        onSurface = Black,
        outlineVariant = LightGray,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
