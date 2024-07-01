package com.bestdeveloper.funnyroad.activity

import EditTextUtils
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import com.bestdeveloper.funnyroad.R

class CredentialValidator {

     fun isPasswordConfident(password: String): Boolean {
        return  password.length > 8
    }

     fun isEmailValid(email: String): Boolean {
        return  Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validate(email: String, password: String, validationInterface: ValidationInterface) {
        var isValid = true

        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
            validationInterface.onBothEmpty()
            isValid = false
        }
        else {

            when {
                TextUtils.isEmpty(email) -> {
                    validationInterface.emailOnEmptyResult()
                    isValid = false
                }
                TextUtils.isEmpty(password) -> {
                    validationInterface.passwordOnEmptyResult()
                    isValid = false
                }
                !isEmailValid(email) -> {
                    validationInterface.emailOnInvalidResult()
                    isValid = false
                }
                !isPasswordConfident(password) -> {
                    validationInterface.passwordOnInvalidResult()
                    isValid = false
                }
            }
        }

        if (isValid) {
            validationInterface.OnSuccessResult()
        }
    }
}



