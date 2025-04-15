package cs.vsu.taskbench.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

@Composable
fun CardEntryField(
    inputText: String = "",
    placeholder: String = "",
    onValueChange: (String) -> Unit,
    interactionSource: MutableInteractionSource? = null,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier,
){
    val shouldShowPlaceholder = inputText.isEmpty()
    val color = if(shouldShowPlaceholder) LightGray else Black

    Box(
        modifier = modifier
            .background(
                color = White,
                shape = RoundedCornerShape(4.dp),
            )
            .height(171.dp)
            .fillMaxWidth()
    ) {

        BasicTextField(
            value = inputText,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            textStyle = TextStyle(color = Black, fontSize = 20.sp),
            readOnly = readOnly,

            decorationBox = { innerTextField ->
                Box(
                    modifier = modifier
                        .padding(24.dp)
                        .height(171.dp)
                        .fillMaxWidth(),
                ) {
                    innerTextField()
                    if (shouldShowPlaceholder) {
                        Text(
                            text = placeholder,
                            fontSize = 20.sp,
                            color = color,
                        )
                    }
                }
            }
        )
        if (!shouldShowPlaceholder) {
            IconButton(
                color = AccentYellow,
                onClick = { Log.d(null, "lorem ipsum clicked") },
                iconResId = R.drawable.icon_save,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }

}


@Composable
@Preview
private fun PreviewWithText(){
    TaskbenchTheme {
        CardEntryField(
            "В три часа дня сегодня созвон с командой |",
            "Введите вашу идею",
            {}
        )
    }
}

@Composable
@Preview
private fun PreviewNoText(){
    var text by remember { mutableStateOf("") }
    TaskbenchTheme {
        CardEntryField(
            text,
            "Введите вашу идею",
            { text = it }
        )
    }
}
