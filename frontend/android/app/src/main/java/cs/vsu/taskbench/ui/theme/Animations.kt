package cs.vsu.taskbench.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith

fun swapTransition(): AnimatedContentTransitionScope<Any>.() -> ContentTransform =
    { scaleIn().togetherWith(scaleOut()) }
