package com.bestdeveloper.funnyroad.activity

import EditTextUtils
import EditTextUtils.highlightFieldAndSetError
import EditTextUtils.setDefaultStrokeOnChangedEditText
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var load_txt: TextView
    private lateinit var mAuth: FirebaseAuthenticator

    // Binding
    private lateinit var binding: ActivityWelcomeBinding

    private lateinit var progressBar: ProgressBar
    private lateinit var greyBackground: View

    private val navigator = ViewNavigator()

    // view model
    private lateinit var viewModel : WelcomeActivityViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(WelcomeActivityViewModel::class.java)

        mAuth = FirebaseAuthenticator()
        progressBar = binding.logInPrBar
        greyBackground = binding.lowTrBackground
        load_txt = binding.loggingInTextBar
        switchBars(View.GONE)
        
        setDefaultStrokeOnChangedEditText(applicationContext, binding.loginEmail)
        setDefaultStrokeOnChangedEditText(applicationContext, binding.loginPassword)

        // Set up click listeners using binding
        binding.logInBtn.setOnClickListener {
            val email = EditTextUtils.trimInput(binding.loginEmail)
            val password = EditTextUtils.trimInput(binding.loginPassword)
            validate(email, password, binding.incPassString)
        }

        binding.signUpTxtBtn.setOnClickListener {
            navigator.navigateToSignUpActivity(this)
        }

    }

    private fun validate(email: String, password: String, errorText: TextView) {
        CredentialValidator().validate(email, password, object : ValidationInterface{
            override fun emailOnEmptyResult() {
                highlightFieldAndSetError(applicationContext, binding.loginEmail, errorText, getString(R.string.please_enter_your_email))
            }

            override fun passwordOnEmptyResult() {
                highlightFieldAndSetError(applicationContext, binding.loginPassword, errorText, getString(R.string.please_enter_your_password))
            }

            override fun onBothEmpty() {
                highlightFieldAndSetError(applicationContext, binding.loginEmail, errorText, "")
                highlightFieldAndSetError(applicationContext, binding.loginPassword, errorText, "")
            }

            override fun emailOnInvalidResult() {
                highlightFieldAndSetError(applicationContext, binding.loginEmail, errorText,  getString(R.string.incorrect_email))
            }

            override fun passwordOnInvalidResult() {
               highlightFieldAndSetError(applicationContext, binding.loginPassword, errorText, getString(R.string.short_password))
            }



            override fun OnSuccessResult() {
                switchBars(View.VISIBLE)
                viewModel.signIn(email, password, object : OnSignUpResult{
                    override fun onSuccess() {
                        onLogInSuccess()
                    }

                    override fun onFailure() {
                        onLogInFailure()
                    }
                })
            }
        })

    }


    private fun onLogInSuccess(){
        switchBars(View.GONE)
        binding.incPassString.text = ""
        Log.d(ContentValues.TAG, "signInWithEmail:success")
        navigator.navigateToMapActivity(this)
    }

    private fun onLogInFailure(){
        switchBars(View.GONE)
        binding.incPassString.text = resources.getString(R.string.incorrect_credentials)
        Log.w(ContentValues.TAG, "signInWithEmail:failure")
        Toast.makeText(
            this@WelcomeActivity, "Authentication failed.",
            Toast.LENGTH_SHORT
        ).show()
    }


    override fun onStart() {
        super.onStart()
        if (viewModel.getCurrentUser() == null) {
            Log.i("TAG", "user: " + viewModel.getCurrentUser()!!.uid)
            navigator.navigateToMapActivity(this)
        }
    }

    private fun switchBars(viewState : Int) {
        load_txt.visibility = viewState
        progressBar.visibility = viewState
        greyBackground.visibility = viewState
    }
}
