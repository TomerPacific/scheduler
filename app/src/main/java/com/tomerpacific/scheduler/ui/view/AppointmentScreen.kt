package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.ui.model.AppointmentModel

@Composable
fun AppointmentScreen(appointment: AppointmentModel) {

    val calendarIconPlaceholderId = "id"
    val appointmentScheduledText: AnnotatedString = buildAnnotatedString {
        append("Appointment Scheduled on \n")
        appendInlineContent(calendarIconPlaceholderId, "[icon]")
        append("${Utils.convertTimestampToDate(appointment.appointmentDate)}")
    }
    val appointmentScheduledInlineText = mapOf(
        Pair(
            calendarIconPlaceholderId,
            InlineTextContent(
                Placeholder(
                    width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center)
            ) {
                Icon(Icons.Filled.CalendarMonth, "", tint = Color.Black)
            }
        )
    )

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
                appointmentScheduledText,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                inlineContent = appointmentScheduledInlineText
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