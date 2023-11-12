package com.tomerpacific.scheduler.service

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.tomerpacific.scheduler.END_HOUR_FOR_APPOINTMENTS
import com.tomerpacific.scheduler.ui.model.AppointmentStartAndEndTimesModel
import com.tomerpacific.scheduler.R
import com.tomerpacific.scheduler.REMOTE_CONFIG_ADMIN_EMAIL_KEY
import com.tomerpacific.scheduler.REMOTE_CONFIG_APPOINTMENT_HOURS_KEY
import com.tomerpacific.scheduler.REMOTE_CONFIG_MONDAY_KEY
import com.tomerpacific.scheduler.REMOTE_CONFIG_SUNDAY_KEY
import com.tomerpacific.scheduler.REMOTE_CONFIG_THURSDAY_KEY
import com.tomerpacific.scheduler.REMOTE_CONFIG_TUESDAY_KEY
import com.tomerpacific.scheduler.REMOTE_CONFIG_WEDNESDAY_KEY
import com.tomerpacific.scheduler.START_HOUR_FOR_APPOINTMENTS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

class RemoteConfigService {

    private val remoteConfig = Firebase.remoteConfig
    private lateinit var appointmentStartAndEndTimes: AppointmentStartAndEndTimesModel
    private lateinit var adminUserEmail: String

    init {
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    suspend fun fetchAndActivate(onRemoteConfigurationActivatedSuccess: () -> Unit, onRemoteConfigurationActivatedFailure: (String) -> Unit) {

        CoroutineScope(Dispatchers.IO).launch {
            val remoteConfigActivation = remoteConfig.fetchAndActivate()
            val didFetchAndActivate = remoteConfigActivation.await()

            if (didFetchAndActivate || remoteConfigActivation.isSuccessful) {
                val appointmentStartAndEndTimesFromConfig = remoteConfig.getString(REMOTE_CONFIG_APPOINTMENT_HOURS_KEY)
                appointmentStartAndEndTimes = Json.decodeFromString(
                    AppointmentStartAndEndTimesModel.serializer(),
                    appointmentStartAndEndTimesFromConfig)
                adminUserEmail = remoteConfig.getString(REMOTE_CONFIG_ADMIN_EMAIL_KEY)
                withContext(Dispatchers.Main) {
                    onRemoteConfigurationActivatedSuccess()
                }
            } else {
                remoteConfigActivation.exception?.localizedMessage?.let { errorMsg ->
                    withContext(Dispatchers.Main) {
                        onRemoteConfigurationActivatedFailure(errorMsg)
                    }

                }
            }
        }
    }

    fun getAppointmentStartAndEndTimeByDay(currentDate: LocalDateTime): List<Int> {
        return when (currentDate.dayOfWeek.toString()) {
            REMOTE_CONFIG_SUNDAY_KEY -> appointmentStartAndEndTimes.sunday
            REMOTE_CONFIG_MONDAY_KEY -> appointmentStartAndEndTimes.monday
            REMOTE_CONFIG_TUESDAY_KEY -> appointmentStartAndEndTimes.tuesday
            REMOTE_CONFIG_WEDNESDAY_KEY -> appointmentStartAndEndTimes.wednesday
            REMOTE_CONFIG_THURSDAY_KEY -> appointmentStartAndEndTimes.thursday
            else -> listOf(START_HOUR_FOR_APPOINTMENTS, END_HOUR_FOR_APPOINTMENTS)
        }
    }

    fun isAdminUser(email: String): Boolean {
        return email == adminUserEmail
    }

}