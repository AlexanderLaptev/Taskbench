package cs.vsu.taskbench.ui.create

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TaskCreationScreenViewModel() {

    class TaskViewModel(
        private val repoTask: TaskRepository,
        private val repoSuggestions: SuggestionRepository,
        private val repoCategory: CategoryRepository,
    ) : ViewModel(){
        var content by mutableStateOf("")
        var newSubtask by mutableStateOf("")
        var priority by mutableStateOf("")
        var deadline by mutableStateOf("")
        var category by mutableStateOf("")
        val subtasks: List<Subtask> by mutableStateOf(emptyList())
        var suggestions: List<Subtask> by mutableStateOf(emptyList())

        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        fun createTask(context: Context){
            val task = Task(
                id = null,
                content = content,
                deadline = deadlineConvertToDateTime(deadline),
                isHighPriority = priority == context.getString(R.string.high_priority),
                subtasks = subtasks,
                categoryId = getCategoryId(category),
            )

            viewModelScope.launch {
                repoTask.saveTask(task)
            }
        }

        fun deadlineConvertToDateTime(deadline: String): LocalDateTime{
            return LocalDateTime.parse(deadline, dateTimeFormatter)
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
                suggestions = repoSuggestions.getSuggestions(it)
            }
        }
    }

    class SubTaskViewModel(
        private val repoTask: TaskRepository,
    ) : ViewModel(){
        var curentSubtask by mutableStateOf("")

        private val _subtaskFlow = MutableStateFlow<List<Subtask>>()
        val subtaskFlow = _subtaskFlow.asSharedFlow()

        fun addSubtask(){
            val subtask = Subtask(id = null, content = curentSubtask, isDone = false)
            subtasks += subtask
            _subtaskFlow.update { subtasks }
        }
    }

}