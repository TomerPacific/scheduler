package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun AppointmentsScreen(viewModel: MainViewModel, onUserLogout: () -> Unit) {

    val isUserConnected by remember { mutableStateOf(viewModel.isUserConnected()) }

    Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                viewModel.logout()
                onUserLogout()
            }) {
                Text("Logout")
            }
        }
        Row(modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center) {
            if (!isUserConnected) {
                CircularProgressIndicator()
            } else {
                Text(text = "Welcome!")
            }
        }
    }
}