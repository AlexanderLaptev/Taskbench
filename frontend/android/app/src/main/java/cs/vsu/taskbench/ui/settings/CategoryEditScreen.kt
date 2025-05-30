package cs.vsu.taskbench.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor

@Composable
@Destination<SettingsGraph>(style = ScreenTransitions::class)
fun CategoryEditScreen(
    settingsNavigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        AnalyticsFacade.logScreen("CategoryEditScreen")
    }
    
    val categoryRepository = koinInject<CategoryRepository>()
    val scope = rememberCoroutineScope()
    
    // Состояние категорий
    val categories = remember { mutableStateListOf<Category>() }
    
    // Загрузка категорий при запуске
    LaunchedEffect(Unit) {
        categories.clear()
        categories.addAll(categoryRepository.getAllCategories())
    }
    
    Scaffold { scaffoldPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(16.dp)
        ) {
            // Кнопка возврата назад
            val buttonShape = RoundedCornerShape(100)
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = stringResource(R.string.button_back),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clip(buttonShape)
                    .clickable(onClick = { settingsNavigator.navigateUp() })
                    .background(color = White, shape = buttonShape)
                    .padding(4.dp)
                    .size(32.dp)
            )
            
            // Список категорий
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = categories,
                    key = { it.id ?: it.name }
                ) { category ->
                    val visibleState = remember { MutableTransitionState(true) }
                    
                    AnimatedVisibility(
                        visibleState = visibleState,
                        enter = fadeIn(),
                        exit = slideOutHorizontally() + fadeOut()
                    ) {
                        CategoryItem(
                            category = category,
                            onEdit = { newName ->
                                scope.launch {
                                    val updatedCategory = category.copy(name = newName)
                                    val result = categoryRepository.saveCategory(updatedCategory)
                                    
                                    // Обновляем категорию в списке
                                    val index = categories.indexOf(category)
                                    if (index != -1) {
                                        categories[index] = result
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    visibleState.targetState = false
                                    categoryRepository.deleteCategory(category)
                                    categories.remove(category)
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
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
    
    // Используем TextFieldValue вместо String для контроля позиции курсора
    var textFieldValue by remember(category) { 
        mutableStateOf(
            TextFieldValue(
                text = category.name,
                selection = TextRange(category.name.length) // Курсор в конце текста
            )
        ) 
    }
    
    val focusRequester = remember { FocusRequester() }
    
    // Запрашиваем фокус при активации режима редактирования
    LaunchedEffect(isEditing) {
        if (isEditing) {
            // Обновляем TextFieldValue с позицией курсора в конце
            textFieldValue = TextFieldValue(
                text = textFieldValue.text,
                selection = TextRange(textFieldValue.text.length)
            )
            focusRequester.requestFocus()
        }
    }
    
    // Создаем общий стиль текста для обеспечения идентичного отображения
    val textStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = Black,
        letterSpacing = 0.sp // Отключаем межбуквенное расстояние для одинакового отображения
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp) // Фиксированная высота плашки для постоянства размеров
            .clip(RoundedCornerShape(10.dp))
            .background(AccentYellow)
            .padding(horizontal = 16.dp)
    ) {
        // Область для текста - используем одинаковое пространство вне зависимости от состояния редактирования
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
                .padding(end = 8.dp)
        ) {
            if (isEditing) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    singleLine = true,
                    cursorBrush = SolidColor(Black),
                    textStyle = textStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            } else {
                Text(
                    text = displayedName,
                    style = textStyle
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditing) {
                // Кнопка отмены (крестик)
                Icon(
                    painter = painterResource(R.drawable.ic_remove_circle_outline),
                    contentDescription = null,
                    tint = DarkGray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            // Отменяем редактирование
                            textFieldValue = TextFieldValue(
                                text = displayedName,
                                selection = TextRange(displayedName.length)
                            )
                            isEditing = false
                        }
                )
                
                // Кнопка подтверждения (галочка) - меняет цвет в зависимости от того, были ли изменения
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
                                // Сразу обновляем отображаемое имя для мгновенной обратной связи
                                displayedName = currentText
                                isEditing = false
                                
                                // Затем асинхронно сохраняем в репозиторий
                                onEdit(currentText)
                            }
                        }
                )
            } else {
                // Кнопка редактирования
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
                
                // Кнопка удаления
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