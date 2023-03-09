package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun AppointmentsScreen(viewModel: MainViewModel, onUserLogout: () -> Unit) {

    val user = viewModel.user.observeAsState()

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