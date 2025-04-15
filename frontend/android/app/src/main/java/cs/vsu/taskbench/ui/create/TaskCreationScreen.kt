package cs.vsu.taskbench.ui.create

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.CardEntryField
import cs.vsu.taskbench.ui.component.Subtask
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

@Composable
fun TaskCreationScreen(
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Beige)
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .height(263.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ){
            Subtask(
                text = "Пройтись по темам созвона",
                selected = true,
                buttonIcon = painterResource(R.drawable.icon_save),
                onButtonClick = {},
            )
            Subtask(
                text = "Пройтись по темам созвона",
                selected = true,
                buttonIcon = painterResource(R.drawable.icon_save),
                onButtonClick = {},
            )
            Subtask(
                text = "Пройтись по темам созвона",
                selected = false,
                buttonIcon = painterResource(R.drawable.icon_add),
                onButtonClick = {},
            )
            Subtask(
                text = "Пройтись по темам созвона",
                selected = false,
                buttonIcon = painterResource(R.drawable.icon_add),
                onButtonClick = {},
            )
            Subtask(
                text = "Пройтись по темам созвона",
                selected = true,
                buttonIcon = painterResource(R.drawable.icon_save),
                onButtonClick = {},
            )
            Subtask(
                text = "Пройтись по темам созвона",
                selected = true,
                buttonIcon = painterResource(R.drawable.icon_save),
                onButtonClick = {},
            )
        }

        Icon(
            painter = painterResource(R.drawable.logo),
            contentDescription = "",
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.Center)
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    "Выбрать время",
                    onClick = { Log.d(null, "lorem ipsum clicked") }
                )
                Button(
                    "Категория",
                    onClick = { Log.d(null, "lorem ipsum clicked") }
                )
                Button(
                    "Приоритет",
                    onClick = { Log.d(null, "lorem ipsum clicked") }
                )
            }
            CardEntryField(
                "В три часа дня сегодня созвон с командой |",
                true,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TaskbenchTheme {
        TaskCreationScreen()
    }
}