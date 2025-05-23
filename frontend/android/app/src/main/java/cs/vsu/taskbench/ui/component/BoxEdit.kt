package cs.vsu.taskbench.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

private val textStyle = TextStyle(
    color = Black,
    fontSize = 16.sp,
)

private val placeholderTextStyle = TextStyle(
    color = LightGray,
    fontSize = 16.sp,
)

@Composable
fun BoxEdit(
    value: String,
    onValueChange: (String) -> Unit,
    buttonIcon: Painter,
    inactiveButtonIcon: Painter,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
) {
    val isEnabled = value.isNotEmpty()
    val iconTint = if (isEnabled) null else ColorFilter.tint(LightGray)
    val currentIcon = if (isEnabled) buttonIcon else inactiveButtonIcon

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
            .heightIn(min = 90.dp, max = 180.dp)
            .fillMaxWidth()
            .background(color = White, shape = RoundedCornerShape(10.dp))
            .padding(
                start = 16.dp,
                top = 16.dp,
                bottom = 4.dp,
                end = 4.dp,
            ),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = false,
            interactionSource = interactionSource,
            textStyle = textStyle,
            modifier = Modifier
                .align(alignment = Alignment.Top)
                .weight(1.0f)
                .focusRequester(focusRequester),

            decorationBox = { field ->
                field()
                if (value.isEmpty()) {
                    BasicText(
                        text = placeholder,
                        style = placeholderTextStyle,
                    )
                }
            }
        )

        IconButton(
            onClick = onClick,
            enabled = isEnabled,
        ) {
            Image(
                painter = currentIcon,
                contentDescription = null,
                colorFilter = iconTint,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    TaskbenchTheme {
        var value by remember { mutableStateOf("") }
        BoxEdit(
            value = value,
            onValueChange = { value = it },
            buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
            inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
            placeholder = "Enter text",
            onClick = {}
        )
    }
}

@Composable
@Preview
private fun PreviewFilled() {
    TaskbenchTheme {
        var value by remember { mutableStateOf(LoremIpsum(80).values.first()) }
        BoxEdit(
            value = value,
            onValueChange = { value = it },
            buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
            inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
            onClick = {}
        )
    }
}
