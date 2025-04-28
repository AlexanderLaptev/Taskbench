package cs.vsu.taskbench.ui.create

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.R
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.AddedSubtask
import cs.vsu.taskbench.ui.component.BoxEdit
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.Chip
import cs.vsu.taskbench.ui.component.CreateSubtaskField
import cs.vsu.taskbench.ui.component.NavigationBar
import cs.vsu.taskbench.ui.component.SuggestedSubtask
import cs.vsu.taskbench.ui.component.TextField
import cs.vsu.taskbench.ui.create.TaskCreationScreenViewModel.Error
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.LightGray
import cs.vsu.taskbench.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ShowToast")
@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun TaskCreationScreen(navController: NavController) {
    val viewModel = koinViewModel<TaskCreationScreenViewModel>()
    val scope = rememberCoroutineScope()
    val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.updateCategories()

        var toast: Toast? = null
        viewModel.errorFlow.collect { error ->
            val messageId = when (error) {
                Error.BlankCategory -> R.string.error_blank_category
            }

            toast?.cancel()
            toast = Toast.makeText(
                context,
                context.getString(messageId),
                Toast.LENGTH_SHORT
            ).apply { show() }
        }
    }

    Scaffold(
        bottomBar = { NavigationBar(navController) },
    ) { padding ->
        var showCategoryDialog by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState()
        CategoryDialog(
            sheetState = sheetState,
            visible = showCategoryDialog,
            onVisibleChange = { showCategoryDialog = it },
            categories = viewModel.categorySearchResults,
            onCategoryAdd = { viewModel.addCategory(it) },
            onSearch = { viewModel.updateCategories(it) },

            onCategorySelect = {
                scope.launch {
                    sheetState.hide()
                    showCategoryDialog = false
                }
                viewModel.selectedCategory = it
            },
        )

        Box(
            Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                    bottom = if (imePadding > 0.dp) imePadding else padding.calculateBottomPadding()
                )
        ) {
            if (viewModel.subtasks.isEmpty() && viewModel.suggestedSubtasks.isEmpty()) {
                Icon(
                    painter = painterResource(R.drawable.logo_full_dark),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
            ) {
                CreateSubtaskField(
                    text = viewModel.subtaskInput,
                    onTextChange = { viewModel.subtaskInput = it },
                    placeholder = stringResource(R.string.label_subtask),
                    onAddButtonClick = viewModel::addSubtask,
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(top = 8.dp, bottom = 48.dp)
                ) {
                    // TODO: make keys unique
                    if (viewModel.subtasks.isNotEmpty()) {
                        item(key = 0) {
                            Text(
                                text = stringResource(R.string.list_subtasks),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGray,
                            )
                        }
                        items(viewModel.subtasks, key = { it.content }) { subtask ->
                            AddedSubtask(
                                text = subtask.content,
                                onTextChange = { /* TODO */ },
                                onRemove = { viewModel.removeSubtask(subtask) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    if (viewModel.suggestedSubtasks.isNotEmpty()) {
                        item(key = 1) {
                            Text(
                                text = stringResource(R.string.list_suggestions),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DarkGray,
                            )
                        }
                        items(viewModel.suggestedSubtasks, key = { it.content }) { suggestion ->
                            SuggestedSubtask(
                                text = suggestion.content,
                                onAdd = { viewModel.addSuggestion(suggestion) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        Chip(
                            text = if (viewModel.deadline != null) {
                                ""
                            } else stringResource(R.string.label_deadline), // TODO: format date

                            icon = painterResource(R.drawable.ic_clock),
                            textColor = if (viewModel.deadline != null) Black else LightGray,
                            color = White,
                            onClick = {},
                        )
                        Chip(
                            text = stringResource(
                                if (viewModel.isHighPriority) {
                                    R.string.priority_high
                                } else R.string.priority_low
                            ),

                            color = if (viewModel.isHighPriority) AccentYellow else White,
                            textColor = Black,
                            onClick = { viewModel.isHighPriority = !viewModel.isHighPriority },
                        )
                        Chip(
                            text = viewModel.selectedCategory?.name
                                ?: stringResource(R.string.label_category),
                            color = White,
                            textColor = if (viewModel.selectedCategory == null) LightGray else Black,
                            onClick = { showCategoryDialog = true },
                        )
                    }

                    BoxEdit(
                        value = viewModel.contentInput,
                        onValueChange = { viewModel.contentInput = it },
                        buttonIcon = painterResource(R.drawable.ic_add_circle_filled),
                        inactiveButtonIcon = painterResource(R.drawable.ic_add_circle_outline),
                        placeholder = stringResource(R.string.label_task),
                        onClick = viewModel::saveTask,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialog(
    sheetState: SheetState,
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    categories: List<Category>,
    onSearch: (String) -> Unit,
    onCategorySelect: (Category?) -> Unit,
    onCategoryAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (visible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { onVisibleChange(false) },
            containerColor = White,
            modifier = modifier,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .defaultMinSize(minHeight = 80.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
                ) {
                    var categoryInput by remember { mutableStateOf("") }
                    TextField(
                        value = categoryInput,
                        onValueChange = {
                            categoryInput = it
                            onSearch(it)
                        },
                        placeholder = "Category",
                        modifier = Modifier.weight(1.0f),
                    )
                    Button(
                        onClick = { onCategoryAdd(categoryInput) },
                        color = AccentYellow,
                        fillWidth = false,
                        modifier = Modifier.size(52.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_circle_outline),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.requiredSize(24.dp),
                        )
                    }
                }

                LazyColumn {
                    item {
                        Text(
                            text = "no category",
                            fontSize = 16.sp,
                            color = LightGray,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .animateItem()
                                .clickable { onCategorySelect(null) }
                                .padding(start = 16.dp)
                                .defaultMinSize(minHeight = 48.dp)
                                .wrapContentHeight()
                                .fillMaxWidth(),
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                    }

                    items(categories, key = { it.id!! }) { category ->
                        Text(
                            text = category.name,
                            fontSize = 16.sp,
                            color = Black,
                            modifier = Modifier
                                .animateItem()
                                .clickable { onCategorySelect(category) }
                                .padding(start = 16.dp)
                                .defaultMinSize(minHeight = 48.dp)
                                .wrapContentHeight()
                                .fillMaxWidth(),
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
    }
}
