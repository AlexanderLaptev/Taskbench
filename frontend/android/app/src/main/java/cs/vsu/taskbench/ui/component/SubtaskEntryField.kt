package cs.vsu.taskbench.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White


@Composable
fun SubtaskEntryField(
    inputText: String,
    saved: Boolean,
    modifier: Modifier = Modifier,
){
    if (saved) {
        Row(
            modifier = modifier
                .background(
                    color = AccentYellow,
                    shape = RoundedCornerShape(4.dp),
                )
                .size(height = 80.dp, width = 404.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = inputText,
                maxLines = 1,
                color = Black,
                fontSize = 20.sp,
                modifier = modifier
                    .width(300.dp)
            )

            IconButton(
                color = Color.Transparent,
                onClick = { Log.d(null, "lorem ipsum clicked") },
                iconResId = R.drawable.icon_save,
            )
        }
    }
    else {
        Row(
            modifier = modifier
                .background(
                    color = White,
                    shape = RoundedCornerShape(4.dp),
                )
                .size(height = 80.dp, width = 404.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = inputText,
                maxLines = 1,
                color = Black,
                fontSize = 20.sp,
                modifier = modifier
                    .width(300.dp)
            )

            IconButton(
                color = Color.Transparent,
                onClick = { Log.d(null, "lorem ipsum clicked") },
                iconResId = R.drawable.icon_add,
            )
        }
    }

}

@Composable
@Preview
private fun PreviewWithText(){
    TaskbenchTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SubtaskEntryField(
            "Пройтись по темам созвона|",
            true
            )
            SubtaskEntryField(
                "Подготовить текст",
                false
            )
        }
    }
}
