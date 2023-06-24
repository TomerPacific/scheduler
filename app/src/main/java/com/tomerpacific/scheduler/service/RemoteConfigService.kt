package com.tomerpacific.scheduler.service

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.tomerpacific.scheduler.END_HOUR_FOR_APPOINTMENTS
import com.tomerpacific.scheduler.R
import com.tomerpacific.scheduler.START_HOUR_FOR_APPOINTMENTS
import org.json.JSONObject
import java.time.ZoneId
import java.util.*

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

    fun getAppointmentStartAndEndTimeByDay(date: Date): List<Int> {
        val currentDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val appointmentStartAndEndTimesFromConfig = remoteConfig.getString("appointment_start_and_end_times_by_day")
        val appointmentStartAndEndTimes = JSONObject(appointmentStartAndEndTimesFromConfig)
        val currentDay = currentDate.dayOfWeek.toString()

        if (!appointmentStartAndEndTimes.has(currentDay)) {
            return listOf(START_HOUR_FOR_APPOINTMENTS, END_HOUR_FOR_APPOINTMENTS)
        }

        val startAndEndTimesForToday = appointmentStartAndEndTimes.getJSONArray(currentDay)
        return listOf(startAndEndTimesForToday.get(0) as Int, startAndEndTimesForToday.get(1) as Int)
    }


}