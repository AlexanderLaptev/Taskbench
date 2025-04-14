package cs.vsu.taskbench.ui.create

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

@Composable
fun TaskCreationScreen() {
    Icon(
        painter = painterResource(R.drawable.logo),
        contentDescription = "",
//       modifier = Modifier.size(32.dp),
        tint = Color.Unspecified,
    )
}

@Preview
@Composable
private fun Preview() {
    TaskbenchTheme {
        TaskCreationScreen()
    }
}