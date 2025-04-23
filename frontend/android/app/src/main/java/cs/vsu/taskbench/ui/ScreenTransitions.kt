package cs.vsu.taskbench.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle
import androidx.compose.animation.AnimatedContentTransitionScope as AnimScope

object ScreenTransitions : DestinationStyle.Animated() {
    override val enterTransition: (AnimScope<NavBackStackEntry>.() -> EnterTransition?) = {
        fadeIn()
    }
}
