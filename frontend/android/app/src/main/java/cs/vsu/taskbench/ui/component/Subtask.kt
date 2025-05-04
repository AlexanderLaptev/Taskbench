package cs.vsu.taskbench.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White

@Composable
@NonRestartableComposable
fun SubtaskCreationField(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    onAdd: () -> Unit,
    canAdd: (String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    var selection by remember { mutableStateOf(TextRange(text.length)) }
    var value by remember(text) { mutableStateOf(TextFieldValue(text, selection)) }

    SubtaskTemplate(
        placeholder = placeholder,
        color = White,
        editable = true,
        modifier = modifier,

        value = value,
        onValueChange = {
            value = it
            selection = it.selection
            onTextChange(it.text)
        },

        mainActionEnabled = canAdd(value.text),
        mainActionIcon = R.drawable.ic_add_circle_outline,
        onMainAction = onAdd,
    )
}

@Composable
fun AddedSubtask(
    text: String,
    onEditConfirm: (String) -> Unit,
    onRemove: () -> Unit,
    canEdit: (String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    var value by remember(text) { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }
    var oldText by remember { mutableStateOf(text) }
    var isEditing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    SubtaskTemplate(
        color = AccentYellow,
        placeholder = "",
        editable = isEditing,

        value = value,
        onValueChange = {
            value = it
        },

        mainActionEnabled = !isEditing || canEdit(value.text),
        mainActionIcon = if (isEditing) {
            R.drawable.ic_ok_circle_outline
        } else R.drawable.ic_delete,
        onMainAction = {
            if (isEditing) {
                value = value.copy(selection = TextRange.Zero)
                focusRequester.freeFocus()
                isEditing = false
                onEditConfirm(value.text)
            } else onRemove()
        },

        showSecondaryAction = true,
        secondaryActionIcon = if (isEditing) {
            R.drawable.ic_remove_circle_outline
        } else R.drawable.ic_edit,
        onSecondaryAction = {
            if (isEditing) {
                value = TextFieldValue(oldText, TextRange.Zero)
                focusRequester.freeFocus()
            } else {
                oldText = text
                value = value.copy(selection = TextRange(value.text.length))
                focusRequester.requestFocus()
            }
            isEditing = !isEditing
        },

        focusRequester = focusRequester,
        modifier = modifier,
    )
}

@Composable
@NonRestartableComposable
fun SuggestedSubtask(
    text: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SubtaskTemplate(
        value = TextFieldValue(text),
        onValueChange = {},
        placeholder = "",
        color = White,
        editable = false,
        mainActionIcon = R.drawable.ic_add_circle_outline,
        onMainAction = onAdd,
        onSecondaryAction = {},
        modifier = modifier,
    )
}

private val shape = RoundedCornerShape(10.dp)
private val textStyle = TextStyle(fontSize = 16.sp)
private val textFieldModifier = Modifier.fillMaxWidth()

@Composable
private fun SubtaskTemplate(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    color: Color,
    editable: Boolean,
    @DrawableRes mainActionIcon: Int,
    onMainAction: () -> Unit,
    modifier: Modifier = Modifier,
    showSecondaryAction: Boolean = false,
    onSecondaryAction: () -> Unit = {},
    @DrawableRes secondaryActionIcon: Int = R.drawable.ic_edit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    mainActionEnabled: Boolean = true,
) {
    val enabled = value.text.isNotEmpty() && mainActionEnabled
    val iconTint = if (enabled) Color.Unspecified else Color.LightGray

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { focusRequester.requestFocus() }
            .background(color, shape)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .heightIn(min = 32.dp, 64.dp)
            .fillMaxWidth()
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = !editable,
            textStyle = textStyle,

            modifier = Modifier
                .weight(1.0f)
                .focusRequester(focusRequester),

            decorationBox = { field ->
                if (value.text.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = LightGray,
                    )
                }
                field()
            }
        )

        Spacer(Modifier.width(8.dp))

        if (showSecondaryAction) {
            IconButton(
                onClick = onSecondaryAction,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(secondaryActionIcon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }
        }

        IconButton(
            onClick = onMainAction,
            enabled = enabled,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painter = painterResource(mainActionIcon),
                contentDescription = null,
                tint = iconTint,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("Lorem ipsum dolor sit amet") }

    TaskbenchTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SubtaskCreationField(
                text = text1,
                onTextChange = { text1 = it },
                placeholder = "Enter subtask",
                onAdd = {},
                canAdd = { true },
            )

            AddedSubtask(
                text = text2,
                onRemove = { text2 = "" },
                onEditConfirm = {},
                canEdit = { true },
            )

            SuggestedSubtask(
                text = "Lorem ipsum dolor sit amet",
                onAdd = {},
            )
        }
    }
}
