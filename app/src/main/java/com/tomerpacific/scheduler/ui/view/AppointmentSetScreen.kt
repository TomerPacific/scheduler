package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomerpacific.scheduler.ui.model.MainViewModel

@Composable
fun AppointmentSetScreen(viewModel: MainViewModel, errorMsg: String?) {

    val wasAppointmentSetSuccessfully: Boolean = when(errorMsg) {
        "null" -> true
        else -> false
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text("Appointment Set!", fontSize = 25.sp)
        }
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            if (wasAppointmentSetSuccessfully) {
                Icon(imageVector = Icons.Default.Check,
                    modifier = Modifier.size(50.dp),
                    contentDescription = "Check Mark",
                    tint = Color.Green)
            } else {
                Icon(imageVector = Icons.Default.WrongLocation,
                    modifier = Modifier.size(50.dp),
                    contentDescription = "Check Mark",
                    tint = Color.Red)
            }

        }
    }
}