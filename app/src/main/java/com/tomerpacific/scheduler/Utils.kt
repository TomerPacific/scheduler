package com.tomerpacific.scheduler

import com.tomerpacific.scheduler.ui.model.AppointmentModel
import java.security.Timestamp
import java.time.ZoneId
import java.util.*
import kotlin.math.floor

object Utils {

    fun convertTimestampToDate(timestamp: Long): Date {
        return Date(timestamp)
    }

    fun convertTimestampToDayAndMonth(timestamp: Long): String {
        val date = convertTimestampToDate(timestamp)
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        return "${localDate.dayOfMonth}-${localDate.month}"
    }

    fun truncateTimestamp(timestamp: Long): Long {
        val t = timestamp.toDouble() / 1000L
        val floored = floor(t) * 1000L
        return floored.toLong()
    }

    fun createStartDateForAppointmentsOfDay(): Date {
        return Date().apply {
            hours = hours + 1
            minutes = 0
            seconds = 0
        }
    }

    fun isAppointmentDatePassed(appointment: AppointmentModel): Boolean {
        val appointmentDate = convertTimestampToDate(appointment.appointmentDate)
        val currentDate = Date()
        return appointmentDate.before(currentDate)
    }

    fun isAppointmentDateInDay(appointmentDate: Long, day: Int): Boolean {
        val appointmentDate = convertTimestampToDate(appointmentDate)
        return appointmentDate.day == day
    }

}