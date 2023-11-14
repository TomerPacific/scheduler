package com.tomerpacific.scheduler.service

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tomerpacific.scheduler.*
import com.tomerpacific.scheduler.ui.model.AppointmentModel
import com.tomerpacific.scheduler.ui.model.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.HashMap
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class DatabaseService(_remoteConfigService: RemoteConfigService) {

    private val database = Firebase.database.reference
    private val remoteConfigService = _remoteConfigService
    private val APPOINTMENTS_KEY = "appointments"
    private val DATES_KEY = "dates"

    fun setAppointment(appointment: AppointmentModel,
                       onAppointmentScheduled: (String?, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataSnapshot = database.child(DATES_KEY)
                .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate)).get()
                .await()
            var data = dataSnapshot.getValue<HashMap<String, String>>()

            if (data == null) {
                data = hashMapOf()
            }

            data[appointment.appointmentDate.toString()] = appointment.userId!!

            database.child(DATES_KEY)
                .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate))
                .setValue(data)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val dataSnapshot = database.child(APPOINTMENTS_KEY).get().await()
            val allAppointments =
                when (val data: HashMap<String, HashMap<String, AppointmentModel>>? =
                    dataSnapshot.getValue<HashMap<String, HashMap<String, AppointmentModel>>>()) {
                    null -> HashMap()
                    else -> data
                }

            val scheduledAppointmentsForUser = allAppointments[appointment.userId!!]
            val appointments = when (scheduledAppointmentsForUser.isNullOrEmpty()) {
                true -> hashMapOf<String, AppointmentModel>()
                false -> scheduledAppointmentsForUser
            }

            appointments[appointment.appointmentId] = appointment
            allAppointments[appointment.userId!!] = appointments
            database.child(APPOINTMENTS_KEY).setValue(allAppointments).await().apply {
                withContext(Dispatchers.Main) {
                    onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, null)
                    }
                }
            }
    }

    fun cancelAppointment(appointment: AppointmentModel,
                          onAppointmentCancelled: (String?, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataSnapshot =  database.child(DATES_KEY)
                .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate)).get().await()
                val scheduledAppointments = dataSnapshot.getValue<HashMap<String, String>>()
                if (scheduledAppointments != null && scheduledAppointments.containsKey(appointment.appointmentDate.toString())) {
                    scheduledAppointments.remove(appointment.appointmentDate.toString())
                }
                database.child(DATES_KEY)
                    .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate)).setValue(scheduledAppointments)
            }

        CoroutineScope(Dispatchers.IO).launch {
            val userScheduledAppointmentsSnapshot = database.child(APPOINTMENTS_KEY)
                .child(appointment.userId!!)
                .get()
                .await()

                val userScheduledAppointments = userScheduledAppointmentsSnapshot.getValue<HashMap<String, AppointmentModel>>()
                if (userScheduledAppointments != null && userScheduledAppointments.containsKey(appointment.appointmentId)) {
                    userScheduledAppointments.remove(appointment.appointmentId)
                }

                database.child(APPOINTMENTS_KEY)
                    .child(appointment.userId!!)
                    .setValue(userScheduledAppointments).await()

                withContext(Dispatchers.Main) {
                    onAppointmentCancelled(APPOINTMENT_ACTION_CANCEL, null)
                }
            }
    }

    fun getAvailableAppointmentsForDate(viewModel: MainViewModel, date: LocalDateTime) {
        val timestamp = date.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()
        val scheduledAppointments = mutableListOf<Long>()

        CoroutineScope(Dispatchers.IO).launch {
            val result =  database.child(DATES_KEY)
                .child(Utils.convertTimestampToDayAndMonth(timestamp)).get().await()
            if (result.exists()) {
                val appointmentDates = result.getValue<HashMap<String, String>>()
                if (appointmentDates != null) {
                    for (appointmentTime in appointmentDates.keys) {
                        scheduledAppointments.add(appointmentTime.toLong())
                    }
                }
            }

            val availableAppointments = createAppointmentsForDay(scheduledAppointments, date)
            viewModel.setAvailableAppointments(availableAppointments)
        }
    }

    private fun removePastAppointmentsForUser(user: FirebaseUser, pastAppointments: MutableList<AppointmentModel>) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataSnapshot = database.child(DATES_KEY)
            .endAt(Date().time.toString())
            .get().await()
            val scheduledAppointments = dataSnapshot.getValue<HashMap<String, HashMap<String, String>>>()
            if (scheduledAppointments != null) {
                for (date in scheduledAppointments.keys) {
                    scheduledAppointments[date]?.clear()
                }
                database.child(DATES_KEY).setValue(scheduledAppointments)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val dataSnapshot = database.child(APPOINTMENTS_KEY).child(user.uid).get().await()
            val scheduledAppointments =
                dataSnapshot.getValue<HashMap<String, AppointmentModel>>()
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
        }
    }

    fun updateAppointmentForUser(user: FirebaseUser, appointment:AppointmentModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataSnapshot = database.child(APPOINTMENTS_KEY).child(user.uid).get().await()
            val scheduledAppointments =
                dataSnapshot.getValue<HashMap<String, AppointmentModel>>()
            if (scheduledAppointments != null && scheduledAppointments.containsKey(appointment.appointmentId)) {
                scheduledAppointments[appointment.appointmentId] = appointment
            }
            database.child(APPOINTMENTS_KEY)
                .child(user.uid)
                .setValue(scheduledAppointments)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun createAppointmentsForDay(scheduledAppointments: List<Long>, date: LocalDateTime): MutableList<AppointmentModel> {
        val appointments: MutableList<AppointmentModel> = mutableListOf()

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
                Duration.hours(1).toIsoString(),
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

        CoroutineScope(Dispatchers.IO).launch {
            val dataSnapshot = database.child(APPOINTMENTS_KEY).get().await()
            val scheduledAppointments = dataSnapshot.getValue<HashMap<String, HashMap<String, AppointmentModel>>>()
            if (scheduledAppointments != null) {
                when (isAdmin) {
                    true -> collectScheduledAppointmentsForAdminUser(scheduledAppointments, viewModel)
                    false -> collectScheduledAppointmentsForRegularUser(
                        scheduledAppointments,
                        user,
                        viewModel)
                }
            } else {
                viewModel.setScheduledAppointments(listOf())
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

        userAppointments?.let {
            for (userAppointment in it.keys) {
                val appointment = it[userAppointment]!!

                when (Utils.isAppointmentDatePassed(appointment)) {
                    true -> pastAppointments.add(appointment)
                    false -> appointments.add(appointment)
                }
            }

            viewModel.setScheduledAppointments(scheduledAppointments = appointments)
            if (pastAppointments.isNotEmpty()) {
                removePastAppointmentsForUser(user, pastAppointments)
            }
        }
    }
}