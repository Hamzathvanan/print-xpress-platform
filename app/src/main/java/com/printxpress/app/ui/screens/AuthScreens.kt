package com.printxpress.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.printxpress.app.data.PrintXpressRepository
import com.printxpress.app.ui.components.BrandHeader
import com.printxpress.app.ui.components.ErrorBanner
import com.printxpress.app.ui.components.PrimaryButton
import com.printxpress.app.ui.components.ScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: PrintXpressRepository,
    onLoggedIn: () -> Unit,
    onGoRegister: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ScreenScaffold {
        ColumnScroll {
            BrandHeader("Digital printing for Sri Lanka — sign in to order.")
            Spacer(modifier = Modifier.height(24.dp))
            AppPasswordFields(
                identifier = identifier,
                onIdentifier = { identifier = it },
                password = password,
                onPassword = { password = it },
                identifierLabel = "Email or mobile number"
            )
            Spacer(modifier = Modifier.height(12.dp))
            ErrorBanner(error)
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(
                text = "Sign in",
                loading = loading,
                onClick = {
                    loading = true
                    error = null
                    scope.launch {
                        val result = repository.login(identifier, password)
                        loading = false
                        result.onSuccess { onLoggedIn() }
                            .onFailure { error = it.message }
                    }
                }
            )
            TextButton(onClick = onGoRegister) {
                Text("New here? Create an account")
            }
        }
    }
}

@Composable
fun RegisterScreen(
    repository: PrintXpressRepository,
    onRegistered: () -> Unit,
    onGoLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ScreenScaffold {
        ColumnScroll {
            BrandHeader("Create your PrintXpress customer account.")
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (optional if phone provided)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Mobile (optional if email provided)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (min 6 characters)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ErrorBanner(error)
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(
                text = "Create account",
                loading = loading,
                onClick = {
                    loading = true
                    error = null
                    scope.launch {
                        val result = repository.register(
                            fullName = fullName,
                            email = email.ifBlank { null },
                            phone = phone.ifBlank { null },
                            password = password
                        )
                        loading = false
                        result.onSuccess { onRegistered() }
                            .onFailure { error = it.message }
                    }
                }
            )
            TextButton(onClick = onGoLogin) {
                Text("Already registered? Sign in")
            }
        }
    }
}

@Composable
private fun ColumnScroll(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        content = content
    )
}

@Composable
private fun AppPasswordFields(
    identifier: String,
    onIdentifier: (String) -> Unit,
    password: String,
    onPassword: (String) -> Unit,
    identifierLabel: String
) {
    OutlinedTextField(
        value = identifier,
        onValueChange = onIdentifier,
        label = { Text(identifierLabel) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = password,
        onValueChange = onPassword,
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        shape = RoundedCornerShape(12.dp)
    )
}
