package cs.vsu.taskbench.ui.login

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.auth.AuthorizationService
import cs.vsu.taskbench.data.user.UserRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class LoginScreenViewModel(
    private val authService: AuthorizationService,
    private val userRepository: UserRepository,
) : ViewModel() {
    companion object {
        private val TAG = LoginScreenViewModel::class.simpleName
    }

    enum class State {
        Login,
        SignUp,
    }

    enum class ErrorType(@StringRes val messageId: Int) {
        EmptyEmail(R.string.error_empty_email),
        InvalidEmail(R.string.error_invalid_email),
        EmptyPassword(R.string.placeholder),
        PasswordsDoNotMatch(R.string.placeholder),
        UserDoesNotExist(R.string.placeholder),
        IncorrectPassword(R.string.placeholder),
        NoInternet(R.string.placeholder),
    }

    sealed interface Event {
        data class Error(val type: ErrorType) : Event
        data object LoggedIn : Event
    }

    var state by mutableStateOf(State.Login)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    private val _events = MutableSharedFlow<Event>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events = _events.asSharedFlow()

    fun login() {
        if (!validateLogin()) return
        _events.tryEmit(Event.LoggedIn)
    }

    private fun validateLogin(): Boolean {
        if (email.isBlank()) {
            _events.tryEmit(Event.Error(ErrorType.EmptyEmail))
            return false
        }
        if (!email.isValidEmail()) {
            _events.tryEmit(Event.Error(ErrorType.InvalidEmail))
            return false
        }
        if (password.isBlank()) {
            _events.tryEmit(Event.Error(ErrorType.EmptyPassword))
            return false
        }
        return true
    }

    private fun String.isValidEmail(): Boolean = TODO()

    fun signUp() {
        _events.tryEmit(Event.LoggedIn)
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
        Log.d(TAG, "forgot password")
    }
}
