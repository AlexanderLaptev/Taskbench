package cs.vsu.taskbench.ui.login

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.TaskCreationScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.TextField
import cs.vsu.taskbench.ui.login.LoginScreenViewModel.Event.Error
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightYellow
import cs.vsu.taskbench.ui.theme.Link
import cs.vsu.taskbench.ui.theme.LinkPressed
import cs.vsu.taskbench.ui.theme.White
import cs.vsu.taskbench.ui.theme.swapTransition
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import cs.vsu.taskbench.ui.login.LoginScreenViewModel.State as LoginState

@Stable
private data class LoginScreenState(
    val email: String,
    val onEmailChange: (String) -> Unit,
    val password: String,
    val onPasswordChange: (String) -> Unit,
    val confirmPassword: String,
    val onConfirmPasswordChange: (String) -> Unit,
    val loginState: LoginState,
    val onLogin: () -> Unit,
    val onSignUp: () -> Unit,
    val onSwitchToLogin: () -> Unit,
    val onSwitchToSignUp: () -> Unit,
    val onForgotPassword: () -> Unit,
)

@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator,
) {
    val viewModel = koinViewModel<LoginScreenViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val resources = LocalContext.current.resources
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is Error -> scope.launch {
                    val messageId = when (event) {
                        Error.EmptyEmail -> R.string.error_empty_email
                        Error.InvalidEmail -> R.string.error_invalid_email
                        Error.LoginFailure -> R.string.error_login_failure
                        Error.EmptyPassword -> R.string.error_empty_password
                        Error.PasswordsDoNotMatch -> R.string.error_passwords_do_not_match
                        Error.NoInternet -> R.string.error_no_internet
                        Error.Unknown -> R.string.error_unknown
                        Error.SignUpFailure -> TODO()
                    }
                    with(snackbarHostState) {
                        currentSnackbarData?.dismiss()
                        showSnackbar(
                            resources.getString(messageId),
                            withDismissAction = true
                        )
                    }
                }

                LoginScreenViewModel.Event.LoggedIn -> {
                    navigator.popBackStack()
                    navigator.navigate(TaskCreationScreenDestination)
                }
            }
        }
    }

    val screenState = LoginScreenState(
        email = viewModel.email,
        onEmailChange = { viewModel.email = it },
        password = viewModel.password,
        onPasswordChange = { viewModel.password = it },
        confirmPassword = viewModel.confirmPassword,
        onConfirmPasswordChange = { viewModel.confirmPassword = it },
        loginState = viewModel.state,
        onLogin = viewModel::login,
        onSignUp = viewModel::signUp,
        onSwitchToLogin = viewModel::switchToLogin,
        onSwitchToSignUp = viewModel::switchToSignUp,
        onForgotPassword = viewModel::forgotPassword,
    )
    LoginScreenContent(snackbarHostState, screenState)
}

private const val OFFSET_FRACTION = 0.16f

private val LOGIN_CONTROLS_ITEM_SPACING = 8.dp

private val loginControlsModifier = Modifier
    .background(
        color = LightYellow,
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 80.dp,
            bottomEnd = 80.dp,
            bottomStart = 0.dp,
        )
    )
    .padding(
        start = 16.dp,
        top = 48.dp,
        end = 32.dp
    )
    .height(360.dp)
    .fillMaxWidth(0.9f)

@Composable
private fun LoginScreenContent(
    snackbarHostState: SnackbarHostState,
    state: LoginScreenState,
) {
    Scaffold(containerColor = Beige) { padding ->
        // We ignore paddings since our background vector is already
        // edge-to-edge, and our layout is sufficiently far away from
        // the edges of the screen.

        Image(
            painter = painterResource(R.drawable.background_login_screen),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )

        val offset = (LocalConfiguration.current.screenHeightDp * OFFSET_FRACTION).dp
        Column(
            verticalArrangement = Arrangement.spacedBy(48.dp),
            modifier = Modifier
                .offset(y = offset)
                .fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(R.drawable.logo_full_dark),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            LoginControls(
                screenState = state,
                modifier = Modifier.align(Alignment.Start),
            )
        }

        val systemBarHeight = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .let {
                    if (
                        WindowInsets.ime
                            .asPaddingValues()
                            .calculateBottomPadding() > systemBarHeight
                    ) it else it.padding(padding)
                }
                .imePadding()
                .fillMaxSize(),
        ) { SnackbarHost(snackbarHostState) }
    }
}


