package com.tomerpacific.scheduler.service

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tomerpacific.scheduler.APPOINTMENT_ACTION_CANCEL
import com.tomerpacific.scheduler.APPOINTMENT_ACTION_SCHEDULE
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.ui.model.AppointmentModel
import com.tomerpacific.scheduler.ui.model.MainViewModel
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
                    var data = result.result.getValue<HashMap<String, String>>()

                    if (data == null) {
                        data =  hashMapOf()
                    }

                    data[appointment.appointmentDate.toString()] = appointment.userId!!

                    database.child(DATES_KEY)
                        .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate))
                        .setValue(data)
                }
            }
            .addOnFailureListener { error ->
                onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, error.message)
            }
        database.child(APPOINTMENTS_KEY).get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {

                    val appointments = when(val data: HashMap<String, HashMap<String, AppointmentModel>>? = result.result.getValue<HashMap<String, HashMap<String, AppointmentModel>>>()) {
                        null -> HashMap()
                        else -> data
                    }

                    val scheduledAppointments = appointments[appointment.userId!!]
                    val aps = when (scheduledAppointments.isNullOrEmpty()) {
                        true -> hashMapOf<String, AppointmentModel>()
                        false -> scheduledAppointments
                    }

                    aps[appointment.appointmentId] = appointment
                    appointments[appointment.userId!!] = aps
                    database.child(APPOINTMENTS_KEY).setValue(appointments).addOnCompleteListener {
                        onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, null)
                    }
                }
            }
            .addOnFailureListener { error ->
                onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, error.message)
            }
    }

    fun cancelAppointment(appointment: AppointmentModel,
                          onAppointmentCancelled: (String?, String?) -> Unit) {
        database.child(DATES_KEY)
            .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate)).get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val scheduledAppointments = result.result.getValue<HashMap<String, String>>()
                    if (scheduledAppointments != null) {
                        for (appointmentTime in scheduledAppointments.keys) {
                            if (appointmentTime.toLong() == appointment.appointmentDate) {
                                scheduledAppointments.remove(appointmentTime)
                            }
                        }
                        database.child(DATES_KEY)
                            .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate)).setValue(scheduledAppointments)
                            .addOnCompleteListener {

                            }.addOnFailureListener {

                            }
                    }
                }
            }
        database.child(APPOINTMENTS_KEY).child(appointment.userId!!).get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val userScheduledAppointments = result.result.getValue<HashMap<String, AppointmentModel>>()
                    if (userScheduledAppointments != null && userScheduledAppointments.containsKey(appointment.appointmentId)) {
                        userScheduledAppointments.remove(appointment.appointmentId)
                        database.child(APPOINTMENTS_KEY)
                            .child(appointment.userId!!)
                            .setValue(userScheduledAppointments)
                            .addOnCompleteListener {
                                onAppointmentCancelled(APPOINTMENT_ACTION_CANCEL, null)
                            }
                    }
                }
            }
    }

    fun getAvailableAppointmentsForDate(viewModel: MainViewModel, date: Long) {
        database.child(DATES_KEY)
            .child(Utils.convertTimestampToDayAndMonth(date)).get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val scheduledAppointments = mutableListOf<Long>()
                    val appointmentDates = result.result.getValue<HashMap<String, String>>()
                    if (appointmentDates != null) {

                        for (appointmentTime in appointmentDates.keys) {
                            scheduledAppointments.add(appointmentTime.toLong())
                        }
                    }

                    val availableAppointments = createAppointmentsForDay(scheduledAppointments)
                    viewModel.setAvailableAppointments(availableAppointments)
                }
            }

    }

    private fun removePastAppointmentsForUser(user: FirebaseUser, pastAppointments: MutableList<AppointmentModel>) {

    }

    private fun createAppointmentsForDay(scheduledAppointments: List<Long>): MutableList<AppointmentModel> {
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
                appointment.appointmentDate == scheduledAppointment
            }

            if (appointmentExists.isEmpty()) {
                appointments.add(appointment)
            }

            startDate.hours += 1
        }

        return appointments
    }

    fun fetchScheduledAppointmentsForUser(user: FirebaseUser, viewModel: MainViewModel) {
        database.child(APPOINTMENTS_KEY).get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val scheduledAppointments = result.result.getValue<HashMap<String, HashMap<String, AppointmentModel>>>()
                    if (scheduledAppointments != null) {
                        val userAppointments = scheduledAppointments[user.uid]
                        val appointments = mutableListOf<AppointmentModel>()
                        if (userAppointments != null) {
                            for (useAppointment in userAppointments.keys) {
                                appointments.add(userAppointments[useAppointment]!!)
                            }
                            viewModel.setScheduledAppointments(scheduledAppointments = appointments)
                        }
                    } else {
                        viewModel.setScheduledAppointments(scheduledAppointments = listOf())
                    }
                }
            }
    }
}