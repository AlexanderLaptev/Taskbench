package cs.vsu.taskbench.ui.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.ui.component.TextField
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.White

enum class CategoryDialogMode {
    Select,
    Filter,
}

interface CategoryDialogActions {
    fun onInputChange(input: String)
    fun onSelect(category: Category)
    fun onDismiss()
    fun onAdd() = Unit
    fun onDeselect() = Unit
    fun onSelectAll() = Unit
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetCategoryDialog(
    mode: CategoryDialogMode,
    categories: List<Category>,
    input: String,
    actions: CategoryDialogActions,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = White,
        onDismissRequest = actions::onDismiss,
        modifier = modifier,
    ) {
        CategoryDialog(
            mode = mode,
            categories = categories,
            value = input,
            actions = actions,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
        )
    }
}

@Composable
fun CategoryDialog(
    mode: CategoryDialogMode,
    categories: List<Category>,
    value: String,
    actions: CategoryDialogActions,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            TextField(
                value = value,
                onValueChange = actions::onInputChange,
                placeholder = stringResource(R.string.placeholder_category),
                placeholderStyle = TextStyle(color = LightGray, fontSize = 16.sp),
                modifier = Modifier.weight(1.0f),
            )

            if (mode == CategoryDialogMode.Select) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .aspectRatio(1.0f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color = AccentYellow)
                        .clickable(onClick = actions::onAdd)
                        .fillMaxHeight(),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_circle_outline),
                        contentDescription = null,
                    )
                }
            }
        }

        LazyColumn {
            item {
                ListItem(
                    text = stringResource(R.string.label_no_category),
                    fontStyle = FontStyle.Italic,
                    onClick = actions::onDeselect,
                )
            }

            if (mode == CategoryDialogMode.Filter) {
                item {
                    ListItem(
                        text = stringResource(R.string.label_filter_category_all),
                        fontStyle = FontStyle.Italic,
                        onClick = actions::onSelectAll,
                    )
                }
            }

            itemsIndexed(categories, key = { _, it -> it.id!! }) { index, category ->
                ListItem(
                    text = category.name,
                    onClick = { actions.onSelect(category) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun ListItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontStyle: FontStyle? = null,
) {
    Text(
        text = text,
        fontStyle = fontStyle,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .height(42.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .fillMaxWidth(),
    )
}
