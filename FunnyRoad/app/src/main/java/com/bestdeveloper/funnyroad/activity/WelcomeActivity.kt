package com.bestdeveloper.funnyroad.activity

import EditTextUtils
import EditTextUtils.highlightFieldAndSetError
import EditTextUtils.setDefaultStrokeOnChangedEditText
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {
    private lateinit var load_txt: TextView
    private var mAuth: FirebaseAuth? = null

    // Binding
    private lateinit var binding: ActivityWelcomeBinding

    private lateinit var progressBar: ProgressBar
    private lateinit var greyBackground: View

    private val navigator = ViewNavigator()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()

        progressBar = binding.logInPrBar
        greyBackground = binding.lowTrBackground
        load_txt = binding.loggingInTextBar
        hideLogInProgressBar()
        
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
                signIn(email, password, object : OnSignUpResult{
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

    private fun signIn(email: String, password: String, onSignUpResult: OnSignUpResult) {

            mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        onSignUpResult.onSuccess()
                    } else {
                        // If sign in fails, display a message to the user.
                        onSignUpResult.onFailure()
                    }
                }
        }

    private fun onLogInSuccess(){
        hideLogInProgressBar()
        binding.incPassString.text = ""
        Log.d(ContentValues.TAG, "signInWithEmail:success")
        navigator.navigateToMapActivity(this)
    }

    private fun onLogInFailure(){
        hideLogInProgressBar()
        binding.incPassString.text = resources.getString(R.string.incorrect_credentials)
        Log.w(ContentValues.TAG, "signInWithEmail:failure")
        Toast.makeText(
            this@WelcomeActivity, "Authentication failed.",
            Toast.LENGTH_SHORT
        ).show()
    }


    override fun onStart() {
        super.onStart()
        if (mAuth!!.currentUser == null) {
            Log.i("TAG", "user: " + mAuth!!.currentUser!!.uid)
            navigator.navigateToMapActivity(this)
        }
    }

    private fun hideLogInProgressBar() {
        load_txt.visibility = View.GONE
        progressBar.visibility = View.GONE
        greyBackground.visibility = View.GONE
    }

    private fun showLogInProgressBar() {
        load_txt.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        greyBackground.visibility = View.VISIBLE
    }
}
