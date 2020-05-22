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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateActivity extends AppCompatActivity {
    //Declarations
    FirebaseFirestore db;
    FirebaseAuth mFirebaseAuth;

    String email = "";
    String first, last, number;
    String newfirst, newlast, newnumber;

    EditText fName, lName, pNum;
    Button btnUpdate;
    TextView tkn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Instantiate the declarations
        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        email = mFirebaseAuth.getCurrentUser().getEmail();

        fName = findViewById(R.id.fname);
        lName = findViewById(R.id.lname);
        pNum = findViewById(R.id.pnum);
        btnUpdate = findViewById(R.id.updatebutton);
        tkn = findViewById(R.id.tokenText);

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

        //Get the Details of the user from the database
        try {
            final DocumentReference docRef = db.collection("User").document(email);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> tasku) {
                    //If the user exists then get results
                    if (tasku.isSuccessful()) {
                        DocumentSnapshot document = tasku.getResult();
                        if (document.exists()) {
                            //Display the first name, last name, and the phone number
                            first = document.getString("FName");
                            last = document.getString("LName");
                            number = document.getString("PNum");
                            fName.setText(first);
                            lName.setText(last);
                            pNum.setText(number);
                            tkn.setText("Tokens: " + document.getLong("TAmt").toString());
                            //Update button
                            btnUpdate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                    public void onClick (View view){
                                    //Regex for the user name and phone number
                                    String numRegex = "\\d+";
                                    String nameRegex = "^[\\p{L} .'-]+$";
                                    newfirst = fName.getText().toString();
                                    newlast = lName.getText().toString();
                                    newnumber = pNum.getText().toString();
                                    //If first name is different and matches regex then update
                                    if(newfirst != first) {
                                        if(newfirst.matches(nameRegex)) {
                                            docRef.update("FName", newfirst);
                                            Toast.makeText(UpdateActivity.this, "Information was updated!", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(UpdateActivity.this, "First name is incorrect!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    //If last name is different and matches regex then update
                                    if(newlast != last){
                                        if(newlast.matches(nameRegex)) {
                                            docRef.update("LName", newlast);
                                            Toast.makeText(UpdateActivity.this, "Information was updated!", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(UpdateActivity.this, "Last name is incorrect!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    //If phone number is different and matches regex then update
                                    if(newnumber != number){
                                        if(newnumber.matches(numRegex) && newnumber.length() == 10) {
                                            docRef.update("PNum", newnumber);
                                            Toast.makeText(UpdateActivity.this, "Information was updated!", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(UpdateActivity.this, "Phone number is incorrect!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    //If no changes are made then generate prompt
                                    if(newfirst.equals(first) && newlast.equals(last) && newnumber.equals(number)){
                                        Toast.makeText(UpdateActivity.this, "No changes made!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            //Log.d(TAG, "No such document");
                        }
                    } else {
                        //Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

        }
        catch(NullPointerException e){

        }
    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
