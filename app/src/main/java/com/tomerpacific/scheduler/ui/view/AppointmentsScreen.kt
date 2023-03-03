package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun AppointmentsScreen(viewModel: MainViewModel) {

    val isUserConnected by remember { mutableStateOf(viewModel.isUserConnected()) }

    Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center) {
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