package com.tomerpacific.scheduler.service

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.ui.model.AppointmentModel
import com.tomerpacific.scheduler.ui.model.MainViewModel

class DatabaseService() {

    private val DATABASE_USERS_KEY = "users"
    private val DATABASE_APPOINTMENT_DATE_KEY = "appointmentDate"
    private val database = Firebase.database.reference

    fun setAppointment(user:FirebaseUser,
                       appointment: AppointmentModel,
                       onAppointmentScheduled: (String?) -> Unit) {
        database.child(DATABASE_USERS_KEY).child(user.uid).push()
            .setValue(appointment)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    onAppointmentScheduled(null)
                }
            }
            .addOnFailureListener { error ->
                onAppointmentScheduled(error.message)
            }
    }

    fun getAvailableAppointmentsForToday(viewModel: MainViewModel) {
        database.orderByChild(DATABASE_APPOINTMENT_DATE_KEY).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val scheduledAppointments = mutableListOf<AppointmentModel>()
                    val children = dataSnapshot.child(DATABASE_USERS_KEY).children
                    children.forEach {
                        it.getValue<AppointmentModel>()?.let { appointment ->
                            scheduledAppointments.add(appointment)
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

    private fun createAppointmentsForDay(scheduledAppointments: List<AppointmentModel>): MutableList<AppointmentModel> {
        val appointments: MutableList<AppointmentModel> = mutableListOf()
        val startDate = Utils.createStartDateForAppointmentsOfDay()

        for (i in 10..19) {
            val appointment = AppointmentModel(
                Utils.truncateTimestamp(startDate.time),
                "",
                "one hour")

            val appointmentExists = scheduledAppointments.filter { scheduledAppointment ->
                val appDate = scheduledAppointment.appointmentDate
                appointment.appointmentDate == appDate
            }

            if (appointmentExists.isEmpty()) {
                appointments.add(appointment)
            }

            startDate.hours += 1
        }

        return appointments
    }

    fun getAppointmentsForUser(user: FirebaseUser, viewModel: MainViewModel) {
        val appointments = mutableListOf<AppointmentModel>()
        database.child(DATABASE_USERS_KEY).child(user.uid).get()
            .addOnCompleteListener { query ->
                if (query.isSuccessful) {
                    val children = query.result.children
                    children.forEach {
                        it.getValue<AppointmentModel>()?.let { appointment ->
                            appointments.add(appointment)
                        }
                    }
                    viewModel.setScheduledAppointments(appointments.toList())
                }
            }
            .addOnFailureListener { error ->

        }
    }

}