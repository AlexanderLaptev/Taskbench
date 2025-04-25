package cs.vsu.taskbench.ui.create

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cs.vsu.taskbench.domain.model.Subtask

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
    var suggestions: List<Subtask> by mutableStateOf(emptyList())


    fun addSubtask(){

    }

    fun deleteSubtask(){

    }

    fun saveTask(){

    }

    fun createSubtask(){

    }




}