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
    onAddButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selection by remember { mutableStateOf(TextRange.Zero) }
    var value by remember(text) { mutableStateOf(TextFieldValue(text, selection)) }

    SubtaskTemplate(
        value = value,
        onValueChange = {
            value = it
            onTextChange(it.text)
            selection = it.selection
        },
        placeholder = placeholder,
        color = White,
        editable = true,
        mainActionIcon = R.drawable.ic_add_circle_outline,
        showEditAction = false,
        onMainAction = onAddButtonClick,
        onEdit = {},
        modifier = modifier,
    )
}

@Composable
fun AddedSubtask(
    text: String,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selection by remember { mutableStateOf(TextRange.Zero) }
    var value by remember(text) { mutableStateOf(TextFieldValue(text, selection)) }
    var isEditing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    SubtaskTemplate(
        value = value,
        onValueChange = {
            value = it
            onTextChange(it.text)
            selection = it.selection
        },
        placeholder = "",
        color = AccentYellow,
        editable = isEditing,
        mainActionIcon = if (isEditing) {
            R.drawable.ic_ok_circle_outline
        } else R.drawable.ic_remove_circle_outline,
        showEditAction = !isEditing,
        onEdit = {
            isEditing = true
            value = TextFieldValue(value.text, TextRange(value.text.length))
            focusRequester.requestFocus()
        },
        focusRequester = focusRequester,
        modifier = modifier,

        onMainAction = {
            if (isEditing) {
                focusRequester.freeFocus()
                isEditing = false
            } else onRemove()
        },
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
        showEditAction = false,
        onMainAction = onAdd,
        onEdit = {},
        modifier = modifier,
    )
}

private val shape = RoundedCornerShape(10.dp)
private val textStyle = TextStyle(fontSize = 16.sp)
private val textFieldModifier = Modifier.fillMaxWidth()

//TODO: make the ripple circular and not square

@Composable
private fun SubtaskTemplate(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    color: Color,
    editable: Boolean,
    @DrawableRes mainActionIcon: Int,
    showEditAction: Boolean,
    onMainAction: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val isEnabled = value.text.isNotEmpty()
    val iconTint = if (isEnabled) Color.Unspecified else Color.LightGray

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

        if (showEditAction) {
            IconButton(
                onClick = onEdit,
                enabled = isEnabled,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }
        }

        IconButton(
            onClick = onMainAction,
            enabled = isEnabled,
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
                onAddButtonClick = { text1 = "" },
            )

            AddedSubtask(
                text = text2,
                onTextChange = { text2 = it },
                onRemove = { text2 = "" },
            )

            SuggestedSubtask(
                text = "Lorem ipsum dolor sit amet",
                onAdd = {},
            )
        }
    }
}
