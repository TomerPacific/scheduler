package com.tomerpacific.scheduler.service

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.tomerpacific.scheduler.END_HOUR_FOR_APPOINTMENTS
import com.tomerpacific.scheduler.FirebaseRemoteConfigurationsModel
import com.tomerpacific.scheduler.R
import com.tomerpacific.scheduler.REMOTE_CONFIG_APPOINTMENT_HOURS_KEY
import com.tomerpacific.scheduler.START_HOUR_FOR_APPOINTMENTS
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

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
                if (task.isSuccessful) {
                    val remoteConfigValues = task.result
                    print(remoteConfigValues)
                }
            }.addOnFailureListener { error ->
                print(error.localizedMessage)
            }
    }

    fun getAppointmentStartAndEndTimeByDay(currentDate: LocalDateTime): List<Int> {
        val appointmentStartAndEndTimesFromConfig = remoteConfig.getString(REMOTE_CONFIG_APPOINTMENT_HOURS_KEY)
        val firebaseRemoteConfigurationsModel: FirebaseRemoteConfigurationsModel = Json.decodeFromString(
            FirebaseRemoteConfigurationsModel.serializer(),
            appointmentStartAndEndTimesFromConfig)

        return when (currentDate.dayOfWeek.toString()) {
            "SUNDAY" -> firebaseRemoteConfigurationsModel.sunday
            "MONDAY" -> firebaseRemoteConfigurationsModel.monday
            "TUESDAY" -> firebaseRemoteConfigurationsModel.tuesday
            "WEDNESDAY" -> firebaseRemoteConfigurationsModel.wednesday
            "THURSDAY" -> firebaseRemoteConfigurationsModel.thursday
            else -> listOf(START_HOUR_FOR_APPOINTMENTS, END_HOUR_FOR_APPOINTMENTS)
        }
    }


}