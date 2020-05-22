package com.example.easypark;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

public class HomeActivity extends AppCompatActivity implements SearchAdapter.OnNoteListener {
    //Button declarations
    Button btnLogout;
    Button btnPackages;
    Button btnSettings;
    Button btnConfirm;
    Button btnAddTime;
    Button btnSubTime;
    //TextView declarations
    TextView timmer;
    TextView totalCost;
    TextView lotSelected;
    TextView lotsAvailable;
    //Declarations for the search bar
    EditText sbar;
    RecyclerView recyclerView;
    SearchAdapter searchAdapter;
    SearchAdapter.OnNoteListener onNoteListenerm;
    //Declarations for firebase
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    CollectionReference collectionReference;

    //General declarations used in the program
    ArrayList<String> pName;
    String currLot;
    String ourLot;
    String ourSpot;
    private long ourTime = 1;
    Long cost;
    double cTotal;
    boolean lock = false;
    boolean disable = false;
    //Declarations for the timer
    private long startTimeInMillis = 900000;
    private long timeLeft;
    private long endTime;
    String email = "";

    private CountDownTimer countDownTimer;

    boolean Lot_open = false;

    DecimalFormat numberFormat = new DecimalFormat("#");


    Long currAmt;
    Long myCost;
    double newAmt;

    private static final String TAG = "MainActivity";

    //Function that runs whenever the screen is opened
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        onNoteListenerm = this;
        //Instantiate the button and textviews from the XML file
        btnLogout = findViewById(R.id.logout);
        btnPackages = findViewById(R.id.packageButton);
        btnSettings = findViewById(R.id.settingbutton);
        btnConfirm = findViewById(R.id.confirmbutton);
        btnAddTime = findViewById(R.id.add15);
        btnSubTime = findViewById(R.id.sub15);
        timmer = findViewById(R.id.timertext);
        totalCost = findViewById(R.id.costtext);
        lotSelected = findViewById(R.id.lottext);
        lotsAvailable = findViewById(R.id.lotsavailable);
        //Instantiate the search bar from the XML file
        sbar = findViewById(R.id.searchbar);
        recyclerView = findViewById(R.id.recyclerview);
        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        //Get user's email
        email = firebaseUser.getEmail();
        //Layout definitions for the search bar
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        pName = new ArrayList<String>();



