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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseUser
import com.tomerpacific.scheduler.BuildConfig
import com.tomerpacific.scheduler.NAVIGATION_DESTINATION_APPOINTMENTS
import com.tomerpacific.scheduler.NAVIGATION_DESTINATION_LOGIN
import com.tomerpacific.scheduler.Utils
import com.tomerpacific.scheduler.service.AuthService
import com.tomerpacific.scheduler.service.DatabaseService
import com.tomerpacific.scheduler.service.RemoteConfigService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val _currentScheduledAppointment: MutableLiveData<AppointmentModel> = MutableLiveData()
    val currentScheduledAppointment: LiveData<AppointmentModel> = _currentScheduledAppointment

    private val _locationText: MutableLiveData<String> = MutableLiveData("")
    val locationText: LiveData<String> = _locationText

    private val _locationAutofill: MutableLiveData<MutableList<MapSearchResult>> = MutableLiveData(mutableListOf())
    val locationAutofill = _locationAutofill

    private var placesClient: PlacesClient
    private var job: Job? = null

    init {
        remoteConfigService.fetchAndActivate(::remoteConfigurationActivatedSuccess, ::remoteConfigurationActivatedFailure)
        Places.initialize(applicationContext.applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(applicationContext)
    }

    fun isUserInputValid(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    fun signupUser(email: String, password: String, onNavigateAfterLoginScreen: () -> Unit) {
        _shouldDisplayCircularProgressBar.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val signedUpUser = authService.signupUser(email, password)
            withContext(Dispatchers.Main) {
                _user.value = signedUpUser
                onNavigateAfterLoginScreen()
            }

        }
    }

    fun loginUser(email: String, password: String, onNavigateAfterLoginScreen: () -> Unit) {
        _shouldDisplayCircularProgressBar.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val loggedInUser = authService.logInUser(email, password)
            withContext(Dispatchers.Main) {
                _user.value = loggedInUser
                onNavigateAfterLoginScreen()
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

    fun setCurrentScheduledAppointment(appointment: AppointmentModel) {
        _currentScheduledAppointment.value = appointment
        val userLocation = _currentScheduledAppointment.value!!.appointmentPlace
        if (userLocation.isNotEmpty()) {
            _currentLocation.value = Utils.convertToLatLng(userLocation)
        }
    }

    @SuppressLint("MissingPermission")
    fun setCurrentUserLocation(shouldUpdateAppointmentLocation: Boolean) {

        val fusedLocation = LocationServices.getFusedLocationProviderClient(
            applicationContext)

        fusedLocation.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    _currentLocation.value = LatLng(location.latitude, location.longitude)

                    if (shouldUpdateAppointmentLocation) {
                        _currentScheduledAppointment.value!!.appointmentPlace = "${location.latitude},${location.longitude}"

                        val index = _scheduledAppointments.value!!.indices.find {
                            _scheduledAppointments.value!![it].appointmentId == _currentScheduledAppointment.value!!.appointmentId
                        }
                        index?.let {
                            _scheduledAppointments.value!!.toMutableList()[it] =
                                _currentScheduledAppointment.value!!
                            databaseService.updateAppointmentForUser(_user.value!!,_currentScheduledAppointment.value!!)
                        }
                    }
                }
            }
    }

    fun updateLocation(location: LatLng) {
        _currentLocation.value = location
        _currentScheduledAppointment.value!!.appointmentPlace = "${location.latitude},${location.longitude}"

        val index = _scheduledAppointments.value!!.indices.find {
            _scheduledAppointments.value!![it].appointmentId == _currentScheduledAppointment.value!!.appointmentId
        }
        index?.let {
            _scheduledAppointments.value!!.toMutableList()[it] =
                _currentScheduledAppointment.value!!
            databaseService.updateAppointmentForUser(_user.value!!,_currentScheduledAppointment.value!!)
        }
    }

    private fun fetchLocations(location: String) {
        job?.cancel()
        _locationAutofill.value?.clear()

        job = viewModelScope.launch {
            val request = FindAutocompletePredictionsRequest
                .builder()
                .setQuery(location)
                .build()
            val results = mutableListOf<MapSearchResult>()
            placesClient
                .findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    results += response.autocompletePredictions.map {
                        MapSearchResult(
                            it.getFullText(null).toString(),
                            it.placeId
                        )
                    }
                    _locationAutofill.value = results
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
        }
    }

    private fun getCoordinatesFromLocationResult(location: MapSearchResult) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(location.placeId, placeFields)
        placesClient.fetchPlace(request)
            .addOnSuccessListener {
                if (it != null) {
                    updateLocation(it.place.latLng!!)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun handleLocationResultItemClicked(location: MapSearchResult) {
        _locationText.value = location.address
        _locationAutofill.value?.clear()
        getCoordinatesFromLocationResult(location)
    }

    fun handleLocationSearchTyping(location: String) {
        _locationText.value = location
        fetchLocations(location)
    }

    fun updateLocationByUserClickOnMap(location: LatLng) {
        _locationText.value = Utils.getAddressFromLatLng(applicationContext, location)
    }

}