@Composable
private fun LoginControls(
    screenState: LoginScreenState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier then loginControlsModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextField(
            value = screenState.email,
            color = Beige,
            placeholder = stringResource(R.string.label_email),
            onValueChange = screenState.onEmailChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        Spacer(Modifier.height(LOGIN_CONTROLS_ITEM_SPACING))
        TextField(
            value = screenState.password,
            color = Beige,
            placeholder = stringResource(R.string.placeholder_password),
            password = true,
            onValueChange = screenState.onPasswordChange,
        )

        AnimatedVisibility(
            visible = screenState.loginState == LoginState.SignUp,
        ) {
            Column {
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = screenState.confirmPassword,
                    color = Beige,
                    placeholder = stringResource(R.string.placeholder_confirm_password),
                    password = true,
                    onValueChange = screenState.onConfirmPasswordChange,
                )
            }
        }

        Spacer(Modifier.height(LOGIN_CONTROLS_ITEM_SPACING))
        VariantButton(
            onClick = {
                when (screenState.loginState) {
                    LoginState.Login -> screenState.onLogin()
                    LoginState.SignUp -> screenState.onSignUp()
                }
            },
            screenState.loginState,
            R.string.label_login,
            R.string.label_sign_up,
            White,
        )

        Spacer(Modifier.height(LOGIN_CONTROLS_ITEM_SPACING))
        VariantButton(
            onClick = {
                when (screenState.loginState) {
                    LoginState.Login -> screenState.onSwitchToSignUp()
                    LoginState.SignUp -> screenState.onSwitchToLogin()
                }
            },
            screenState.loginState,
            R.string.label_sign_up,
            R.string.label_back,
            White,
        )

        AnimatedVisibility(
            visible = screenState.loginState == LoginState.Login,
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isLinkPressed by interactionSource.collectIsPressedAsState()

            Column {
                Spacer(Modifier.height(LOGIN_CONTROLS_ITEM_SPACING))
                Text(
                    text = stringResource(R.string.label_forgot_password),
                    fontSize = 14.sp,
                    color = if (isLinkPressed) LinkPressed else Link,
                    textDecoration = TextDecoration.Underline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable(
                            onClick = screenState.onForgotPassword,
                            interactionSource = interactionSource,
                            indication = null,
                        )
                        .height(32.dp)
                        .fillMaxWidth(0.5f),
                )
            }
        }
    }
}

@Composable
private fun VariantButton(
    onClick: () -> Unit,
    state: LoginState,
    @StringRes loginText: Int,
    @StringRes signUpText: Int,
    color: Color,
) {
    Button(onClick = onClick, color = color) {
        AnimatedContent(
            targetState = state,
            transitionSpec = swapTransition(),
        ) {
            when (it) {
                LoginState.Login -> ButtonText(stringResource(loginText))
                LoginState.SignUp -> ButtonText(stringResource(signUpText))
            }
        }
    }
}

@Composable
private fun ButtonText(text: String) {
    Text(
        text = text,
        style = TextStyle(color = Black, fontSize = 16.sp),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(device = "id:Nexus 6P")
@Preview(device = "id:pixel_9")
@Preview(device = "id:pixel_4")
@Composable
private fun Preview() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loginState by remember { mutableStateOf(LoginState.Login) }

    val screenState = LoginScreenState(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        confirmPassword = confirmPassword,
        onConfirmPasswordChange = { confirmPassword = it },
        loginState = loginState,
        onSwitchToLogin = { loginState = LoginState.Login },
        onSwitchToSignUp = { loginState = LoginState.SignUp },
        onLogin = {},
        onSignUp = {},
        onForgotPassword = {},
    )
    LoginScreenContent(remember { SnackbarHostState() }, screenState)
}
