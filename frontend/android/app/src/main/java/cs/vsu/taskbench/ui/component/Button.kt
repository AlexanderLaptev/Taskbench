package cs.vsu.taskbench.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White
import androidx.compose.material3.Button as MaterialButton

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = White,
    fillWidth: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    MaterialButton(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Black,
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .let { if (fillWidth) it.fillMaxWidth() else it },
        content = content,
    )
}

@Composable
fun Button(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    color: Color = White,
    textStyle: TextStyle = TextStyle(color = Black, fontSize = 20.sp),
    fillWidth: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        color = color,
        fillWidth = fillWidth,
    ) {
        Text(
            text = text,
            style = textStyle,
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
                text = "Lorem ipsum",
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
