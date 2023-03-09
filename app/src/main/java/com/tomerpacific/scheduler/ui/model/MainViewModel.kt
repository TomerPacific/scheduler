package com.tomerpacific.scheduler.ui.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.tomerpacific.scheduler.service.AuthService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var authService: AuthService = AuthService()
    private val _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?> = _user

    fun isUserInputValid(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    fun signupUser(email: String, password: String) {
        viewModelScope.launch {
            coroutineScope {
                launch {
                    _user.value = authService.signupUser(email, password)
                }
            }

        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            coroutineScope {
                launch {
                    _user.value = authService.logInUser(email, password)
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

    fun getStartDestination(): String {
        return when (isUserConnected()) {
            true -> "appointments"
            false -> "login"
        }
    }

    fun getAppointmentsForUser(): List<AppointmentModel> {
        return listOf()
    }

    fun convertTimestampToDate(timestamp: Long): Date {
        return Date(timestamp)
    }

}