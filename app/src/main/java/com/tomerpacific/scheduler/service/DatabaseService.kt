package com.tomerpacific.scheduler.service

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tomerpacific.scheduler.APPOINTMENT_ACTION_CANCEL
import com.tomerpacific.scheduler.APPOINTMENT_ACTION_SCHEDULE
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.ui.model.AppointmentModel
import com.tomerpacific.scheduler.ui.model.MainViewModel

class DatabaseService() {

    private val DATABASE_USERS_KEY = "users"
    private val DATABASE_APPOINTMENT_DATE_KEY = "appointmentDate"
    private val database = Firebase.database.reference

    fun setAppointment(user:FirebaseUser,
                       appointment: AppointmentModel,
                       onAppointmentScheduled: (String?, String?) -> Unit) {
        database.child(DATABASE_USERS_KEY).child(user.uid).push()
            .setValue(appointment)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, null)
                }
            }
            .addOnFailureListener { error ->
                onAppointmentScheduled(APPOINTMENT_ACTION_SCHEDULE, error.message)
            }
    }

    fun cancelAppointment(user: FirebaseUser,
                          appointment: AppointmentModel,
                          onAppointmentCancelled: (String?, String?) -> Unit) {
        database.child(DATABASE_USERS_KEY).child(user.uid).get()
            .addOnCompleteListener { datasnapshot ->
                if (datasnapshot.isSuccessful) {
                    val children = datasnapshot.result.children
                        children.forEach { userAppointment ->
                            userAppointment.getValue<AppointmentModel>()?.let { scheduledAppointment ->
                                if (scheduledAppointment.appointmentId == appointment.appointmentId) {
                                    userAppointment.ref.removeValue()
                                        .addOnCompleteListener {
                                            onAppointmentCancelled(APPOINTMENT_ACTION_CANCEL, null)
                                        }
                                        .addOnFailureListener { error ->
                                            onAppointmentCancelled(APPOINTMENT_ACTION_CANCEL, error.message)
                                        }
                                }
                            }
                        }
                 }
            }
            .addOnFailureListener { error ->
                onAppointmentCancelled(APPOINTMENT_ACTION_CANCEL, error.message)
            }
    }

    fun getAvailableAppointmentsForToday(viewModel: MainViewModel) {
        database.orderByChild(DATABASE_APPOINTMENT_DATE_KEY).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val scheduledAppointments = mutableListOf<AppointmentModel>()
                    val children = dataSnapshot.child(DATABASE_USERS_KEY).children
                    children.forEach { userAppointments ->
                        userAppointments.children.forEach {
                            it.getValue<AppointmentModel>()?.let { appointment ->
                                scheduledAppointments.add(appointment)
                            }
                        }
                    }
                    val availableAppointments = createAppointmentsForDay(scheduledAppointments.toList())
                    viewModel.setAvailableAppointments(availableAppointments)
                } else {
                    val availableAppointments = createAppointmentsForDay(listOf())
                    viewModel.setAvailableAppointments(availableAppointments)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun removePastAppointmentsForUser(user: FirebaseUser, pastAppointments: MutableList<AppointmentModel>) {
        database.child(DATABASE_USERS_KEY).child(user.uid).get()
            .addOnCompleteListener { datasnapshot ->
                if (datasnapshot.isSuccessful) {
                    val children = datasnapshot.result.children
                    children.forEach { userAppointment ->
                        userAppointment.getValue<AppointmentModel>()?.let { scheduledAppointment ->
                            if (pastAppointments.contains(scheduledAppointment)) {
                                userAppointment.ref.removeValue()
                                pastAppointments.remove(scheduledAppointment)
                            }
                        }
                    }
                }
            }
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
                "one hour")

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
        val appointments = mutableListOf<AppointmentModel>()
        val pastAppointments = mutableListOf<AppointmentModel>()

        database.child(DATABASE_USERS_KEY).child(user.uid).get()
            .addOnCompleteListener { query ->
                if (query.isSuccessful) {
                    val children = query.result.children
                    children.forEach {
                        it.getValue<AppointmentModel>()?.let { appointment ->
                            if (!Utils.isAppointmentDatePassed(appointment)) {
                                appointments.add(appointment)
                            } else {
                                pastAppointments.add(appointment)
                            }
                        }
                    }
                    viewModel.setScheduledAppointments(appointments.toList())
                    if (pastAppointments.isNotEmpty()) {
                        removePastAppointmentsForUser(user, pastAppointments)
                    }
                }
            }
            .addOnFailureListener { error ->

            }
        }
}