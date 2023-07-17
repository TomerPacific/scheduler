package com.tomerpacific.scheduler.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthService(remoteConfigurationService: RemoteConfigService) {

    private val auth: FirebaseAuth = Firebase.auth
    private val remoteConfigService: RemoteConfigService = remoteConfigurationService

    fun isUserCurrentlySignedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun signupUser(email: String, password: String) =
        withContext(Dispatchers.Default) {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user
        }

    suspend fun logInUser(email: String, password: String) =
        withContext(Dispatchers.Default) {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user
        }

    fun logOutUser() {
        auth.signOut()
    }

    fun getCurrentlySignedInUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isAdminUser(email: String): Boolean {
        return remoteConfigService.isAdminUser(email)
    }

}