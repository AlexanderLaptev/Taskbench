package cs.vsu.taskbench

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import cs.vsu.taskbench.data.dataModule
import cs.vsu.taskbench.domain.usecase.useCaseModule
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.uiModule
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.compose.KoinApplication
import org.koin.core.logger.Level

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installAppMetrica()

        val style = SystemBarStyle.light(
            scrim = Color.TRANSPARENT,
            darkScrim = Color.TRANSPARENT,
        )
        enableEdgeToEdge(
            statusBarStyle = style,
            navigationBarStyle = style,
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // TODO: keep view models after navigation
        setContent {
            KoinApplication(
                application = {
                    androidLogger(Level.DEBUG)
                    androidContext(this@MainActivity)
                    modules(dataModule, useCaseModule, uiModule)
                }
            ) {
                val navController = rememberNavController()
                TaskbenchTheme {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        navController = navController,
                    )
                }
            }
        }
    }

    private fun installAppMetrica() {
        if (BuildConfig.DEBUG) return // do not send analytics from debug builds
        val config = AppMetricaConfig.newConfigBuilder(BuildConfig.APPMETRICA_API_KEY).build()
        AppMetrica.activate(applicationContext, config)
        AppMetrica.enableActivityAutoTracking(application)
    }
}
