package cs.vsu.taskbench

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import cs.vsu.taskbench.ui.theme.TaskbenchNavGraph
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskbenchTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TaskbenchNavGraph(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
