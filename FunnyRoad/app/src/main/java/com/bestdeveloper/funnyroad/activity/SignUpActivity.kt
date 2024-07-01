package com.bestdeveloper.funnyroad.activity

import EditTextUtils
import EditTextUtils.highlightFieldAndSetError
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth


class SignUpActivity : AppCompatActivity() {
    private lateinit  var load_txt: TextView
    lateinit var signUpButton: Button
    lateinit  var userEmail: EditText
    lateinit var userPassword: EditText

    private lateinit var progressBar: ProgressBar
    private lateinit var greyBackground: View

    // Firebase auth state
    private var mAuth: FirebaseAuth? = null
    private lateinit var binding: ActivitySignUpBinding

    // Navigator
    private val navigator = ViewNavigator()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        // Progress bar
        progressBar = binding.logInPrBar
        greyBackground = binding.lowTrBackground
        load_txt = binding.loggingInTextBar

        userEmail = binding.signUpEmail
        userPassword = binding.signUpPassword
        signUpButton = binding.signUpBtn

        val errorText = binding.incPassString

        switchLogInProgressBar(View.GONE)

        EditTextUtils.setDefaultStrokeOnChangedEditText(applicationContext, userEmail)
        EditTextUtils.setDefaultStrokeOnChangedEditText(applicationContext, userPassword)

        signUpButton.setOnClickListener(View.OnClickListener {
            val email = EditTextUtils.trimInput(userEmail)
            val password = EditTextUtils.trimInput(userPassword)
            validate(email, password, errorText)
        })
    }

    private fun validate(email: String, password: String, errorText: TextView) {
        CredentialValidator().validate(email, password, object : ValidationInterface {
            override fun emailOnEmptyResult() {
                highlightFieldAndSetError(applicationContext, userEmail, errorText, getString(R.string.please_enter_your_email))
            }

            override fun passwordOnEmptyResult() {
                highlightFieldAndSetError(applicationContext, userPassword, errorText, getString(R.string.please_enter_your_password))
            }

            override fun emailOnInvalidResult() {
                highlightFieldAndSetError(applicationContext, userEmail, errorText, getString(R.string.please_enter_correct_email))
            }

            override fun passwordOnInvalidResult() {
                highlightFieldAndSetError(applicationContext, userPassword, errorText, getString(R.string.short_password))
            }

            override fun onBothEmpty() {
                highlightFieldAndSetError(applicationContext, userEmail, errorText, "")
                highlightFieldAndSetError(applicationContext, userPassword, errorText, "")
            }

            override fun OnSuccessResult() {
                switchLogInProgressBar(View.VISIBLE)
                errorText.text = ""
                createUser(email, password, object : OnSignUpResult{
                    override fun onSuccess() {
                        onSignUpSuccess()
                    }

                    override fun onFailure() {
                        onSignUpFail()
                    }
                })
            }
        })
    }

    private fun createUser(email: String, password: String, onSignUpResult: OnSignUpResult) {
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSignUpResult.onSuccess()
                } else {
                    onSignUpResult.onFailure()
                }
            }
    }

    private fun switchLogInProgressBar(optionView : Int) {
        load_txt.visibility = optionView
        progressBar.visibility = optionView
        greyBackground.visibility = optionView
    }

    private fun onSignUpSuccess() {
        switchLogInProgressBar(View.GONE)
        Log.d("TAG", "createUserWithEmail:success")
        navigator.navigateToMapActivity(this)
    }

    private fun onSignUpFail() {
        switchLogInProgressBar(View.GONE)
        Log.d("TAG", "createUserWithEmail:success")
        navigator.navigateToMapActivity(this)
    }


}



