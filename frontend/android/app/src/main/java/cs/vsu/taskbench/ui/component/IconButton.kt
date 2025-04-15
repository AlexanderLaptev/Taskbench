package cs.vsu.taskbench.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White


@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = White,
    iconResId: Int,
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(color, RoundedCornerShape(30.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = "",
            modifier = Modifier.size(40.dp),
            tint = Color.Unspecified,
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
            IconButton(
                onClick = { Log.d(null, "lorem ipsum clicked") },
                iconResId = R.drawable.icon_clock,
            )
            IconButton(
                color = AccentYellow,
                onClick = { Log.d(null, "lorem ipsum clicked") },
                iconResId = R.drawable.icon_save,
            )
            IconButton(
                color = AccentYellow,
                onClick = { Log.d(null, "lorem ipsum clicked") },
                iconResId = R.drawable.icon_edit,
            )
            IconButton(
                color = AccentYellow,
                onClick = { Log.d(null, "lorem ipsum clicked") },
                iconResId = R.drawable.icon_settings,
            )
            IconButton(
                color = AccentYellow,
                onClick = { Log.d(null, "lorem ipsum clicked") },
                iconResId = R.drawable.icon_add,
            )
            IconButton(
                color = Color.Transparent,
                onClick = { Log.d(null, "lorem ipsum clicked") },
                iconResId = R.drawable.icon_back,
            )
        }
    }
}