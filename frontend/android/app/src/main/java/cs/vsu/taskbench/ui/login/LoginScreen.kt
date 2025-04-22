package cs.vsu.taskbench.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.ScreenTransitions
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.TextField
import org.koin.androidx.compose.koinViewModel

@Destination<RootGraph>(style = ScreenTransitions::class)
@Composable
fun LoginScreen(
    viewModel: LoginScreenViewModel = koinViewModel(),
) {
    val loginState by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextField(
                value = viewModel.email,
                placeholder = stringResource(R.string.label_email),
                onValueChange = { viewModel.email = it }
            )

            TextField(
                value = viewModel.password,
                placeholder = stringResource(R.string.label_password),
                password = true,
                onValueChange = { viewModel.password = it }
            )

            if (loginState == LoginScreenViewModel.State.SignUp) {
                TextField(
                    value = viewModel.confirmPassword,
                    placeholder = stringResource(R.string.label_confirm_password),
                    password = true,
                    onValueChange = { viewModel.confirmPassword = it }
                )
            }

            val topButtonText = when (loginState) {
                LoginScreenViewModel.State.Login -> stringResource(R.string.label_login)
                LoginScreenViewModel.State.SignUp -> stringResource(R.string.label_sign_up)
            }

            val bottomButtonText = when (loginState) {
                LoginScreenViewModel.State.Login -> stringResource(R.string.label_sign_up)
                LoginScreenViewModel.State.SignUp -> stringResource(R.string.label_back)
            }

            // Top button
            Button(
                text = topButtonText,
                onClick = {
                    when (loginState) {
                        LoginScreenViewModel.State.Login -> viewModel.login()
                        LoginScreenViewModel.State.SignUp -> viewModel.switchToSignUp()
                    }
                },
            )

            // Bottom button
            Button(
                text = bottomButtonText,
                onClick = {
                    when (loginState) {
                        LoginScreenViewModel.State.Login -> viewModel.signUp()
                        LoginScreenViewModel.State.SignUp -> viewModel.switchToLogin()
                    }
                },
            )
        }
    }
}
