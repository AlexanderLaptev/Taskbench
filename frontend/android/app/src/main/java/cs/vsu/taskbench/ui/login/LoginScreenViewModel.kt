package cs.vsu.taskbench.ui.login

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.AuthService.LoginResult
import cs.vsu.taskbench.data.auth.AuthService.SignUpResult
import cs.vsu.taskbench.domain.usecase.BootstrapUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    private val authService: AuthService,
    private val bootstrapUseCase: BootstrapUseCase,
) : ViewModel() {
    companion object {
        private val TAG = LoginScreenViewModel::class.simpleName
    }

    enum class State {
        Login,
        SignUp,
    }

    sealed interface Event {
        enum class Error : Event {
            EmptyEmail,
            InvalidEmail,
            EmptyPassword,
            PasswordsDoNotMatch,
            UserDoesNotExist,
            UserAlreadyExists,
            IncorrectPassword,
            NoInternet,
            Unknown,
        }

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
        viewModelScope.launch { handleLogin() }
    }

    private suspend fun handleLogin() {
        val result = authService.login(email, password)
        when (result) {
            LoginResult.Success -> {
                // Ignore the result since at this point we should
                // be authorized anyway.
                bootstrapUseCase()
                _events.tryEmit(Event.LoggedIn)
            }

            LoginResult.UserNotFound -> _events.tryEmit(Event.Error.UserDoesNotExist)
            LoginResult.IncorrectPassword -> _events.tryEmit(Event.Error.IncorrectPassword)
            LoginResult.UnknownError -> _events.tryEmit(Event.Error.Unknown)
        }
    }

    // TODO: password requirements
    private fun validateLogin(): Boolean {
        if (email.isBlank()) {
            _events.tryEmit(Event.Error.EmptyEmail)
            return false
        }
        if (!email.isValidEmail()) {
            _events.tryEmit(Event.Error.InvalidEmail)
            return false
        }
        if (password.isBlank()) {
            _events.tryEmit(Event.Error.EmptyPassword)
            return false
        }
        return true
    }

    private fun String.isValidEmail(): Boolean =
        this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun signUp() {
        if (!validateSignUp()) return
        viewModelScope.launch { handleSignUp() }
    }

    private fun validateSignUp(): Boolean {
        if (!validateLogin()) return false
        if (password != confirmPassword) {
            _events.tryEmit(Event.Error.PasswordsDoNotMatch)
            return false
        }
        return true
    }

    private suspend inline fun handleSignUp() {
        val result = authService.signUp(email, password)
        when (result) {
            SignUpResult.Success -> {
                // Ignore the result since at this point we should
                // be authorized anyway.
                bootstrapUseCase()
                _events.tryEmit(Event.LoggedIn)
            }

            SignUpResult.UserAlreadyExists -> _events.tryEmit(Event.Error.UserAlreadyExists)
            SignUpResult.UnknownError -> _events.tryEmit(Event.Error.Unknown)
        }
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
        // TODO
        Log.d(TAG, "forgot password")
    }
}
