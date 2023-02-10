package com.tomerpacific.scheduler.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AuthService {

    private var auth: FirebaseAuth = Firebase.auth

    fun isUserCurrentlySignedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun signupUser(email: String, password: String): FirebaseUser? {
        var user: FirebaseUser? = null

        coroutineScope {
            launch(Dispatchers.IO) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    user = when (it.isSuccessful) {
                        true -> auth.currentUser
                        false -> null
                    }
                }
            }

        }
        return user
    }

    suspend fun signInUser(email: String, password: String): FirebaseUser? {

        var user: FirebaseUser? = null
        coroutineScope {
            launch(Dispatchers.IO) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    user = when (it.isSuccessful) {
                        true -> auth.currentUser
                        false -> null
                    }
                }
            }
        }

        return user
    }
}