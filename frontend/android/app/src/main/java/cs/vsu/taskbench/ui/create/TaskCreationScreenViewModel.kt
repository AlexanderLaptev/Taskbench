package cs.vsu.taskbench.ui.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.task.SuggestionRepository
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TaskCreationScreenViewModel(
    private val taskRepository: TaskRepository,
    private val suggestionRepository: SuggestionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    var newSubtask by mutableStateOf("")
    var content by mutableStateOf("")
    var priority by mutableStateOf("")
    var deadline by mutableStateOf("")
    var category by mutableStateOf("")

    private val _subtasks = MutableStateFlow<List<Subtask>>(emptyList())
    val subtasks = _subtasks.asStateFlow()

    private val _suggestions = MutableStateFlow<List<Subtask>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun saveSubtask() {
        val task = Task(
            id = null,
            content = content,
            deadline = deadlineToLocalDateTime(deadline),
            isHighPriority = priority == "Высокий приоритет",
            subtasks = _subtasks.value,
            categoryId = getCategoryId(category),
        )

        viewModelScope.launch {
            taskRepository.saveTask(task)
        }

        for (subtask in _subtasks.value) {
            // TODO: save subtasks
        }

        clearInput()
    }

    private fun clearInput() {
        newSubtask = ""
        content = ""
        priority = ""
        deadline = ""
        category = ""
        _subtasks.value = emptyList()
        _suggestions.value = emptyList()
    }

    fun addSubtask() {
        val subtask = Subtask(id = null, content = newSubtask, isDone = false)
        viewModelScope.launch {
            _subtasks.update { it + subtask }
        }
    }

    fun removeSubtask(subtask: Subtask) {
        viewModelScope.launch {
            _subtasks.update { it - subtask }
        }
    }

    fun addSuggestion(subtask: Subtask) {
        viewModelScope.launch {
            _subtasks.update { it + subtask }
            _suggestions.update { it - subtask }
        }
    }

    private fun deadlineToLocalDateTime(deadline: String): LocalDateTime {
        return LocalDateTime.now()
    }

    private fun deadlineToString(deadline: LocalDateTime): String {
        return deadline.format(dateTimeFormatter)
    }

    // TODO: review
    private fun getCategoryId(category: String): Int {
        return 0
    }

    fun updateSuggestions(it: String) {
        viewModelScope.launch {
            // TODO: add delay after last change before sending
            _suggestions.value = emptyList()
            val newSuggestions = suggestionRepository.getSuggestions(it)
            _suggestions.value = newSuggestions.map { content ->
                Subtask(id = null, content = content, isDone = false)
            }
        }
    }
}
