package cs.vsu.taskbench.ui.util

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue

@OptIn(ExperimentalMaterial3Api::class)
val SheetState.isEffectivelyVisible
    get() =
        isVisible || (targetValue != SheetValue.Hidden)
