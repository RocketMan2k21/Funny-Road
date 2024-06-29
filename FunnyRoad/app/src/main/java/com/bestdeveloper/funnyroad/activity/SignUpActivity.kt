package com.bestdeveloper.funnyroad.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.activity.MapActivity
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

        switchLogInProgressBar(View.GONE)

        signUpButton.setOnClickListener(View.OnClickListener {
            val email = userEmail.getText().toString().trim { it <= ' ' }
            val password = userPassword.getText().toString().trim { it <= ' ' }

            if (!isEmailValid(email)) {
                Toast.makeText(this@SignUpActivity, "Please Enter Your Email", Toast.LENGTH_SHORT)
                    .show()

                // highlighting the field
            }
            if (!isPasswordConfident(password)) {
                //Highlight field
            } else {     // Creates account
                createUser(email, password)
            }
        })
    }

    private fun createUser(email: String, password: String) {
        switchLogInProgressBar(View.VISIBLE)
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    switchLogInProgressBar(View.GONE)
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "createUserWithEmail:success")
                    val user = mAuth!!.currentUser
                    val i = Intent(
                        this@SignUpActivity,
                        MapActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                    finish()
                } else {
                    switchLogInProgressBar(View.GONE)
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        this@SignUpActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun isPasswordConfident(password: String): Boolean {
        return !TextUtils.isEmpty(password) && password.length > 8
    }

    private fun isEmailValid(email: String?): Boolean {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun switchLogInProgressBar(optionView : Int) {
        load_txt.visibility = optionView
        progressBar.visibility = optionView
        greyBackground.visibility = optionView
    }

    companion object {
        const val SIGNED_USER: String = "user"
    }
}