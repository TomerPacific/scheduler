package com.tomerpacific.scheduler.service

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tomerpacific.scheduler.APPOINTMENT_ACTION_SCHEDULE
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.ui.model.AppointmentModel
import com.tomerpacific.scheduler.ui.model.MainViewModel
import java.util.*
import kotlin.collections.HashMap

class DatabaseService() {

    private val database = Firebase.database.reference
    private val APPOINTMENTS_KEY = "appointments"
    private val DATES_KEY = "dates"

    fun setAppointment(appointment: AppointmentModel,
                       onAppointmentScheduled: (String?, String?) -> Unit) {
        database.child(DATES_KEY)
            .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate)).get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    var data = result.result.getValue<HashMap<String, List<String>>>()

                    if (data == null) {
                        data =  hashMapOf()
                    }

                    if (data.containsKey(appointment.userId!!)) {
                        val appointments = data.get(appointment.userId!!)?.toMutableList()
                        appointments?.add(appointment.appointmentId)
                        data.put(appointment.userId!!, appointments!!)
                    } else {
                        data[appointment.userId!!] = listOf(appointment.appointmentId)
                    }

                    database.child(DATES_KEY)
                        .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate))
                        .setValue(data)
                    onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, null)
                }
            }
            .addOnFailureListener { error ->
                onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, error.message)
            }
        database.child(APPOINTMENTS_KEY).get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {

                    val appointments = when(val data: HashMap<String, AppointmentModel>? = result.result.getValue<HashMap<String, AppointmentModel>>()) {
                        null -> HashMap()
                        else -> data
                    }

                    appointments[appointment.appointmentId] = appointment
                    database.child(APPOINTMENTS_KEY).setValue(appointments).addOnCompleteListener {
                    }
                }
            }
            .addOnFailureListener { error ->
                onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, error.message)
            }
    }

    fun cancelAppointment(user: FirebaseUser,
                          appointment: AppointmentModel,
                          onAppointmentCancelled: (String?, String?) -> Unit) {

    }

    fun getAvailableAppointmentsForDate(viewModel: MainViewModel, date: Date) {

    }

    private fun removePastAppointmentsForUser(user: FirebaseUser, pastAppointments: MutableList<AppointmentModel>) {

    }

    private fun createAppointmentsForDay(scheduledAppointments: List<AppointmentModel>): MutableList<AppointmentModel> {
        val appointments: MutableList<AppointmentModel> = mutableListOf()
        val startDate = Utils.createStartDateForAppointmentsOfDay()

        var startHour = 10
        if (startDate.hours >= 19) {
            return appointments
        } else if (startDate.hours > 10 && startDate.hours < 19) {
            startHour = startDate.hours
        }

        for (i in startHour..19) {
            val appointment = AppointmentModel(
                Utils.truncateTimestamp(startDate.time),
                "",
                "one hour",
            null)

            val appointmentExists = scheduledAppointments.filter { scheduledAppointment ->
                val scheduledAppointmentDate = scheduledAppointment.appointmentDate
                appointment.appointmentDate == scheduledAppointmentDate
            }

            if (appointmentExists.isEmpty()) {
                appointments.add(appointment)
            }

            startDate.hours += 1
        }

        return appointments
    }

    fun fetchScheduledAppointmentsForUser(user: FirebaseUser, viewModel: MainViewModel) {

    }
}