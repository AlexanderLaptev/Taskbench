package cs.vsu.taskbench.ui.login

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.TextField
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.Beige
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.White
import org.koin.androidx.compose.koinViewModel

@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun LoginScreen(
    viewModel: LoginScreenViewModel = koinViewModel(),
    onError: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.errors.collect {
            onError(it)
        }
    }

    LoginScreenContent(
        email = viewModel.email,
        onEmailChange = { viewModel.email = it },
        password = viewModel.password,
        onPasswordChange = { viewModel.password = it },
        confirmPassword = viewModel.confirmPassword,
        onConfirmPasswordChange = { viewModel.confirmPassword = it },
        state = viewModel.state,
        onLogin = viewModel::login,
        onSignUp = viewModel::signUp,
        onSwitchToLogin = viewModel::switchToLogin,
        onSwitchToSignUp = viewModel::switchToSignUp,
        onForgotPassword = viewModel::forgotPassword,
    )
}

@Composable
private fun LoginScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    state: LoginScreenViewModel.State,
    onLogin: () -> Unit,
    onSignUp: () -> Unit,
    onSwitchToLogin: () -> Unit,
    onSwitchToSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    Box {
        Image(
            painter = painterResource(R.drawable.login_screen),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize(),
        )
        Box(
            Modifier
                .offset(y = 40.dp)
                .fillMaxHeight()
                .wrapContentSize()
                .background(
                    color = Beige, shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 80.dp,
                        bottomEnd = 80.dp,
                        bottomStart = 0.dp,
                    )
                )
                .padding(
                    start = 8.dp,
                    top = 24.dp,
                    end = 24.dp,
                )
                .fillMaxHeight(0.5f)
                .fillMaxWidth(0.85f),
        ) {
            ControlsBox(
                email,
                onEmailChange,
                password,
                onPasswordChange,
                confirmPassword,
                onConfirmPasswordChange,
                state,
                onLogin,
                onSignUp,
                onSwitchToLogin,
                onSwitchToSignUp,
                onForgotPassword
            )
        }
    }
}

@Composable
private fun ControlsBox(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    state: LoginScreenViewModel.State,
    onLogin: () -> Unit,
    onSignUp: () -> Unit,
    onSwitchToLogin: () -> Unit,
    onSwitchToSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextField(
            value = email,
            placeholder = stringResource(R.string.label_email),
            onValueChange = onEmailChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(8.dp))

        TextField(
            value = password,
            placeholder = stringResource(R.string.label_password),
            password = true,
            onValueChange = onPasswordChange,
        )
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = state == LoginScreenViewModel.State.SignUp,
        ) {
            Column {
                TextField(
                    value = confirmPassword,
                    placeholder = stringResource(R.string.label_confirm_password),
                    password = true,
                    onValueChange = onConfirmPasswordChange,
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        VariantButton(
            onClick = {
                when (state) {
                    LoginScreenViewModel.State.Login -> onLogin()
                    LoginScreenViewModel.State.SignUp -> onSignUp()
                }
            },
            state,
            R.string.label_login,
            R.string.label_sign_up,
            AccentYellow,
        )
        Spacer(Modifier.height(8.dp))

        VariantButton(
            onClick = {
                when (state) {
                    LoginScreenViewModel.State.Login -> onSwitchToSignUp()
                    LoginScreenViewModel.State.SignUp -> onSwitchToLogin()
                }
            },
            state,
            R.string.label_sign_up,
            R.string.label_back,
            White,
        )

        AnimatedVisibility(
            visible = state == LoginScreenViewModel.State.Login,
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isLinkPressed by interactionSource.collectIsPressedAsState()

            Column {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.label_forgot_password),
                    fontSize = 16.sp,
                    color = if (isLinkPressed) Color(0xFF0823D5) else Color(0xFF586EFF),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(
                            onClick = onForgotPassword,
                            interactionSource = interactionSource,
                            indication = null,
                        ),
                )
            }
        }
    }
}

@Composable
private fun VariantButton(
    onClick: () -> Unit,
    state: LoginScreenViewModel.State,
    @StringRes loginText: Int,
    @StringRes signUpText: Int,
    color: Color,
) {
    Button(onClick = onClick, color = color) {
        AnimatedContent(
            targetState = state,
            transitionSpec = { scaleIn().togetherWith(scaleOut()) }
        ) {
            when (it) {
                LoginScreenViewModel.State.Login -> ButtonText(stringResource(loginText))
                LoginScreenViewModel.State.SignUp -> ButtonText(stringResource(signUpText))
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

@Preview
@Composable
private fun PreviewLogin() {
    LoginScreenContent(
        email = "",
        onEmailChange = {},
        password = "",
        onPasswordChange = {},
        confirmPassword = "",
        onConfirmPasswordChange = {},
        state = LoginScreenViewModel.State.Login,
        onLogin = {},
        onSignUp = {},
        onSwitchToLogin = {},
        onSwitchToSignUp = {},
        onForgotPassword = {},
    )
}
