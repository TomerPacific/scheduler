package com.tomerpacific.scheduler.service

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tomerpacific.scheduler.*
import com.tomerpacific.scheduler.ui.model.AppointmentModel
import com.tomerpacific.scheduler.ui.model.MainViewModel
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.HashMap

class DatabaseService(_remoteConfigService: RemoteConfigService) {

    private val database = Firebase.database.reference
    private val remoteConfigService = _remoteConfigService
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
                    if (scheduledAppointments != null && scheduledAppointments.containsKey(appointment.appointmentDate.toString())) {
                            scheduledAppointments.remove(appointment.appointmentDate.toString())
                    }
                    database.child(DATES_KEY)
                        .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate)).setValue(scheduledAppointments)
                        .addOnCompleteListener {

                        }.addOnFailureListener {

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

    fun getAvailableAppointmentsForDate(viewModel: MainViewModel, date: LocalDateTime) {
        val timestamp = date.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()
        database.child(DATES_KEY)
            .child(Utils.convertTimestampToDayAndMonth(timestamp)).get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val scheduledAppointments = mutableListOf<Long>()
                    val appointmentDates = result.result.getValue<HashMap<String, String>>()
                    if (appointmentDates != null) {

                        for (appointmentTime in appointmentDates.keys) {
                            scheduledAppointments.add(appointmentTime.toLong())
                        }
                    }

                    val availableAppointments = createAppointmentsForDay(scheduledAppointments, date)
                    viewModel.setAvailableAppointments(availableAppointments)
                }
            }
            .addOnFailureListener { error ->
                print(error.localizedMessage)
            }

    }

    private fun removePastAppointmentsForUser(user: FirebaseUser, pastAppointments: MutableList<AppointmentModel>) {

        database.child(DATES_KEY)
            .endAt(Date().time.toString())
            .get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val scheduledAppointments = result.result.getValue<HashMap<String, HashMap<String, String>>>()
                    if (scheduledAppointments != null) {
                        for (date in scheduledAppointments.keys) {
                            scheduledAppointments[date]?.clear()
                        }
                        database.child(DATES_KEY).setValue(scheduledAppointments)
                    }
                }
            }.addOnFailureListener {

            }
        database.child(APPOINTMENTS_KEY).child(user.uid).get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val scheduledAppointments = result.result.getValue<HashMap<String, AppointmentModel>>()
                    if (scheduledAppointments != null) {
                        for (appointment in pastAppointments) {
                            if (scheduledAppointments.containsKey(appointment.appointmentId)) {
                                scheduledAppointments.remove(appointment.appointmentId)
                            }
                        }
                    }
                    database.child(APPOINTMENTS_KEY)
                        .child(user.uid)
                        .setValue(scheduledAppointments)
                        .addOnCompleteListener {

                        }
                        .addOnFailureListener {

                        }
                }
            }
    }

    private fun createAppointmentsForDay(scheduledAppointments: List<Long>, date: LocalDateTime): MutableList<AppointmentModel> {
        val appointments: MutableList<AppointmentModel> = mutableListOf()

        //There can be no appointments available on the weekend
        if (Utils.isWeekend()) {
            return appointments
        }

        var startDate = Utils.createStartDateForAppointmentsOfDay(dateToStart = date)
        val startAndEndTimeByDay = remoteConfigService.getAppointmentStartAndEndTimeByDay(startDate)
        var startHour = startAndEndTimeByDay[0]
        val endHour = startAndEndTimeByDay[1]

        if (startDate.dayOfMonth == LocalDateTime.now().dayOfMonth) {
            if (startDate.hour >= endHour) {
                return appointments
            } else if (startDate.hour in (startHour + 1) until endHour) {
                startHour = startDate.hour
            }
        }

        for (i in startHour..endHour) {
            val appointment = AppointmentModel(
                Utils.truncateTimestamp(startDate.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()),
                "",
                "one hour",
            null)

            val appointmentExists = scheduledAppointments.filter { scheduledAppointment ->
                appointment.appointmentDate == scheduledAppointment
            }

            if (appointmentExists.isEmpty()) {
                appointments.add(appointment)
            }

            startDate = startDate.plusHours(1)
        }

        return appointments
    }

    fun fetchScheduledAppointmentsForUser(user: FirebaseUser, viewModel: MainViewModel, isAdmin: Boolean) {

        database.child(APPOINTMENTS_KEY).get()
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val scheduledAppointments = result.result.getValue<HashMap<String, HashMap<String, AppointmentModel>>>()
                    if (scheduledAppointments != null) {
                        when (isAdmin) {
                            true -> collectScheduledAppointmentsForAdminUser(scheduledAppointments, viewModel)
                            false -> collectScheduledAppointmentsForRegularUser(
                            scheduledAppointments,
                            user,
                            viewModel)
                        }
                    } else {
                        viewModel.setScheduledAppointments(scheduledAppointments = listOf())
                    }
                }
            }
        }

    private fun collectScheduledAppointmentsForAdminUser(scheduledAppointments: HashMap<String, HashMap<String, AppointmentModel>>,
                                                         viewModel: MainViewModel) {
        val appointments = mutableListOf<AppointmentModel>()
        for (userId in scheduledAppointments.keys) {
            val userAppointments: HashMap<String, AppointmentModel> = scheduledAppointments[userId]!!
                for (userAppointment in userAppointments.keys) {
                    val appointment = userAppointments[userAppointment]!!
                    if (!Utils.isAppointmentDatePassed(appointment)) {
                        appointments.add(appointment)
                    }
                }
        }
        viewModel.setScheduledAppointments(scheduledAppointments = appointments)
    }

    private fun collectScheduledAppointmentsForRegularUser(scheduledAppointments: HashMap<String, HashMap<String, AppointmentModel>>,
                                                           user: FirebaseUser,
                                                           viewModel: MainViewModel) {
        val pastAppointments = mutableListOf<AppointmentModel>()
        val userAppointments = scheduledAppointments[user.uid]
        val appointments = mutableListOf<AppointmentModel>()
        if (userAppointments != null) {
            for (userAppointment in userAppointments.keys) {
                val appointment = userAppointments[userAppointment]!!
                if (!Utils.isAppointmentDatePassed(appointment)) {
                    appointments.add(appointment)
                } else {
                    pastAppointments.add(appointment)
                }
            }
            viewModel.setScheduledAppointments(scheduledAppointments = appointments)
            if (pastAppointments.isNotEmpty()) {
                removePastAppointmentsForUser(user, pastAppointments)
            }
        }
    }
}