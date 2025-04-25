package cs.vsu.taskbench.ui.create

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cs.vsu.taskbench.data.task.SuggestionRepository
import cs.vsu.taskbench.domain.model.Subtask
import org.koin.compose.koinInject

class TaskCreationScreenViewModel(

) : ViewModel()  {

    enum class ErrorType(@StringRes val messageId: Int) {
    }

    var task by mutableStateOf("")
    var newSubtask by mutableStateOf("")
    var priority by mutableStateOf("")
    var deadline by mutableStateOf("")
    var category by mutableStateOf("")
    var subtasks: List<Subtask> by mutableStateOf(emptyList())
//    var suggestions: List<Subtask> by mutableStateOf(emptyList())
    var suggestions: List<String> by mutableStateOf(emptyList())

    fun updateSuggestions(newSuggestions: List<String>) {
        suggestions = newSuggestions
    }


    fun addSubtask(){

    }

    fun deleteSubtask(){

    }

    fun saveTask(){

    }

    fun createSubtask(){

    }




}