package com.tomerpacific.scheduler.ui.model

import java.util.*

data class AppointmentModel(
    val appointmentDate: Long,
    val appointmentPlace: String,
    val appointmentDuration: String,
    val appointmentId: String = generateId()
) {
    companion object {

        private fun generateId(): String {
            return UUID.randomUUID().toString()
        }
    }

    constructor() : this(0, "", "")
}
