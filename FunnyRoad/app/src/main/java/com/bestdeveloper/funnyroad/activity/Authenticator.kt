package com.bestdeveloper.funnyroad.activity

import com.google.firebase.auth.FirebaseAuth

interface Authenticator {
     fun signInWithEmailAndPassword(email: String,
                                           password: String,
                                           onSignUpResult: OnSignUpResult)
     fun createUserWithEmailAndPassword(email: String, password: String, onSignUpResult: OnSignUpResult)
}

class FirebaseAuthenticator: Authenticator {
    private val mAuth = FirebaseAuth.getInstance()

    override fun signInWithEmailAndPassword(
        email: String,
        password: String,
        onSignUpResult: OnSignUpResult,
    ) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    onSignUpResult.onSuccess()
                } else {
                    // If sign in fails, display a message to the user.
                    onSignUpResult.onFailure()
                }
            }
    }

    override fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        onSignUpResult: OnSignUpResult) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSignUpResult.onSuccess()
                } else {
                    onSignUpResult.onFailure()
                }
            }
    }

    fun getAuthInstance() : FirebaseAuth{
        return mAuth
    }


}