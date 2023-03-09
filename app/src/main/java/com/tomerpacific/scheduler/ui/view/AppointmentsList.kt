package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tomerpacific.scheduler.ui.model.MainViewModel


@Composable
fun AppointmentsList(viewModel: MainViewModel) {

    val appointments = viewModel.getAppointmentsForUser()

    if (appointments.isNotEmpty()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(appointments) { appointment ->
                Row() {
                    Text(text = viewModel.convertTimestampToDate(appointment.appointmentDate).toString())
                }
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center) {
            Text(text = "You have no upcoming appointments.")
        }
    }


}