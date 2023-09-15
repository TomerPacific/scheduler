package com.tomerpacific.scheduler.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.runtime.*
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
import java.time.LocalDateTime

@Composable
fun AddAppointmentScreen(viewModel: MainViewModel, onAppointmentScheduled: (String?, String?) -> Unit) {

    var currentDate: LocalDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var shouldPreviousDayButtonBeEnabled: Boolean by remember {
        mutableStateOf(calculatePreviousDayButtonEnabledState(currentDate))
    }

    viewModel.getAppointmentsForDay(currentDate)

    val availableAppointments = viewModel.availableAppointments.observeAsState()
    val isWeekend = Utils.isWeekend(currentDate.dayOfWeek)

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)) {
                    Text(text = "Schedule An Appointment For ${Utils.convertLocalDateTimeToDayOfWeek(currentDate)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center)
                }
            }

            if (availableAppointments.value.isNullOrEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = if (isWeekend) {
                                "There are no available appointments on the weekend."
                                } else {
                                "There are no available appointments. Please try again later."
                               },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(availableAppointments.value!!) { appointment ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
                        shape = MaterialTheme.shapes.small,
                        elevation = 10.dp,
                        border = BorderStroke(width = 3.dp, color = Color.Black)) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Icon(modifier = Modifier.padding(start = 5.dp),
                                imageVector = Icons.Default.Timelapse,
                                contentDescription = "Clock Icon")
                            Text(text = Utils.convertTimestampToDate(appointment.appointmentDate).toString(),
                                fontSize = 15.sp)
                            TextButton(onClick = {
                                viewModel.addAppointment(appointment, onAppointmentScheduled)
                            },
                                shape = RoundedCornerShape(50)) {
                                Text("Schedule", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(
                enabled = shouldPreviousDayButtonBeEnabled,
                onClick = {
                    currentDate = LocalDateTime.from(currentDate).minusDays(1)
                    shouldPreviousDayButtonBeEnabled = calculatePreviousDayButtonEnabledState(currentDate)
                    viewModel.getAppointmentsForDay(currentDate)
                },
                shape = RoundedCornerShape(50)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ArrowLeft, contentDescription = "Left Arrow")
                    Text("Previous Day", fontWeight = FontWeight.Bold)
                }

            }

            TextButton(onClick = {
                    currentDate = LocalDateTime.from(currentDate).plusDays(1)
                    shouldPreviousDayButtonBeEnabled = calculatePreviousDayButtonEnabledState(currentDate)
                    viewModel.getAppointmentsForDay(currentDate)
                },
                shape = RoundedCornerShape(50)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Next Day", fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.ArrowRight, contentDescription = "Right Arrow")
                }
            }
        }
    }
}

private fun calculatePreviousDayButtonEnabledState(currentDate: LocalDateTime): Boolean {
    return Utils.getDayAndMonthFromLocalDateTime(currentDate) !=
            Utils.getDayAndMonthFromLocalDateTime(LocalDateTime.now())
}