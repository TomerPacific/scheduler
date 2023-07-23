package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tomerpacific.scheduler.R
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun AppointmentsScreen(viewModel: MainViewModel,
                       onUserLogout: () -> Unit,
                       onAddAppointmentClicked: () -> Unit,
                       onAppointmentCancelled: (String?, String?) -> Unit,
                       onAppointmentClicked: (String) -> Unit) {

    viewModel.disableCircularProgressBarIndicator()
    val user = viewModel.user.observeAsState()

    Scaffold(floatingActionButton = {
        if (!viewModel.isAdminUser()) {
            FloatingActionButton(onClick = { onAddAppointmentClicked() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Appointment")
            }
        }
    }) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_black),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.matchParentSize()
                )
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End) {
                    LogoutButton(shouldBeDisplayed = user.value != null, onClickHandler = {
                        viewModel.logout()
                        onUserLogout()
                    })
                }
                AppointmentsList(viewModel, onAppointmentCancelled, onAppointmentClicked)
                Row(modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center) {
                    CircularProgressBarIndicator(shouldBeDisplayed = user.value == null)
                }
            }
        }
    }
}