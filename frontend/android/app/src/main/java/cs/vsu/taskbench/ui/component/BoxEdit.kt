package cs.vsu.taskbench.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

private val DEFAULT_TEXT_STYLE = TextStyle(
    color = Black,
    fontSize = 16.sp,
)

@Composable
fun BoxEdit(
    value: String,
    onValueChange: (String) -> Unit,
    button: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    val focusRequester = remember { FocusRequester() }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { focusRequester.requestFocus() }
            .heightIn(min = 140.dp, max = 180.dp)
            .fillMaxWidth()
            .background(color = White, shape = RoundedCornerShape(10.dp))
            .padding(
                start = 16.dp,
                top = 16.dp,
                bottom = 8.dp,
                end = 8.dp,
            ),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = false,
            interactionSource = interactionSource,
            textStyle = DEFAULT_TEXT_STYLE,
            modifier = Modifier
                .align(alignment = Alignment.Top)
                .weight(1.0f)
                .padding(bottom = 8.dp)
                .focusRequester(focusRequester),
        )

        button()
    }
}

@Composable
@NonRestartableComposable
fun BoxEdit(
    value: String,
    onValueChange: (String) -> Unit,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    BoxEdit(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        interactionSource = interactionSource,
        button = {
            IconButton(
                onClick = {},
                modifier = Modifier.requiredSize(32.dp),
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
    )
}

@Composable
@Preview
private fun Preview() {
    TaskbenchTheme {
        var value by remember { mutableStateOf("") }
        BoxEdit(
            value = value,
            onValueChange = { value = it },
            iconRes = R.drawable.ic_plus_circle_filled,
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
            iconRes = R.drawable.ic_plus_circle_filled,
        )
    }
}
