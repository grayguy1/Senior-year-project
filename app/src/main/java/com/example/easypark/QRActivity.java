package com.example.easypark;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRActivity extends AppCompatActivity {
    //Declarations
    FirebaseFirestore db;
    FirebaseAuth mFirebaseAuth;

    SurfaceView surfacev;
    CameraSource camerasrc;
    BarcodeDetector barcodedetector;

    String email = "";

    String currLot = "";
    String currSpace = "";
    Long currAmt;
    Long cost;
    double newAmt;
    String my_time;
    Long pTime = 1L;
    Boolean found = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Instantiations
        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        email = mFirebaseAuth.getCurrentUser().getEmail();

        final Map<String, Object> User = new HashMap<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        surfacev = (SurfaceView)findViewById(R.id.qrcamera);

        Intent intent = getIntent();
        pTime = intent.getLongExtra("parkTime", 1L);
        //Create a barcode detector object
        barcodedetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();
        //Create a camera source object
        camerasrc = new CameraSource.Builder(this, barcodedetector)
                .setRequestedPreviewSize(640, 480).build();
        //View screen
        surfacev.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            //Function that runs when the surface is created which is whenever the screen is opened
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    //Start the camera
                    camerasrc.start(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                camerasrc.stop();
            }
        });
        //Process the barcode
        barcodedetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }
            //Get the barcode
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                //Change the QR code to a list
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                //If size of the QR code array is not 0 and a QR code has not been found
                if(qrCodes.size() != 0 && found == true){
                    //Get the Lots collection with the parking lot specified the QR code
                    String[] qrResults = qrCodes.valueAt(0).rawValue.split("_");
                    currLot = qrResults[0].replace('-', ' ');
                    currSpace = qrResults[1];
                    final DocumentReference docRef = db.collection("Lots").document(currLot);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            //If the document exists
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    //Get the cost to park per 15 minutes
                                    cost = document.getLong("cost");
                                    //if (document.exists()){
                                        try {
                                            //If the lot specified in the QR code exists
                                            if (document.getBoolean(currSpace)) {
                                                //Set found to false
                                                found = false;
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
                                                                if ((cost * (pTime * 4/(1000 * 3600))) <= currAmt) {
                                                                    boolean start = true;
                                                                    //Set a new amount of tokens
                                                                    double costOfParking = (cost * ((double)pTime * 4/(1000 * 3600)));
                                                                    newAmt = currAmt - costOfParking;//(cost * ((double)pTime * 4/(1000 * 3600)));
                                                                    docRefu.update("TAmt", newAmt);
                                                                    //Set the lot to filled
                                                                    docRef.update(currSpace, false);
                                                                    Intent serviceIntent = new Intent(QRActivity.this, MyService.class)
                                                                            .putExtra("timeBooked", pTime)
                                                                            .putExtra("lotName", currLot)
                                                                            .putExtra("lotID", currSpace);
                                                                    ContextCompat.startForegroundService(QRActivity.this, serviceIntent);
                                                                    //startService(serviceIntent);
                                                                    //Go to the home screen
                                                                    Intent intToHome = new Intent(QRActivity.this, HomeActivity.class)
                                                                            .putExtra("startTime", start)
                                                                            .putExtra("lotID", currSpace)
                                                                            .putExtra("timeBooked", pTime)
                                                                            .putExtra("parkingCost", costOfParking);
                                                                    startActivity(intToHome);
                                                                } else {
                                                                    Toast.makeText(QRActivity.this, "Not enough tokens available!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                //Log.d(TAG, "No such document");
                                                            }
                                                        } else {
                                                            //Log.d(TAG, "get failed with ", task.getException());
                                                        }
                                                    }
                                                });
                                            }
                                            else {
                                                Toast.makeText(QRActivity.this, "This lot is already in use.", Toast.LENGTH_SHORT).show();
                                            }
                                        }catch(NullPointerException e){

                                        }
                                    //}
                                    //else{

                                    //}
                                } else {
                                    //Log.d(TAG, "No such document");
                                    Toast.makeText(QRActivity.this, "Wrong QR code scanned", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                //Log.d(TAG, "get failed with ", task.getException());
                                Toast.makeText(QRActivity.this, "Wrong QR code scanned", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

    }
}
