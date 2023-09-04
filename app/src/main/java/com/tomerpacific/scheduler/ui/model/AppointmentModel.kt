package com.tomerpacific.scheduler.ui.model

import kotlinx.serialization.Serializable
import java.util.*
import kotlin.time.Duration

@Serializable
data class AppointmentModel(
    val appointmentDate: Long,
    var appointmentPlace: String,
    val appointmentDuration: String,
    var userId: String?,
    val appointmentId: String = generateId()
) {
    companion object {

        private fun generateId(): String {
            return UUID.randomUUID().toString()
        }
    }

    constructor() : this(0, "", Duration.ZERO.toIsoString(), "")
}
