package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppointmentSetScreen(appointmentAction: String?,
                         errorMsg: String?,
                         onBackToAppointmentScreenPressed: () -> Unit) {

    val wasAppointmentActionCompletedSuccessfully: Boolean = when(errorMsg) {
        "null" -> true
        else -> false
    }

    val titleText:String = getAppointmentSetScreenTitle(appointmentAction, wasAppointmentActionCompletedSuccessfully)
    val iconImageVector: ImageVector = if (wasAppointmentActionCompletedSuccessfully) Icons.Default.Check else Icons.Default.Error
    val iconContentDescription: String = if (wasAppointmentActionCompletedSuccessfully) "Check Mark" else "Error"

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text(titleText, fontSize = 25.sp)
        }
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
                Icon(imageVector = iconImageVector,
                    modifier = Modifier.size(50.dp),
                    contentDescription = iconContentDescription,
                    tint = Color.Green)
        }
        if (!errorMsg.isNullOrEmpty() && errorMsg != "null") {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Text(errorMsg, fontSize = 25.sp)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().weight(1f, false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = {
                onBackToAppointmentScreenPressed()
            }) {
                Text("Back To Appointments", fontSize = 20.sp)
            }
        }

    }
}

private fun getAppointmentSetScreenTitle(appointmentAction: String?,
                                         wasAppointmentActionCompletedSuccessfully: Boolean): String {
    appointmentAction?.let { action ->
        return when(action) {
            "schedule" -> if (wasAppointmentActionCompletedSuccessfully) "Appointment Set!" else "Appointment Not Set!"
            "cancel" -> if (wasAppointmentActionCompletedSuccessfully) "Appointment Cancelled!" else "Appointment Not Cancelled!"
            else -> ""
        }
    }

    return ""
}