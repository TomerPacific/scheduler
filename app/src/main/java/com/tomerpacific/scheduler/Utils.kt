package com.tomerpacific.scheduler

import com.tomerpacific.scheduler.ui.model.AppointmentModel
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
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

    fun createStartDateForAppointmentsOfDay(dateToStart: LocalDateTime): LocalDateTime {
        return when (dateToStart.dayOfMonth != LocalDateTime.now().dayOfMonth) {
            true -> dateToStart.with {
                LocalTime.of(START_HOUR_FOR_APPOINTMENTS, 0)
            }
            false -> dateToStart.with {
                LocalTime.of(dateToStart.hour + 1, 0)
            }
        }
    }

    fun isAppointmentDatePassed(appointment: AppointmentModel): Boolean {
        val appointmentDate = convertTimestampToDate(appointment.appointmentDate)
        val currentDate = Date()
        return appointmentDate.before(currentDate)
    }

    fun getDayAndMonthFromLocalDateTime(date: LocalDateTime): String {
        return date.month.toString() + date.dayOfMonth
    }

    fun isWeekend(): Boolean {
        val today = LocalDateTime.now()
        return today.dayOfWeek == DayOfWeek.SATURDAY || today.dayOfWeek == DayOfWeek.SUNDAY
    }

}