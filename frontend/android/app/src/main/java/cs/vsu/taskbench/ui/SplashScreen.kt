package cs.vsu.taskbench.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

@Composable
fun SplashScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Beige),
    ) {
        Image(
            painter = painterResource(R.drawable.logo_full_dark),
            contentDescription = "",
            modifier = Modifier.offset(y = (-60).dp)
        )
    }
}

@Composable
@Preview
private fun Preview() {
    TaskbenchTheme {
        SplashScreen()
    }
}
