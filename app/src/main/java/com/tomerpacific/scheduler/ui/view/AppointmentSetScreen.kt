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
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun AppointmentSetScreen(viewModel: MainViewModel,
                         errorMsg: String?,
                         onBackToAppointmentScreenPressed: () -> Unit) {

    val wasAppointmentSetSuccessfully: Boolean = when(errorMsg) {
        "null" -> true
        else -> false
    }

    val titleText:String = if (wasAppointmentSetSuccessfully) "Appointment Set!" else "Appointment Not Set!"
    val iconImageVector: ImageVector = if (wasAppointmentSetSuccessfully) Icons.Default.Check else Icons.Default.Error
    val iconContentDescription: String = if (wasAppointmentSetSuccessfully) "Check Mark" else "Error"

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 20.dp),
        verticalArrangement = Arrangement.Top,
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
        Spacer(modifier = Modifier.height(50.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = {
                onBackToAppointmentScreenPressed()
            }) {
                Text("Back To Appointments")
            }
        }

    }
}