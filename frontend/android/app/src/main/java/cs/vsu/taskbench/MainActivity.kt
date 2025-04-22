package cs.vsu.taskbench

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import cs.vsu.taskbench.data.dataModule
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.compose.KoinApplication
import org.koin.core.logger.Level

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoinApplication(
                application = {
                    androidLogger(Level.DEBUG)
                    androidContext(this@MainActivity)
                    modules(dataModule, uiModule)
                }
            ) {
                val navController = rememberNavController()
                TaskbenchTheme {
                    Scaffold(
                        content = { padding ->
                            DestinationsNavHost(
                                navGraph = NavGraphs.root,
                                navController = navController,
                                modifier = Modifier.padding(padding),
                            )
                        }
                    )
                }
            }
        }
    }
}
