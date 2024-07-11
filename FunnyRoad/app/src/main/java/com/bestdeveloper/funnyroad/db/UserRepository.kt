package com.bestdeveloper.funnyroad.db

import com.bestdeveloper.funnyroad.activity.FirebaseAuthenticator
import com.bestdeveloper.funnyroad.activity.OnSignUpResult
import com.google.firebase.auth.FirebaseUser

class UserRepository private constructor() {

    private val firebaseAuthenticator = FirebaseAuthenticator()

    companion object {
        @Volatile
        private var intance: UserRepository? = null

        fun getInstance(): UserRepository {
            return intance ?: synchronized(this) {
                intance ?: UserRepository().also {
                    intance = it
                }
            }
        }
    }

    fun createUser(email: String, password: String, onSignUpResult: OnSignUpResult){
        firebaseAuthenticator.createUserWithEmailAndPassword(email, password, onSignUpResult)
    }

    fun signIn(email: String, password: String, onSignUpResult: OnSignUpResult){
        firebaseAuthenticator.signInWithEmailAndPassword(email, password, onSignUpResult)
    }

    fun getCurrentUser() : FirebaseUser? {
        return firebaseAuthenticator.getAuthInstance().currentUser
    }
}
