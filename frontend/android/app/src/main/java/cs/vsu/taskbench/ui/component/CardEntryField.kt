package cs.vsu.taskbench.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

@Composable
fun CardEntryField(
    inputText: String,
    visibility: Boolean,
){
    

}

@Composable
@Preview
private fun PreviewWithText(){
    TaskbenchTheme {
        CardEntryField(
            "В три часа дня сегодня созвон с командой |",
            true
        )
    }
}

@Composable
@Preview
private fun PreviewNoText(){
    TaskbenchTheme {
        CardEntryField(
            "",
            false
        )
    }
}
