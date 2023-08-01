package com.tomerpacific.scheduler.ui.model

import com.tomerpacific.scheduler.REMOTE_CONFIG_MONDAY_KEY
import com.tomerpacific.scheduler.REMOTE_CONFIG_SUNDAY_KEY
import com.tomerpacific.scheduler.REMOTE_CONFIG_THURSDAY_KEY
import com.tomerpacific.scheduler.REMOTE_CONFIG_TUESDAY_KEY
import com.tomerpacific.scheduler.REMOTE_CONFIG_WEDNESDAY_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentStartAndEndTimesModel(
    @SerialName(REMOTE_CONFIG_SUNDAY_KEY)
    val sunday: List<Int>,
    @SerialName(REMOTE_CONFIG_MONDAY_KEY)
    val monday: List<Int>,
    @SerialName(REMOTE_CONFIG_TUESDAY_KEY)
    val tuesday: List<Int>,
    @SerialName(REMOTE_CONFIG_WEDNESDAY_KEY)
    val wednesday: List<Int>,
    @SerialName(REMOTE_CONFIG_THURSDAY_KEY)
    val thursday: List<Int>
)
