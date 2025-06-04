package cs.vsu.taskbench.ui.util

import androidx.compose.material3.SnackbarHostState

suspend fun SnackbarHostState.replaceMessage(message: String, allowDismiss: Boolean = true) {
    currentSnackbarData?.dismiss()
    showSnackbar(message, withDismissAction = allowDismiss)
}
