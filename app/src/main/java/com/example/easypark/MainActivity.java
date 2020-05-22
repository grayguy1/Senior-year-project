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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //Declarations
    EditText emailId, password, cPassword, fName, lName, pNum;
    Button btnSignUp;
    TextView tvSignIn;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore db;

    int tokenAmt = 0;
    int carNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Instantiations
        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        emailId = findViewById(R.id.editText);
        password = findViewById(R.id.editText2);
        cPassword = findViewById(R.id.editText7);
        fName = findViewById(R.id.editText6);
        lName = findViewById(R.id.editText5);
        pNum = findViewById(R.id.editText4);

        btnSignUp = findViewById(R.id.button);
        tvSignIn = findViewById(R.id.textView2);

        emailId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        cPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        fName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        lName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        pNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        //Sign up button
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User information
                final String email = emailId.getText().toString();
                String pwd = password.getText().toString();
                String cpwd = cPassword.getText().toString();
                final String fname = fName.getText().toString();
                final String lname = lName.getText().toString();
                final String pnum = pNum.getText().toString();
                String regex = "\\d+";
                //HashMap to store user information
                final Map<String, Object> newUser = new HashMap<>();

                //All fields should be filled
                if(email.isEmpty() && pwd.isEmpty() && cpwd.isEmpty() && fname.isEmpty() && lname.isEmpty() && pnum.isEmpty()){
                    Toast.makeText(MainActivity.this,  "Fields are empty!", Toast.LENGTH_SHORT).show();
                }
                //Email should not be empty
                else if(email.isEmpty()){
                    emailId.setError("Please enter email id");
                    emailId.requestFocus();
                }
                //Password should not be empty
                else if(pwd.isEmpty()){
                    password.setError("Please enter a password");
                    password.requestFocus();
                }
                //Confirm password should not be empty
                else if(cpwd.isEmpty()){
                    cPassword.setError("Please confirm your password");
                    cPassword.requestFocus();
                }
                //First name should not be empty
                else if(fname.isEmpty()){
                    fName.setError("Please enter your first name");
                    fName.requestFocus();
                }
                //Last name should not be empty
                else if(lname.isEmpty()){
                    lName.setError("Please enter your last name");
                    lName.requestFocus();
                }
                //Phone number should not be empty
                else if(pnum.isEmpty()){
                    pNum.setError("Please enter your phone number");
                    pNum.requestFocus();
                }
                //Phone number should match the regex
                else if(!pnum.matches(regex)){
                    pNum.setError("Phone number should only have digits");
                    pNum.requestFocus();
                }
                //Phone number should have 10 digits
                else if(pnum.length() != 10){
                    pNum.setError("Phone number should have 10 digits");
                    pNum.requestFocus();
                }
                else {
                    //Password should equal confirm password
                    if(pwd.equals(cpwd)){
                        //Add the new user to firebase authenticator
                        mFirebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Signup unsuccessful, Please try again", Toast.LENGTH_SHORT).show();
                                }
                                //If a new user is added to the authenticator then add the user to the firestore with all the attributes
                                else {
                                    newUser.put("FName", fname);
                                    newUser.put("LName", lname);
                                    newUser.put("PNum", pnum);
                                    newUser.put("TAmt", tokenAmt);
                                    newUser.put("CNum", carNum);
                                    //Create a user collection and put the user in it with his email as the key
                                    db.collection("User").document(email)
                                            .set(newUser)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(MainActivity.this, "SignUp Unsuccessful, Please try again", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        });
                    }
                    //If password does not match confirm password
                    else{
                        cPassword.setError("Password does not match confirm password");
                        cPassword.requestFocus();
                    }
                }
            }

        });
        //Textview to go to sign up screen
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v){
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
