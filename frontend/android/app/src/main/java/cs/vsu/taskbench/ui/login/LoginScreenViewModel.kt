package cs.vsu.taskbench.ui.login

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.SettingsRepository
import cs.vsu.taskbench.data.auth.AuthorizationService
import cs.vsu.taskbench.data.user.UserRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    private val authService: AuthorizationService,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
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
        Unknown(R.string.placeholder),
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
        viewModelScope.launch { handleLogin() }
    }

    private suspend fun handleLogin() {
        when (val result = authService.authorize(email, password)) {
            AuthorizationService.Result.Error -> {
                _events.tryEmit(Event.Error(ErrorType.Unknown))
                return
            }

            is AuthorizationService.Result.Success -> {
                saveJwtToken(result.jwtToken)
                userRepository.fetchUser(result.jwtToken)
                Log.d(TAG, "current user: ${userRepository.user}")
                _events.tryEmit(Event.LoggedIn)
            }
        }
    }

    private suspend inline fun saveJwtToken(token: String) {
        Log.d(TAG, "saving token: $token")
        settingsRepository.setJwtToken(token)
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

    private fun String.isValidEmail(): Boolean = "@" in this // TODO

    fun signUp() {
        if (!validateSignUp()) return
        viewModelScope.launch { handleSignUp() }
    }

    private fun validateSignUp(): Boolean {
        if (!validateLogin()) return false
        if (password != confirmPassword) {
            _events.tryEmit(Event.Error(ErrorType.PasswordsDoNotMatch))
            return false
        }
        return true
    }

    private suspend inline fun handleSignUp() {
        when (val result = authService.signUp(email, password)) {
            AuthorizationService.Result.Error -> {
                _events.tryEmit(Event.Error(ErrorType.Unknown))
                return
            }

            is AuthorizationService.Result.Success -> {
                saveJwtToken(result.jwtToken)
                userRepository.fetchUser(result.jwtToken)
                Log.d(TAG, "current user: ${userRepository.user}")
                _events.tryEmit(Event.LoggedIn)
            }
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
