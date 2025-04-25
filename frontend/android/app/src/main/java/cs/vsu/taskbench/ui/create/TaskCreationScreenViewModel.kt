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

    sealed interface Event {
        data class SaveTask(val itemId: String) : Event
        data class Save(val itemId: String) : Event
        data class Edit(val itemId: String) : Event
        data class Delete(val itemId: String) : Event
        data class Add(val itemId: String) : Event
        data class Create(val value: String) : Event

    }

    fun updateEvent(event: Event) {
        when (event) {
            is Event.SaveTask -> saveTask()
            is Event.Edit -> TODO()
            is Event.Save -> TODO()
            is Event.Add -> addSubtask()
            is Event.Delete -> deleteSubtask()
            is Event.Create -> createSubtask()
        }
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