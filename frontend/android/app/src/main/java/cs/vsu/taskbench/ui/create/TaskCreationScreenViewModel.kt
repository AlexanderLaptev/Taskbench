package cs.vsu.taskbench.ui.create

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.task.SuggestionRepository
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.domain.model.Subtask
import cs.vsu.taskbench.domain.model.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TaskCreationScreenViewModel(
    private val taskRepository: TaskRepository,
    private val suggestionRepository: SuggestionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel(){

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

    fun createTask(){
        val task = Task(
            id = null,
            content = content,
            deadline = deadlineConvertToDateTime(deadline),
//            isHighPriority = priority == context.getString(R.string.priority_high), надо передавать Context
            isHighPriority = priority == "Высокий приоритет",
            subtasks = _subtasks.value,
            categoryId = getCategoryId(category),
        )
        viewModelScope.launch {
            taskRepository.saveTask(task)
        }
        for (subtask in _subtasks.value){
            //сохранение подтасков
        }

        newTask()
    }

    fun newTask() {
        newSubtask = ""
        content = ""
        priority = ""
        deadline = ""
        category = ""
        _subtasks.value = emptyList()
        _suggestions.value = emptyList()
    }

    fun createSubtask(){
        val subtask = Subtask(id = null, content = newSubtask, isDone = false)
        viewModelScope.launch {
            _subtasks.update { it + subtask }
        }
    }

    fun removeSubtask(subtask: Subtask){
        viewModelScope.launch {
            _subtasks.update { it - subtask }
        }
    }

    fun addSubtask(subtask: Subtask){
        viewModelScope.launch {
            _subtasks.update {  it + subtask }
            _suggestions.update { it - subtask }
        }
    }

    fun deadlineConvertToDateTime(deadline: String): LocalDateTime{
//        return LocalDateTime.parse(deadline, dateTimeFormatter)
        return LocalDateTime.now()
    }

    fun deadlineConvertToString(deadline: LocalDateTime): String{
        return deadline.format(dateTimeFormatter)
    }

    fun getCategoryId(category: String): Int{
        //find in repo
        return 0
    }

    fun updateSuggestions(it: String) {
        viewModelScope.launch {
            delay(300)
            _suggestions.value = emptyList()

            val newSuggestions = suggestionRepository.getSuggestions(it)

            _suggestions.value = newSuggestions.map { content ->
                Subtask(id = null, content = content, isDone = false)
            }
        }
    }

}