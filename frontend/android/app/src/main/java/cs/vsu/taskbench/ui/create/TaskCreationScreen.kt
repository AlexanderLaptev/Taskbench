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
import cs.vsu.taskbench.ui.component.Subtask
import cs.vsu.taskbench.ui.theme.Beige
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.Chip
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

@Destination<RootGraph>(style = ScreenTransitions::class)
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
        ) {
            Subtask(
                text = "Пройтись по темам созвона",
                selected = true,
                buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
                onButtonClick = {},
            )
            Subtask(
                text = "Пройтись по темам созвона",
                selected = true,
                buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
                onButtonClick = {},
            )
            Subtask(
                text = "Пройтись по темам созвона",
                selected = false,
                buttonIcon = painterResource(R.drawable.ic_add_circle_outline),
                onButtonClick = {},
            )
            Subtask(
                text = "Пройтись по темам созвона",
                selected = false,
                buttonIcon = painterResource(R.drawable.ic_add_circle_outline),
                onButtonClick = {},
            )
            Subtask(
                text = "Пройтись по темам созвона",
                selected = true,
                buttonIcon = painterResource(R.drawable.ic_ok_circle_filled),
                onButtonClick = {},
            )
            Subtask(
                text = "Пройтись по темам созвона",
                selected = true,
                buttonIcon = painterResource(R.drawable.ic_ok_circle_filled),
                onButtonClick = {},
            )
        }

        Icon(
            painter = painterResource(R.drawable.logo_full_dark),
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
//            Row(
//                modifier = Modifier
//                    .horizontalScroll(rememberScrollState()),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Button(
//                    "Выбрать время",
//                    onClick = { Log.d(null, "lorem ipsum clicked") }
//                )
//                Button(
//                    "Категория",
//                    onClick = { Log.d(null, "lorem ipsum clicked") }
//                )
//                Button(
//                    "Приоритет",
//                    onClick = { Log.d(null, "lorem ipsum clicked") }
//                )
//            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                Chip("tomorrow, 17:30", White, Black, {}, icon = painterResource(R.drawable.ic_clock))
                Chip("high priority", AccentYellow, Black, {})
                Chip("select category", White, LightGray, {})
            }


//            CardEntryField(
//                "",
//                "Введите вашу идею",
//                {}
//            )
        }
    }
}
//    navController: NavController,
//) {
//    Scaffold(
//        bottomBar = {
//            Column {
//                NavigationBar(navController)
//            }
//        }
//    ) { padding ->
//        // TODO!
//        Text(
//            text = "Task Creation Screen",
//            fontSize = 28.sp,
//            color = LightGray,
//            modifier = Modifier
//                .fillMaxSize()
//                .wrapContentSize()
//                .padding(padding),
//        )
//}}


@Preview
@Composable
private fun Preview() {
    TaskbenchTheme {
        TaskCreationScreen()
    }
}