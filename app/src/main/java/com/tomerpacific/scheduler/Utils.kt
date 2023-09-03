package com.tomerpacific.scheduler

import com.tomerpacific.scheduler.ui.model.AppointmentModel
import java.time.DayOfWeek
import java.time.LocalDateTime
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
            true ->  dateToStart.withHour(START_HOUR_FOR_APPOINTMENTS).withMinute(0).withSecond(0)
            false -> dateToStart.plusHours(1).withMinute(0).withSecond(0)
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

    fun isWeekend(day: DayOfWeek? = null): Boolean {
        val dayToCheck = when (day) {
            null -> LocalDateTime.now().dayOfWeek
            else -> day
        }

        return dayToCheck == DayOfWeek.SATURDAY || dayToCheck == DayOfWeek.SUNDAY
    }

}