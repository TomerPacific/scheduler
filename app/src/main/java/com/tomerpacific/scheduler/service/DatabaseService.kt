package com.tomerpacific.scheduler.service

import android.util.Log
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
    private val TAG = DatabaseService::class.java.simpleName

    fun setAppointment(appointment: AppointmentModel,
                       onAppointmentScheduled: (String?, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = database.child(DATES_KEY)
                .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate)).get()
            if (result.isSuccessful) {
                var data = result.result.getValue<HashMap<String, String>>()

                if (data == null) {
                    data =  hashMapOf()
                }

                data[appointment.appointmentDate.toString()] = appointment.userId!!

                database.child(DATES_KEY)
                    .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate))
                    .setValue(data)
            } else {
                result.exception?.localizedMessage?.let { errorMsg ->
                    withContext(Dispatchers.Main) {
                        onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, errorMsg)
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val result = database.child(APPOINTMENTS_KEY).get()
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
            } else {
                result.exception?.localizedMessage?.let { errorMsg ->
                    withContext(Dispatchers.Main) {
                        onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, errorMsg)
                    }
                }
            }
        }
    }

    fun cancelAppointment(appointment: AppointmentModel,
                          onAppointmentCancelled: (String?, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result =  database.child(DATES_KEY)
                .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate)).get()
            if (result.isSuccessful) {
                val scheduledAppointments = result.result.getValue<HashMap<String, String>>()
                if (scheduledAppointments != null && scheduledAppointments.containsKey(appointment.appointmentDate.toString())) {
                    scheduledAppointments.remove(appointment.appointmentDate.toString())
                }
                database.child(DATES_KEY)
                    .child(Utils.convertTimestampToDayAndMonth(appointment.appointmentDate)).setValue(scheduledAppointments)
            } else {
                result.exception?.localizedMessage?.let { errorMsg ->
                    Log.d(TAG, errorMsg)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val userScheduledAppointmentsSnapshot = database.child(APPOINTMENTS_KEY).child(appointment.userId!!).get()
            if (userScheduledAppointmentsSnapshot.isSuccessful) {
                val userScheduledAppointments = userScheduledAppointmentsSnapshot.result.getValue<HashMap<String, AppointmentModel>>()
                if (userScheduledAppointments != null && userScheduledAppointments.containsKey(appointment.appointmentId)) {
                    userScheduledAppointments.remove(appointment.appointmentId)
                }
                val result = database.child(APPOINTMENTS_KEY)
                    .child(appointment.userId!!)
                    .setValue(userScheduledAppointments)
                if (result.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onAppointmentCancelled(APPOINTMENT_ACTION_CANCEL, null)
                    }
                }
            } else {
                userScheduledAppointmentsSnapshot.exception?.localizedMessage?.let { errorMsg ->
                    Log.d(TAG, errorMsg)
                }
            }
        }
    }

    fun getAvailableAppointmentsForDate(viewModel: MainViewModel, date: LocalDateTime) {
        val timestamp = date.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()

        CoroutineScope(Dispatchers.IO).launch {
            val result =  database.child(DATES_KEY)
                .child(Utils.convertTimestampToDayAndMonth(timestamp)).get()
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
            } else {
                result.exception?.localizedMessage?.let { errorMsg ->
                    Log.d(TAG, errorMsg)
                }
            }
        }
    }

    private fun removePastAppointmentsForUser(user: FirebaseUser, pastAppointments: MutableList<AppointmentModel>) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = database.child(DATES_KEY)
                .endAt(Date().time.toString())
                .get()
            if (result.isSuccessful) {
                val scheduledAppointments = result.result.getValue<HashMap<String, HashMap<String, String>>>()
                if (scheduledAppointments != null) {
                    for (date in scheduledAppointments.keys) {
                        scheduledAppointments[date]?.clear()
                    }
                    database.child(DATES_KEY).setValue(scheduledAppointments)
                }
            } else {
                result.exception?.localizedMessage?.let { errorMsg ->
                    Log.d(TAG, errorMsg)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val result = database.child(APPOINTMENTS_KEY).child(user.uid).get()
            if (result.isSuccessful) {
                val scheduledAppointments =
                    result.result.getValue<HashMap<String, AppointmentModel>>()
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
            } else {
                result.exception?.localizedMessage?.let { errorMsg ->
                    Log.d(TAG, errorMsg)
                }
            }
        }
    }

    fun updateAppointmentForUser(user: FirebaseUser, appointment:AppointmentModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = database.child(APPOINTMENTS_KEY).child(user.uid).get()
            if (result.isSuccessful) {
                val scheduledAppointments =
                    result.result.getValue<HashMap<String, AppointmentModel>>()
                if (scheduledAppointments != null && scheduledAppointments.containsKey(appointment.appointmentId)) {
                    scheduledAppointments[appointment.appointmentId] = appointment
                }
                database.child(APPOINTMENTS_KEY)
                    .child(user.uid)
                    .setValue(scheduledAppointments)
            } else {
                result.exception?.localizedMessage?.let { errorMsg ->
                    Log.d(TAG, errorMsg)
                }
            }
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
            val result = database.child(APPOINTMENTS_KEY).get()
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