package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.LightYellow
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

private val textStyle = TextStyle(color = Black, fontSize = 16.sp)

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LightYellow,
    placeholder: String = "",
    readOnly: Boolean = false,
    password: Boolean = false,
    keyboardOptions: KeyboardOptions? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val shouldShowPlaceholder = value.isEmpty()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        textStyle = textStyle,
        readOnly = readOnly,
        singleLine = true,
        keyboardOptions = keyboardOptions ?: KeyboardOptions(
            keyboardType = if (password) KeyboardType.Password else KeyboardType.Text
        ),

        visualTransformation = if (password) {
            PasswordVisualTransformation()
        } else VisualTransformation.None,

        decorationBox = { innerTextField ->
            Box(
                modifier = modifier
                    .background(color, RoundedCornerShape(10.dp))
                    .padding(start = 16.dp)
                    .height(52.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart,
            ) {
                innerTextField()
                if (shouldShowPlaceholder) {
                    Text(
                        text = placeholder,
                        fontSize = 16.sp,
                        color = LightGray,
                    )
                }
            }
        }
    )
}

@Composable
@Preview
private fun Preview() {
    var inputText by remember { mutableStateOf("") }
    TaskbenchTheme {
        TextField(
            inputText,
            { inputText = it },
            placeholder = "Hint text"
        )
    }
}
