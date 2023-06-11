package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.ui.model.AppointmentModel

@Composable
fun AppointmentScreen(appointment: AppointmentModel) {
    Column(modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Appointment Scheduled on : ${Utils.convertTimestampToDate(appointment.appointmentDate)}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center)
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center) {
            Text(
                "Appointment Duration : ${appointment.appointmentDuration}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center)
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center) {
            Text(
                "Appointment Place : ${appointment.appointmentPlace}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center)
        }
    }
}