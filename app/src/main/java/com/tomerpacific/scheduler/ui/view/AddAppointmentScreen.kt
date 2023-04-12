package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun AddAppointmentScreen(viewModel: MainViewModel) {

    val availableAppointments = viewModel.availableAppointments.observeAsState().value

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Add An Appointment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center)
            }
        }

//        item {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                TextButton(onClick = {
//                    val date = Date()
//                    date.hours = 11
//                    date.minutes = 0
//                    date.seconds = 0
//                    val appointment = AppointmentModel(
//                        date.time,
//                        "somewhere",
//                        "one hour"
//                    )
//                    viewModel.addAppointment(appointment)
//                }) {
//                    Text("Add Appointment")
//                }
//            }
//        }

        if (availableAppointments == null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "There are no available appointments. Please try again later.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            items(availableAppointments) { appointment ->
                Card(modifier = Modifier.fillMaxWidth()
                    .height(50.dp)
                    .padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
                    shape = MaterialTheme.shapes.small,
                    backgroundColor = Color.Transparent,
                    border = BorderStroke(width = 3.dp, color = Color.Black)) {
                    Text(text = appointment.appointmentDate.toString(),
                        fontSize = 20.sp)
                }
            }
        }

    }
}