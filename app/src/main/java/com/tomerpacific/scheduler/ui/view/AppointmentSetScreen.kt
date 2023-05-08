package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tomerpacific.scheduler.R

@Composable
fun AppointmentSetScreen(appointmentAction: String?,
                         errorMsg: String?,
                         onBackToAppointmentScreenPressed: () -> Unit) {

    val wasAppointmentActionCompletedSuccessfully: Boolean = when(errorMsg) {
        "null" -> true
        else -> false
    }

    val titleText:String = getAppointmentSetScreenTitle(appointmentAction, wasAppointmentActionCompletedSuccessfully)
    
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.check_mark_success))
    val progress by animateLottieCompositionAsState(composition)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text(titleText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
        if (errorMsg == "null") {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                )
            }
        } else if (!errorMsg.isNullOrEmpty()){
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