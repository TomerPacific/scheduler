package com.tomerpacific.scheduler.ui.view

import android.Manifest
import android.content.Context
import android.location.Geocoder
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.ui.model.MainViewModel
import kotlinx.coroutines.launch
import kotlin.time.Duration

@Composable
fun AppointmentScreen(viewModel: MainViewModel,
                      onAddLocationPressed: () -> Unit) {

    val scheduledAppointment = viewModel.currentScheduledAppointment.observeAsState()

    val areLocationPermissionsGranted = remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

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
    val appointmentScheduledText = buildAnnotatedString {
        append("Appointment Scheduled on \n")
        appendInlineContent(iconPlaceholderId, "[icon]")
        append("${Utils.convertTimestampToDate(scheduledAppointment.value!!.appointmentDate)}")
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
        append(Duration.parseIsoString(scheduledAppointment.value!!.appointmentDuration).toString())
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
            if (scheduledAppointment.value!!.appointmentPlace.isEmpty()) {
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
                    getAppointmentPlaceText(context , iconPlaceholderId, scheduledAppointment.value!!.appointmentPlace),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    inlineContent = appointmentPlaceInlineText
                )
            }

        }
    }
}

fun getAppointmentPlaceText(context: Context,
                            iconPlaceholderId: String,
                            coordinates: String): AnnotatedString {
    return buildAnnotatedString {
        append("Appointment Place \n")
        appendInlineContent(iconPlaceholderId, "[icon]")
        append(getAddress(context, coordinates))
    }
}

fun getAddress(context: Context, coordinates: String): String {
    if (coordinates.isEmpty()) {
        return ""
    }
    val latitudeAndLongitude = coordinates.split(",")
    val latLng: LatLng = LatLng(latitudeAndLongitude[0].toDouble(), latitudeAndLongitude[1].toDouble())
    val geoCoder: Geocoder = Geocoder(context)
    val address = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
    return address?.get(0)?.getAddressLine(0).toString()
}