package com.bestdeveloper.funnyroad.activity

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bestdeveloper.funnyroad.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {
    private lateinit var load_txt: TextView
    private var mAuth: FirebaseAuth? = null

    // Binding
    private lateinit var binding: ActivityWelcomeBinding

    private lateinit var progressBar: ProgressBar
    private lateinit var greyBackground: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()

        // Set up click listeners using binding
        binding.logInBtn.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()
            signIn(email, password)
        }

        binding.signUpTxtBtn.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Progress bar
        progressBar = binding.logInPrBar
        greyBackground = binding.lowTrBackground
        load_txt = binding.loggingInTextBar

        hideLogInProgressBar()
    }



    private fun signIn(email: String, password: String) {
        showLogInProgressBar()
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        hideLogInProgressBar()
                        Log.d(ContentValues.TAG, "signInWithEmail:success")
                        val user = mAuth!!.currentUser
                        startActivity(Intent(this@WelcomeActivity, MapActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        hideLogInProgressBar()
                        Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            this@WelcomeActivity, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth!!.currentUser == null) {
            Log.i("TAG", "user: " + mAuth!!.currentUser!!.uid)
            val intent = Intent(this, MapActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
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
