package com.tomerpacific.scheduler

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentStartAndEndTimesModel(
    @SerialName("SUNDAY")
    val sunday: List<Int>,
    @SerialName("MONDAY")
    val monday: List<Int>,
    @SerialName("TUESDAY")
    val tuesday: List<Int>,
    @SerialName("WEDNESDAY")
    val wednesday: List<Int>,
    @SerialName("THURSDAY")
    val thursday: List<Int>
)
