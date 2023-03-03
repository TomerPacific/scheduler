package com.tomerpacific.scheduler.ui.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.tomerpacific.scheduler.service.AuthService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var authService: AuthService = AuthService()
    private var user: FirebaseUser? = null

    fun isUserInputValid(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    fun signupUser(email: String, password: String) {
        viewModelScope.launch {
            coroutineScope {
                launch {
                    user = authService.signupUser(email, password)
                }
            }

        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            coroutineScope {
                launch {
                    user = authService.logInUser(email, password)
                }
            }

        }
    }

    fun isUserConnected(): Boolean {
        return authService.isUserCurrentlySignedIn()
    }

}