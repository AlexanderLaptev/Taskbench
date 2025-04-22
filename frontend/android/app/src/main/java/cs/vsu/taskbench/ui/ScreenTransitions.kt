package cs.vsu.taskbench.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle
import androidx.compose.animation.AnimatedContentTransitionScope as AnimScope

object ScreenTransitions : DestinationStyle.Animated() {
    override val enterTransition: (AnimScope<NavBackStackEntry>.() -> EnterTransition?) = {
        fadeIn(animationSpec = tween(700))
    }

    override val exitTransition: (AnimScope<NavBackStackEntry>.() -> ExitTransition?) = {
        fadeOut(animationSpec = tween(700))
    }
}
