package cs.vsu.taskbench.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.component.TextField
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

@Composable
fun LoginScreen() {
    var isSigningUp by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val topButtonText = if (isSigningUp) "Зарегистрироваться" else "Войти"
    val bottomButtonText = if (isSigningUp) "Назад" else "Зарегистрироваться"

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextField(
                value = email,
                placeholder = "Электронная почта",
                onValueChange = { email = it }
            )
            TextField(
                value = password,
                placeholder = "Пароль",
                password = true,
                onValueChange = { password = it }
            )

            if (isSigningUp) {
                TextField(
                    value = confirmPassword,
                    placeholder = "Подтвердите пароль",
                    password = true,
                    onValueChange = { confirmPassword = it }
                )
            }

            // Top button
            Button(
                text = topButtonText,
                onClick = {},
            )

            // Bottom button
            Button(
                text = bottomButtonText,
                onClick = {
                    isSigningUp = !isSigningUp
                },
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    TaskbenchTheme {
        LoginScreen()
    }
}
