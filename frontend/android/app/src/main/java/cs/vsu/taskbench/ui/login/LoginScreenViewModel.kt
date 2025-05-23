package cs.vsu.taskbench.ui.login

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.vsu.taskbench.data.analytics.AnalyticsFacade
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.LoginException
import cs.vsu.taskbench.domain.usecase.BootstrapUseCase
import cs.vsu.taskbench.util.mutableEventFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.net.ConnectException

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
            EmptyPassword,
            InvalidEmail,
            LoginFailure,
            SignUpFailure,
            PasswordsDoNotMatch,
            NoInternet,
            Unknown,
        }

        data object LoggedIn : Event
    }

    var state by mutableStateOf(State.Login)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    private val _events = mutableEventFlow<Event>()
    val events = _events.asSharedFlow()

    fun login() {
        if (!validateLogin()) return
        viewModelScope.launch { handleLogin(isSignUp = false) }
    }

    // TODO: password requirements
    private fun validateLogin(): Boolean {
        if (email.isBlank()) {
            AnalyticsFacade.logLoginFailure("EmptyEmail")
            _events.tryEmit(Event.Error.EmptyEmail)
            return false
        }
        if (!email.isValidEmail()) {
            AnalyticsFacade.logLoginFailure("InvalidEmail")
            _events.tryEmit(Event.Error.InvalidEmail)
            return false
        }
        if (password.isBlank()) {
            AnalyticsFacade.logLoginFailure("EmptyPassword")
            _events.tryEmit(Event.Error.EmptyPassword)
            return false
        }
        return true
    }

    private fun String.isValidEmail(): Boolean =
        this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun signUp() {
        if (!validateSignUp()) return
        viewModelScope.launch { handleLogin(isSignUp = true) }
    }

    private fun validateSignUp(): Boolean {
        if (!validateLogin()) return false
        if (password != confirmPassword) {
            AnalyticsFacade.logLoginFailure("PasswordsDoNotMatch")
            _events.tryEmit(Event.Error.PasswordsDoNotMatch)
            return false
        }
        return true
    }

    private suspend inline fun handleLogin(isSignUp: Boolean) {
        try {
            if (isSignUp) {
                AnalyticsFacade.logEvent("signup_attempt", mapOf("email" to email))
                authService.signUp(email, password)
            } else {
                AnalyticsFacade.logEvent("login_attempt", mapOf("email" to email))
                authService.login(email, password)
            }
        } catch (e: ConnectException) {
            AnalyticsFacade.logLoginFailure("NoInternet")
            AnalyticsFacade.logError("login_no_internet", e)
            Log.e(TAG, "connection error", e)
            _events.tryEmit(Event.Error.NoInternet)
            return
        } catch (e: LoginException) {
            val reason = if (isSignUp) "SignUpFailure" else "LoginFailure"
            AnalyticsFacade.logLoginFailure(reason)
            AnalyticsFacade.logError("login_failure", e)
            Log.e(TAG, "login error", e)
            _events.tryEmit(
                if (isSignUp) {
                    Event.Error.SignUpFailure
                } else Event.Error.LoginFailure
            )
            return
        } catch (e: Exception) {
            AnalyticsFacade.logLoginFailure("Unknown")
            AnalyticsFacade.logError("login_unknown_error", e)
            Log.e(TAG, "unknown error", e)
            _events.tryEmit(Event.Error.Unknown)
            return
        }

        if (isSignUp) {
            AnalyticsFacade.logEvent("signup_success", mapOf("email" to email))
        } else {
            AnalyticsFacade.logLoginSuccess(email)
        }
        bootstrapUseCase()
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
        // TODO
        AnalyticsFacade.logEvent("login_forgot_password_clicked", mapOf("email" to email))
        Log.d(TAG, "forgot password")
    }
}
