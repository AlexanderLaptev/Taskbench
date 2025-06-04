package cs.vsu.taskbench.ui.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.analytics.AnalyticsFacade
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val TAG = "CategoryEditScreen"

@OptIn(DelicateCoroutinesApi::class)
@Composable
@Destination<SettingsGraph>(style = ScreenTransitions::class)
fun CategoryEditScreen(
    settingsNavigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val categoryRepository = koinInject<CategoryRepository>()
    val categories = remember { mutableStateListOf<Category>() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AnalyticsFacade.logScreen("CategoryEditScreen")
        scope.launch {
            categories.clear()
            categories.addAll(categoryRepository.getAllCategories())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = stringResource(R.string.button_back),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clip(RoundedCornerShape(100))
                    .clickable {
                        GlobalScope.launch {
                            try {
                                categoryRepository.preload()
                            } catch (e: Exception) {
                                AnalyticsFacade.logError("unknown", e)
                                Log.e(TAG, "update category error", e)
                            }
                        }
                        settingsNavigator.navigateUp()
                    }
                    .padding(8.dp)
                    .size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(
                items = categories,
                key = { it.id!! }
            ) { category ->
                CategoryItem(
                    category = category,
                    modifier = Modifier.animateItem(),

                    onEdit = { newName ->
                        scope.launch {
                            Log.d(TAG, "updating category $category -> $newName")
                            val updatedCategory = category.copy(name = newName)

                            try {
                                val result = categoryRepository.saveCategory(updatedCategory)
                                Log.d(TAG, "saved category: $result")

                                val index = categories.indexOf(category)
                                if (index != -1) {
                                    categories[index] = result
                                }
                            } catch (e: Exception) {
                                AnalyticsFacade.logError("unknown", e)
                                Log.e(TAG, "update category error", e)
                                Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    },

                    onDelete = {
                        scope.launch {
                            try {
                                Log.d(TAG, "deleting category $category")
                                categoryRepository.deleteCategory(category)
                                categories.remove(category)
                            } catch (e: Exception) {
                                AnalyticsFacade.logError("unknown", e)
                                Log.e(TAG, "update category error", e)
                                Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditing by remember { mutableStateOf(false) }
    var displayedName by remember(category) { mutableStateOf(category.name) }

    var textFieldValue by remember(category) {
        mutableStateOf(
            TextFieldValue(
                text = category.name,
                selection = TextRange(category.name.length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            textFieldValue = TextFieldValue(
                text = textFieldValue.text,
                selection = TextRange(textFieldValue.text.length)
            )
            focusRequester.requestFocus()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AccentYellow)
            .padding(horizontal = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            if (isEditing) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    singleLine = true,
                    cursorBrush = SolidColor(Black),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .focusRequester(focusRequester)
                )
            } else {
                Text(
                    text = displayedName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditing) {
                Icon(
                    painter = painterResource(R.drawable.ic_remove_circle_outline),
                    contentDescription = null,
                    tint = DarkGray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            textFieldValue = TextFieldValue(
                                text = displayedName,
                                selection = TextRange(displayedName.length)
                            )
                            isEditing = false
                        }
                )

                val currentText = textFieldValue.text
                val isChanged = currentText != displayedName && currentText.isNotBlank()
                Icon(
                    painter = painterResource(R.drawable.ic_ok_circle_outline),
                    contentDescription = null,
                    tint = if (isChanged) Black else LightGray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(enabled = isChanged) {
                            if (isChanged) {
                                displayedName = currentText
                                isEditing = false
                                onEdit(currentText)
                            }
                        }
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = null,
                    tint = DarkGray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            isEditing = true
                        }
                )

                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = null,
                    tint = DarkGray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}
