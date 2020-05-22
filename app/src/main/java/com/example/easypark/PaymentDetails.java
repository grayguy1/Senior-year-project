package com.example.easypark;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentDetails extends AppCompatActivity {
    //Declarations
    FirebaseFirestore db;
    FirebaseAuth mFirebaseAuth;
    TextView txtId, txtAmount, txtStatus, txtToken, txtTotal;

    String email = "";
    Long currAmt;
    Long newAmt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Instantiations
        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        email = mFirebaseAuth.getCurrentUser().getEmail();

        txtId = (TextView)findViewById(R.id.txtId);
        txtAmount = (TextView)findViewById(R.id.txtAmount);
        txtStatus = (TextView)findViewById(R.id.txtStatus);
        txtToken = findViewById(R.id.txtToken);
        txtTotal = findViewById(R.id.txtTotal);


        //Get the intent that this activity came from
        Intent intent = getIntent();
        //Get the variables being passed through and put them in a JSON object
        try{
            JSONObject jsonObject = new JSONObject(intent.getStringExtra("PaymentDetails"));
            showDetails(jsonObject.getJSONObject("response"), intent.getStringExtra("PaymentAmount"),
                    intent.getStringExtra("TokenAmount"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //Function to display the details of the transaction
    private void showDetails(JSONObject response, String paymentAmount, final String tokenAmount) {
        try {
            //Set the text to the detail parameters
            txtId.setText("ID: " + response.getString("id"));
            txtStatus.setText("State: " + response.getString("state"));
            txtAmount.setText("Amount: " + "$" + paymentAmount);
            txtToken.setText("Tokens purchased: " + tokenAmount);
            //Get the current user document from firebase
            final DocumentReference docRef = db.collection("User").document(email);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //Update the amount of tokens that the user has
                            currAmt = document.getLong("TAmt");
                            newAmt = currAmt + Integer.parseInt(tokenAmount);
                            docRef.update("TAmt", newAmt);
                            txtTotal.setText("Total tokens: " + newAmt);
                        } else {
                            //Log.d(TAG, "No such document");
                        }
                    } else {
                        //Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