        //Add 15 minutes when add time button is pressed
        btnAddTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if time is less than or equal to 1hr 45 minutes add 15 minutes
                if(startTimeInMillis <= 6300000) {
                    startTimeInMillis = startTimeInMillis + 900000;
                }
                //Else if time is greater than 1 hour 15 minutes then set time to 2 hours and generate a prompt
                else if(startTimeInMillis > 6300000){
                    startTimeInMillis = 7200000;
                    Toast.makeText(HomeActivity.this,  "Time cannot be increased to more than 2 hours!", Toast.LENGTH_SHORT).show();
                }
                int hours = (int) (startTimeInMillis / 1000) / 3600;
                int minutes = (int) ((startTimeInMillis/1000) % 3600) / 60;
                int seconds = (int) ((startTimeInMillis % 60000) / 1000);
                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                timmer.setText(timeLeftFormatted);
                if(Lot_open != false) {
                    cTotal = ((double) startTimeInMillis * 4 * cost) / (3600 * 1000);
                    totalCost.setText(String.valueOf(numberFormat.format(cTotal)) + " coins");
                }
                //else{
                //    Toast.makeText(HomeActivity.this,  "Select a lot to see the cost!", Toast.LENGTH_SHORT).show();
                //}
            }
        });
        //Subtract 15 minutes when subtract button is pressed
        btnSubTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If time is greater than equal to 30 minutes subtract 15 minutes
                if(startTimeInMillis >= 1800000) {
                    startTimeInMillis = startTimeInMillis - 900000;
                }
                //Else generate a prompt
                else{
                    Toast.makeText(HomeActivity.this,  "Time cannot be decreased to less than 15 minutes!", Toast.LENGTH_SHORT).show();
                }
                int hours = (int) (startTimeInMillis / 1000) / 3600;
                int minutes = (int) ((startTimeInMillis/1000) % 3600) / 60;
                int seconds = (int) ((startTimeInMillis % 60000) / 1000);
                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                timmer.setText(timeLeftFormatted);
                if(Lot_open != false) {
                    cTotal = ((double) startTimeInMillis * 4 * cost) / (3600 * 1000);
                    totalCost.setText(String.valueOf(numberFormat.format(cTotal)) + " coins");
                }
                //else{
                //   Toast.makeText(HomeActivity.this,  "Select a lot to see the cost!", Toast.LENGTH_SHORT).show();
                //}
            }
        });

        //Search bar functionality
        sbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            //Display new results after the text in the search bar is changed
            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    setAdapter(editable.toString());
                }
                else{
                    pName.clear();
                    recyclerView.removeAllViews();
                }
            }
        });
        sbar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        //Button to sign out from the application
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intToMain = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intToMain);
            }
        });
        //Button to go to the buy token screen
        btnPackages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intToPay = new Intent(HomeActivity.this, PackageActivity.class);
                startActivity(intToPay);
            }
        });
        //Button to go to the update user information screen
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intToPay = new Intent(HomeActivity.this, UpdateActivity.class);
                startActivity(intToPay);
            }
        });
        //Button to go to the QR code scanner screen
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If the user has no parking space booked at the moment
                if (disable == false) {
                    if (lock == false) {
                        Intent intToPay = new Intent(HomeActivity.this, QRActivity.class)
                                .putExtra("parkTime", startTimeInMillis);
                        startActivity(intToPay);
                    }
                    //If the user has parking space booked
                    else {
                        if (ourTime < 999 && startTimeInMillis != 0) {
                            final DocumentReference docRef = db.collection("Lots").document(ourLot);
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    //If the document exists
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            //Get the cost to park per 15 minutes
                                            myCost = document.getLong("cost");
                                            //if (document.exists()){
                                            try {
                                                //If the lot specified in the QR code exists
                                                if (document.getBoolean(ourSpot)) {
                                                    //Get the user document for the current user
                                                    final DocumentReference docRefu = db.collection("User").document(email);
                                                    docRefu.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> tasku) {
                                                            if (tasku.isSuccessful()) {
                                                                DocumentSnapshot documentu = tasku.getResult();
                                                                if (documentu.exists()) {
                                                                    //Get the user's amount of tokens
                                                                    currAmt = documentu.getLong("TAmt");
                                                                    //If the amount of tokens is enough
                                                                    if ((myCost * (startTimeInMillis * 4 / (1000 * 3600))) <= currAmt) {
                                                                        boolean start = true;
                                                                        //Set a new amount of tokens
                                                                        double costOfParking = (myCost * ((double) startTimeInMillis * 4 / (1000 * 3600)));
                                                                        newAmt = currAmt - costOfParking;//(cost * ((double)pTime * 4/(1000 * 3600)));
                                                                        docRefu.update("TAmt", newAmt);
                                                                        //Set the lot to filled
                                                                        docRef.update(ourSpot, false);
                                                                        Intent serviceIntent = new Intent(HomeActivity.this, MyService.class)
                                                                                .putExtra("timeBooked", startTimeInMillis)
                                                                                .putExtra("lotName", ourLot)
                                                                                .putExtra("lotID", ourSpot);
                                                                        ContextCompat.startForegroundService(HomeActivity.this, serviceIntent);
                                                                        //startService(serviceIntent);
                                                                        startTimer();
                                                                    } else {
                                                                        Toast.makeText(HomeActivity.this, "Not enough tokens available!", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                } else {
                                                                    //Log.d(TAG, "No such document");
                                                                }
                                                            } else {
                                                                //Log.d(TAG, "get failed with ", task.getException());
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(HomeActivity.this, "This lot is already in use.", Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (NullPointerException e) {

                                            }
                                            //}
                                            //else{

                                            //}
                                        } else {
                                            //Log.d(TAG, "No such document");
                                            //Toast.makeText(QRActivity.this, "Wrong QR code scanned", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        //Log.d(TAG, "get failed with ", task.getException());
                                        //Toast.makeText(QRActivity.this, "Wrong QR code scanned", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                            Intent stopIntent = new Intent(HomeActivity.this, MyService.class)
                                    .putExtra("timeBooked", startTimeInMillis)
                                    .putExtra("lotName", ourLot)
                                    .putExtra("lotID", ourSpot);
                            ContextCompat.startForegroundService(HomeActivity.this, stopIntent);
                            startTimer();
                        } else {
                            Toast.makeText(HomeActivity.this, "Please wait for the time to end and try later!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else{
                    Toast.makeText(HomeActivity.this, "Please wait for the time to end or close the application and open using the notification!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("startTime")) {
                boolean str = extras.getBoolean("startTime", false);
                if(str == true){
                    String lotNum = extras.getString("lotID");
                    ourSpot = lotNum;
                    lotSelected.setText(lotNum);
                    startTimeInMillis = extras.getLong("timeBooked");
                    //double parkingCost = extras.getDouble("parkingCost");
                    //totalCost.setText(String.valueOf(parkingCost));
                    lock = true;
                    disable = true;
                    startTimer();
                }
            }
            if(extras.containsKey("currTime")){
                String lotNum = extras.getString("lotID");
                ourSpot = lotNum;
                ourLot = extras.getString("lotName");
                lotSelected.setText(lotNum);
                ourTime = extras.getLong("currTime");
                if(ourTime > 1000) {
                    ourTime = startTimeInMillis;
                    startTimer();
                }
                lock = true;
            }
        }
    }

    //Search bar dropdown display function
    private void setAdapter(final String searchString) {
        //Get the Lots collection to search through from the database
        collectionReference = db.collection("Lots");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                //Clear the search result array to remove the previous search results
                pName.clear();
                recyclerView.removeAllViews();
                int counter = 0;
                //Loop through the items in the database Lots collections
                for(DocumentSnapshot snapshot: queryDocumentSnapshots.getDocuments()){
                    String lot_id = snapshot.getId();
                    //If the parking lot name contains the searched string then add that parking lot to the search results
                    if(lot_id.toLowerCase().contains(searchString.toLowerCase())){
                        pName.add(lot_id);
                        counter++;
                    }
                    //Display a maximum of 15 results
                    if(counter == 15){
                        break;
                    }

                }
                //Display all the results in the dropdown
                searchAdapter = new SearchAdapter(HomeActivity.this, pName, onNoteListenerm);
                recyclerView.setAdapter(searchAdapter);
            }
        });
    }

    //Fucntions to display the results of the parking lot selected
    @Override
    public void onNoteClick(int position) {
        currLot = pName.get(position);
        sbar.setText(currLot);
        sbar.setSelection(sbar.getText().length());
        //Get details of the Parking selected from the database
        final DocumentReference docRef = db.collection("Lots").document(currLot);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //If the parking lot exists get the results
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        cost = document.getLong("cost");
                        try {
                            //Calculate the cst of parking for the given time
                            cTotal = ((double)startTimeInMillis * 4 * cost)/(3600 * 1000);
                            totalCost.setText(String.valueOf(numberFormat.format(cTotal)) + " coins");
                            Lot_open = true;
                            Map<String, Object> lotMap = document.getData();
                            Iterator<String> itr = lotMap.keySet().iterator();
                            String Mykey = "";
                            //While loop to show the free spaces in the parking lot
                            while (itr.hasNext())
                            {
                                String lot_name = itr.next();
                                try {
                                    if(document.getBoolean(lot_name) == true) {
                                        Mykey = Mykey + "   " + lot_name;
                                        Log.d(TAG, "issue for search");
                                    }
                                }
                                catch(RuntimeException e){

                                }
                            }
                            lotsAvailable.setText(Mykey);
                        }
                        catch(NullPointerException e){

                        }
                    }
                    else {
                        //Log.d(TAG, "No such document");
                    }
                }
                else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    //Display new time whenever countdown is updated
    private void updateCountdown(){
        int hours = (int) (startTimeInMillis / 1000) / 3600;
        int minutes = (int) ((startTimeInMillis/1000) % 3600) / 60;
        int seconds = (int) ((startTimeInMillis % 60000) / 1000);
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        timmer.setText(timeLeftFormatted);
    }
    //Start the timer
    public void startTimer(){
        //Update the countdown every second
        countDownTimer = new CountDownTimer(startTimeInMillis, 1000) {
            @Override
            public void onTick(long l) {
                startTimeInMillis = l;
                updateCountdown();
            }

            @Override
            public void onFinish() {
                lock = false;
            }
        }.start();
    }
    public void stopTimer(){

    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
