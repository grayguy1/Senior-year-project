package com.example.easypark;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {
    //Declarations
    Button rBtn;
    EditText emailId;

    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Instantiations
        mFirebaseAuth = FirebaseAuth.getInstance();
        rBtn = findViewById(R.id.resetbtn);
        emailId = findViewById(R.id.emailtext);

        emailId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        //Reset button
        rBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rEmail = emailId.getText().toString();
                //If email is empty ask the user to enter email
                if(rEmail.isEmpty()){
                    emailId.setError("Please enter email id");
                    emailId.requestFocus();
                }
                //Get firebase to send a reset email
                FirebaseAuth.getInstance().sendPasswordResetEmail(rEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(ResetPassword.this, "Reset email has been sent!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(ResetPassword.this, "Email does not exist!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
