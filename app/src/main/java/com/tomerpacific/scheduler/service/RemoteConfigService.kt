package com.tomerpacific.scheduler.service

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.tomerpacific.scheduler.END_HOUR_FOR_APPOINTMENTS
import com.tomerpacific.scheduler.R
import com.tomerpacific.scheduler.START_HOUR_FOR_APPOINTMENTS

class RemoteConfigService {

    private val remoteConfig = Firebase.remoteConfig

    init {
        val remoteConfigSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 2000
        }

        remoteConfig.setConfigSettingsAsync(remoteConfigSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->

            }.addOnFailureListener { error ->
                print(error.localizedMessage)
            }
    }

    fun getAppointmentStartTime(): Int {
        return when(val appointmentStartTime = remoteConfig.getDouble("appointment_start_time")) {
            0.0 -> START_HOUR_FOR_APPOINTMENTS
            else -> appointmentStartTime.toInt()
        }
    }

    fun getAppointmentEndTime(): Int {
        return when(val appointmentEndTime = remoteConfig.getDouble("appointment_end_time")) {
            0.0 -> END_HOUR_FOR_APPOINTMENTS
            else -> appointmentEndTime.toInt()
        }
    }

}