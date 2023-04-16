package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Watch
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun AddAppointmentScreen(viewModel: MainViewModel) {

    val availableAppointments = viewModel.availableAppointments.observeAsState().value

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)) {
                Text("Schedule An Appointment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center)
            }
        }

        if (availableAppointments == null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = "There are no available appointments. Please try again later.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            items(availableAppointments) { appointment ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
                    shape = MaterialTheme.shapes.small,
                    elevation = 10.dp,
                    border = BorderStroke(width = 3.dp, color = Color.Black)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Icon(modifier = Modifier.padding(start = 5.dp),
                            imageVector = Icons.Default.Timelapse,
                            contentDescription = "Clock Icon")
                        Text(text = Utils.convertTimestampToDate(appointment.appointmentDate).toString(),
                            fontSize = 15.sp)
                        TextButton(onClick = {}) {
                            Text("Schedule", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

    }
}