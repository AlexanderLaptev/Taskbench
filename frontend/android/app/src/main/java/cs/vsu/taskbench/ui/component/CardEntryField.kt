package cs.vsu.taskbench.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

@Composable
fun CardEntryField(
    inputText: String,
    visibility: Boolean,
    modifier: Modifier = Modifier,
){
    Box(
        modifier = modifier
            .background(
                color = White,
                shape = RoundedCornerShape(4.dp),
            )
            .size(height = 171.dp, width = 404.dp),
    ) {
        if (visibility) {
            Box(
                modifier = Modifier
                    .padding(25.dp)
                    .fillMaxSize()
                    .padding(bottom = 40.dp)
            ) {
                val scrollState = rememberScrollState()
                Text(
                    text = inputText,
                    color = Black,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                )
            }

            IconButton(
                color = AccentYellow,
                onClick = { Log.d(null, "lorem ipsum clicked") },
                iconResId = R.drawable.icon_save,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            )
        }
        else {
            Text(
                text = "Введите вашу идею",
                color = LightGray,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(25.dp),
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
            true
        )
    }
}

@Composable
@Preview
private fun PreviewNoText(){
    TaskbenchTheme {
        CardEntryField(
            "",
            false
        )
    }
}
