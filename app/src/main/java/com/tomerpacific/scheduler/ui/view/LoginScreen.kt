package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun LoginScreen(viewModel: MainViewModel, onNavigateAfterLoginScreen: () -> Unit) {

    val shouldDisplayCircularProgressBar = viewModel.shouldDisplayCircularProgressBar.observeAsState()

    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row() {
            Text(
                "Welcome To Scheduler",
                style = MaterialTheme.typography.h1
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .width(300.dp)
                    .padding(top = 370.dp),
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "emailIcon",
                        tint = Color.Blue
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
            OutlinedTextField(
                modifier = Modifier.width(300.dp),
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter your password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(20.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "lockIcon",
                        tint = Color.Blue
                    )
                },
                trailingIcon = {
                    val iconImage = if (passwordVisible) {
                        Icons.Default.Visibility
                    } else {
                        Icons.Default.VisibilityOff
                    }

                    IconButton(onClick = {
                        passwordVisible = !passwordVisible
                    }) {
                        Icon(
                            imageVector = iconImage,
                            contentDescription = "passwordIcon",
                            tint = Color.Blue
                        )
                    }
                },
            )
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = {
                    if (viewModel.isUserInputValid(email.text, password.text)) {
                        viewModel.loginUser(email.text, password.text, onNavigateAfterLoginScreen)
                    }
                }) {
                    Text("Login", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = {
                    if (viewModel.isUserInputValid(email.text, password.text)) {
                        viewModel.signupUser(email.text, password.text, onNavigateAfterLoginScreen)
                    }
                }) {
                    Text("Sign up", fontWeight = FontWeight.Bold)
                }
            }
            CircularProgressBarIndicator(shouldBeDisplayed = shouldDisplayCircularProgressBar.value!!)
        }
    }
}