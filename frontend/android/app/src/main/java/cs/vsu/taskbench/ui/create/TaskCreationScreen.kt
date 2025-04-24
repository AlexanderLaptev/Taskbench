package cs.vsu.taskbench.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.data.task.SuggestionRepository
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.TextField
import cs.vsu.taskbench.ui.theme.DarkGray
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun TaskCreationScreen(
    navController: NavController,
) {
    Scaffold(
        bottomBar = {
            NavigationBar(navController)
        }
    ) { padding ->
        // TODO!
        val suggestionRepository = koinInject<SuggestionRepository>()
        var suggestions by remember { mutableStateOf(listOf<String>()) }
        val scope = rememberCoroutineScope()

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(16.dp)
                .padding(padding),
        ) {
            var prompt by remember { mutableStateOf("") }
            TextField(
                value = prompt,
                onValueChange = {
                    prompt = it
                    scope.launch { suggestions = suggestionRepository.getSuggestions(it) }
                },
                placeholder = "prompt",
            )

            for (suggestion in suggestions) {
                Text(
                    text = suggestion,
                    fontSize = 20.sp,
                    color = DarkGray,
                )
            }
        }
    }
}
