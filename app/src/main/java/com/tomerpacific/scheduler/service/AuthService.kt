package com.tomerpacific.scheduler.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthService {

    private var auth: FirebaseAuth = Firebase.auth

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
}