package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.LightYellow
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

private val textStyle = TextStyle(color = Black, fontSize = 16.sp)

private val placeholderTextStyle = TextStyle(
    color = LightGray,
    fontSize = 16.sp,
)

private val shape = RoundedCornerShape(10.dp)

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LightYellow,
    placeholder: String = "",
    placeholderStyle: TextStyle = placeholderTextStyle,
    readOnly: Boolean = false,
    password: Boolean = false,
    keyboardOptions: KeyboardOptions? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val focusRequester = remember { FocusRequester() }
    var passwordVisible by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { focusRequester.requestFocus() }
            .background(color, shape)
            .padding(start = 16.dp)
            .defaultMinSize(minHeight = 52.dp)
            .fillMaxWidth(),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            textStyle = textStyle,
            readOnly = readOnly,
            singleLine = true,

            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1.0f),

            keyboardOptions = keyboardOptions ?: KeyboardOptions(
                keyboardType = if (password) KeyboardType.Password else KeyboardType.Text
            ),

            visualTransformation = if (password && !passwordVisible) {
                PasswordVisualTransformation()
            } else VisualTransformation.None,

            decorationBox = { innerTextField ->
                innerTextField()
                if (value.isEmpty()) {
                    BasicText(
                        text = placeholder,
                        style = placeholderStyle,
                    )
                }
            }
        )

        if (password) {
            IconButton(
                onClick = { passwordVisible = !passwordVisible },
            ) {
                Icon(
                    painter = painterResource(
                        if (passwordVisible) {
                            R.drawable.ic_eye_closed
                        } else R.drawable.ic_eye_open
                    ),
                    contentDescription = null,
                )
            }
        } else Spacer(Modifier.width(16.dp))
    }
}

@Preview
@Composable
private fun PreviewNormal() {
    var inputText by remember { mutableStateOf("") }
    TaskbenchTheme {
        TextField(
            inputText,
            { inputText = it },
            placeholder = "Hint text",
        )
    }
}

@Preview
@Composable
private fun PreviewPassword() {
    var inputText by remember { mutableStateOf("") }
    TaskbenchTheme {
        TextField(
            inputText,
            { inputText = it },
            placeholder = "Password",
            password = true,
        )
    }
}
