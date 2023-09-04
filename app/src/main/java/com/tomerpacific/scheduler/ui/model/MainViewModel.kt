package com.tomerpacific.scheduler.ui.model

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.tomerpacific.scheduler.NAVIGATION_DESTINATION_APPOINTMENTS
import com.tomerpacific.scheduler.NAVIGATION_DESTINATION_LOGIN
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.service.AuthService
import com.tomerpacific.scheduler.service.DatabaseService
import com.tomerpacific.scheduler.service.RemoteConfigService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainViewModel(application: Application) : AndroidViewModel(application) {


    private val applicationContext = application
    private val remoteConfigService: RemoteConfigService = RemoteConfigService()
    private val authService: AuthService = AuthService(remoteConfigService)
    private val databaseService: DatabaseService = DatabaseService(remoteConfigService)
    private val _user: MutableLiveData<FirebaseUser> = MutableLiveData()
    val user: LiveData<FirebaseUser?> = _user

    private val _scheduledAppointments: MutableLiveData<List<AppointmentModel>> = MutableLiveData()
    val scheduledAppointments: LiveData<List<AppointmentModel>> = _scheduledAppointments

    private val _availableAppointments: MutableLiveData<List<AppointmentModel>> = MutableLiveData()
    val availableAppointments: LiveData<List<AppointmentModel>> = _availableAppointments

    private val _shouldDisplayCircularProgressBar: MutableLiveData<Boolean> = MutableLiveData(false)
    val shouldDisplayCircularProgressBar: LiveData<Boolean> = _shouldDisplayCircularProgressBar

    private val _currentLocation: MutableLiveData<LatLng> = MutableLiveData()
    val currentLocation: LiveData<LatLng> = _currentLocation

    init {
        remoteConfigService.fetchAndActivate(::remoteConfigurationActivatedSuccess, ::remoteConfigurationActivatedFailure)
    }

    fun isUserInputValid(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    fun signupUser(email: String, password: String, onNavigateAfterLoginScreen: () -> Unit) {
        _shouldDisplayCircularProgressBar.value = true
        viewModelScope.launch {
            coroutineScope {
                launch {
                    _user.value = authService.signupUser(email, password)
                    onNavigateAfterLoginScreen()
                }
            }

        }
    }

    fun loginUser(email: String, password: String, onNavigateAfterLoginScreen: () -> Unit) {
        _shouldDisplayCircularProgressBar.value = true
        viewModelScope.launch {
            coroutineScope {
                launch {
                    _user.value = authService.logInUser(email, password)
                    onNavigateAfterLoginScreen()
                }
            }

        }
    }

    private fun isUserConnected(): Boolean {
        return authService.isUserCurrentlySignedIn()
    }

    fun logout() {
        authService.logOutUser()
    }

    fun isAdminUser(): Boolean {
        _user.value?.let {
            return when(it.email) {
                null -> false
                else -> authService.isAdminUser(it.email!!)
            }
        }

        return  false
    }

    fun getStartDestination(): String {
        return when (isUserConnected()) {
            true -> NAVIGATION_DESTINATION_APPOINTMENTS
            false -> NAVIGATION_DESTINATION_LOGIN
        }
    }

    fun addAppointment(appointment: AppointmentModel, onAppointmentScheduled: (String?, String?) -> Unit) {
        appointment.userId = _user.value!!.uid
        databaseService.setAppointment(appointment, onAppointmentScheduled)
    }

    fun setAvailableAppointments(appointments: List<AppointmentModel>) {
        _availableAppointments.value = appointments
    }

    fun setScheduledAppointments(scheduledAppointments: List<AppointmentModel>) {
        _scheduledAppointments.value = scheduledAppointments
    }

    fun updateScheduledAppointmentsForUser() {
        databaseService.fetchScheduledAppointmentsForUser(_user.value!!, this, isAdminUser())
    }

    fun cancelScheduledAppointmentForUser(appointment: AppointmentModel,
                                          onAppointmentCancelled: (String?, String?) -> Unit) {
        databaseService.cancelAppointment(appointment, onAppointmentCancelled)
    }

    fun getAppointmentsForDay(date: LocalDateTime) {
        if (Utils.isWeekend(date.dayOfWeek)) {
            setAvailableAppointments(listOf())
            return
        }
        databaseService.getAvailableAppointmentsForDate(this, date)
    }

    private fun remoteConfigurationActivatedSuccess() {
        _user.value = authService.getCurrentlySignedInUser()
        if (_user.value != null) {
            databaseService.fetchScheduledAppointmentsForUser(_user.value!!, this, isAdminUser())
        }
    }

    private fun remoteConfigurationActivatedFailure(errorMsg: String) {
        _user.value = authService.getCurrentlySignedInUser()
        if (_user.value != null) {
            databaseService.fetchScheduledAppointmentsForUser(_user.value!!, this, isAdminUser())
        }
    }

    fun disableCircularProgressBarIndicator() {
        _shouldDisplayCircularProgressBar.value = false
    }

    @SuppressLint("MissingPermission")
    fun updateLocation() {
        val fusedLocation = LocationServices.getFusedLocationProviderClient(
            applicationContext)

        fusedLocation.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    _currentLocation.value = LatLng(location.latitude, location.longitude)
                }
            }
    }

}