package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun AppointmentsScreen(viewModel: MainViewModel, onUserLogout: () -> Unit) {
    val user = viewModel.user.observeAsState()
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {}) {
            Icon(Icons.Default.Add, contentDescription = "Add Appointment")
        }
    }) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End) {
                    LogoutButton(shouldBeDisplayed = user != null, onClickHandler = {
                        viewModel.logout()
                        onUserLogout()
                    })
                }
                AppointmentsList(viewModel)
                Row(modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center) {
                    CircularProgressBarIndicator(shouldBeDisplayed = user == null)
                }
            }
        }
    }
}