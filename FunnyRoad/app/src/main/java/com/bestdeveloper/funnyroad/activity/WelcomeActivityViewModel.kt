package com.bestdeveloper.funnyroad.activity

import androidx.lifecycle.ViewModel
import com.bestdeveloper.funnyroad.db.UserRepository
import com.google.firebase.auth.FirebaseUser

class WelcomeActivityViewModel: ViewModel() {
    private var userRepository = UserRepository.getInstance()

    fun signIn(email: String, password:String, onSignUpResult: OnSignUpResult){
        userRepository.signIn(email, password, onSignUpResult)
    }

    fun createUser(email: String, password:String, onSignUpResult: OnSignUpResult){
        userRepository.createUser(email, password, onSignUpResult)
    }

    fun getCurrentUser() : FirebaseUser? {
        return userRepository.getCurrentUser()
    }
}