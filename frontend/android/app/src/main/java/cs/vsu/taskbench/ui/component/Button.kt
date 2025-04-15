package cs.vsu.taskbench.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = White,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    val style = TextStyle(fontSize = 16.sp).merge(textStyle)
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(color, RoundedCornerShape(10.dp))
            .padding(16.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = style,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
@Preview
private fun Preview() {
    TaskbenchTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                "Lorem ipsum",
                onClick = { Log.d(null, "lorem ipsum clicked") }
            )
            Button(
                "Lorem ipsum",
                color = AccentYellow,
                onClick = { Log.d(null, "lorem ipsum clicked") }
            )
            Button(
                "Lorem ipsum",
                color = AccentYellow,
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    color = Black,
                    fontWeight = FontWeight.Bold
                ),
                onClick = { Log.d(null, "lorem ipsum clicked") }
            )
        }
    }
}
