package cs.vsu.taskbench.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class LoginScreenViewModel : ViewModel() {
    enum class State {
        Login,
        SignUp,
    }

    sealed interface Result {
        data class Error(val message: String): Result
        data object Success: Result
    }

    var state by mutableStateOf(State.Login)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    private val _messages = MutableSharedFlow<Result>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val messages = _messages.asSharedFlow()

    fun login() {
        _messages.tryEmit(Result.Success)
    }

    fun signUp() {
        _messages.tryEmit(Result.Error("Signing up"))
    }

    fun switchToSignUp() {
        if (state == State.SignUp) return
        confirmPassword = ""
        state = State.SignUp
    }

    fun switchToLogin() {
        if (state == State.Login) return
        state = State.Login
    }

    fun forgotPassword() {
        _messages.tryEmit(Result.Error("Forgot password"))
    }
}
