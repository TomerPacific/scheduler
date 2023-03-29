package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun AddAppointmentScreen(viewModel: MainViewModel) {

    val availableAppointments = viewModel.adminAppointments.observeAsState()
    val areThereAnyAvailableAppointments: Boolean = when(availableAppointments.value) {
        null -> false
        else -> true
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Add An Appointment",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                textAlign = TextAlign.Center)
        }

        if (!areThereAnyAvailableAppointments) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "There are no available appointments. Please try again later.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                items(availableAppointments.value!!) { appointment ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(text = appointment.appointmentDate.toString())
                    }
                }
            }
        }
    }
}