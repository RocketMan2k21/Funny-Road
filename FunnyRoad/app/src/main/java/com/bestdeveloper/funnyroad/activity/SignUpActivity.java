package com.bestdeveloper.funnyroad.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bestdeveloper.funnyroad.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    public static final String SIGNED_USER = "user";
    Button signUpButton;
    EditText userEmail;
    EditText userPassword;

    // Firebase auth state
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        userEmail = findViewById(R.id.signUpEmail);
        userPassword = findViewById(R.id.signUpPassword);
        signUpButton = findViewById(R.id.signUpBtn);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = userEmail.getText().toString().trim();
                String password = userPassword.getText().toString().trim();

                if (!isEmailValid(email)) {
                    Toast.makeText(SignUpActivity.this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();

                    // highlighting the field
                }
                if (!isPasswordConfident(password)){
                    //Highlight field
                }
                else{     // Creates account
                    createUser(email, password);
                }



            }
        });
    }

    private void createUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(SignUpActivity.this, MapActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }



    private boolean isPasswordConfident(String password) {
        return !TextUtils.isEmpty(password) && password.length()>8;

    }

    private boolean isEmailValid(String email) {
        return email !=null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


}