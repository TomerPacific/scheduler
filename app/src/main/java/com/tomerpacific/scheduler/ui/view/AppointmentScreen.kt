package com.tomerpacific.scheduler.ui.view

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.tomerpacific.scheduler.ui.model.MainViewModel
import kotlin.time.Duration

@Composable
fun AppointmentScreen(appointment: AppointmentModel,
                      onAddLocationPressed: () -> Unit) {

    val areLocationPermissionsGranted = remember {
        mutableStateOf(false)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isPermissionGranted ->
            areLocationPermissionsGranted.value = isPermissionGranted
            if (isPermissionGranted) {
                onAddLocationPressed()
            }
        }
    )

    val iconPlaceholderId = "iconPlaceholderId"
    val appointmentScheduledText: AnnotatedString = buildAnnotatedString {
        append("Appointment Scheduled on \n")
        appendInlineContent(iconPlaceholderId, "[icon]")
        append("${Utils.convertTimestampToDate(appointment.appointmentDate)}")
    }

    val appointmentScheduledInlineText = mapOf(
        Pair(
            iconPlaceholderId,
            InlineTextContent(
                Placeholder(
                    width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Icon(Icons.Filled.CalendarMonth, "Calendar icon", tint = Color.Black)
            }
        )
    )

    val appointmentDurationText: AnnotatedString = buildAnnotatedString {
        append("Appointment Duration \n")
        appendInlineContent(iconPlaceholderId, "[icon]")
        append(Duration.parseIsoString(appointment.appointmentDuration).toString())
    }

    val appointmentDurationInlineText = mapOf(
        Pair(
            iconPlaceholderId,
            InlineTextContent(
                Placeholder(
                    width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Icon(Icons.Filled.HourglassFull, "Hour glass icon", tint = Color.Black)
            }
        )
    )

    val appointmentPlaceText: AnnotatedString = buildAnnotatedString {
        append("Appointment Place \n")
        appendInlineContent(iconPlaceholderId, "[icon]")
        append(appointment.appointmentPlace)
    }

    val appointmentPlaceInlineText = mapOf(
        Pair(
            iconPlaceholderId,
            InlineTextContent(
                Placeholder(
                    width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Icon(Icons.Filled.LocationOn, "Location icon", tint = Color.Black)
            }
        )
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 230.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Appointment Details",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                appointmentScheduledText,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                inlineContent = appointmentScheduledInlineText
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                appointmentDurationText,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                inlineContent = appointmentDurationInlineText
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            horizontalArrangement = Arrangement.Center
        ) {
            if (appointment.appointmentPlace.isEmpty()) {
                Button(onClick = {
                    if (areLocationPermissionsGranted.value) {
                        onAddLocationPressed()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }) {
                    Text("Add A Location")
                }
            } else {
                Text(
                    appointmentPlaceText,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    inlineContent = appointmentPlaceInlineText
                )
            }

        }
    }
}