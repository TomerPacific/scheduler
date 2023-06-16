package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.ui.model.AppointmentModel

@Composable
fun AppointmentScreen(appointment: AppointmentModel) {
    Column(modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 200.dp),
            horizontalArrangement = Arrangement.Center) {
            Text("Appointment Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center) {
            Text(
                "Appointment Scheduled on \n ${Utils.convertTimestampToDate(appointment.appointmentDate)}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
        Row(modifier = Modifier.fillMaxWidth()
            .background(Color.White)
            .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center) {
            Text(
                "Appointment Duration : ${appointment.appointmentDuration}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center)
        }
        Row(modifier = Modifier.fillMaxWidth().background(Color.White),
            horizontalArrangement = Arrangement.Center) {
            Text(
                "Appointment Place : ${appointment.appointmentPlace}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center)
        }
    }
}