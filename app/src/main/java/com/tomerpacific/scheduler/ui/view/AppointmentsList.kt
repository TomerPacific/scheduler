package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.runtime.Composable
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
fun AppointmentsList(viewModel: MainViewModel,
                     onAppointmentCancelled: (String?) -> Unit) {

    val appointments = viewModel.appointments.observeAsState()

    if (appointments.value != null &&
        appointments.value!!.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 25.dp),
             verticalAlignment = Alignment.CenterVertically) {
            Text("Your Scheduled Appointments",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(appointments.value!!) { appointment ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
                    shape = MaterialTheme.shapes.small,
                    elevation = 10.dp,
                    border = BorderStroke(width = 3.dp, color = Color.Black)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Icon(modifier = Modifier.padding(start = 5.dp),
                            imageVector = Icons.Default.SupervisedUserCircle,
                            contentDescription = "Cancel Icon")
                        Text(text = Utils.convertTimestampToDate(appointment.appointmentDate).toString(),
                            fontSize = 15.sp)
                        TextButton(onClick = {
                            viewModel.cancelScheduledAppointmentForUser(appointment, onAppointmentCancelled)
                        },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    }
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