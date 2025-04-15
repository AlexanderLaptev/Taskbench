package cs.vsu.taskbench.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginScreenViewModel : ViewModel() {
    enum class State {
        Login,
        SignUp,
    }

    private var _state = MutableStateFlow(State.Login)
    val state = _state.asStateFlow()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    fun login() {}

    fun signUp() {}

    fun switchToSignUp() {}

    fun switchToLogin() {}
}
