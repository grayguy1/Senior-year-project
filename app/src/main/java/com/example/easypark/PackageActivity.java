package com.example.easypark;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.easypark.Config.Config;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;

public class PackageActivity extends AppCompatActivity {

    public static final int PAYPAL_REQUEST_CODE = 7171;
    //Create a paypal config
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);
    //Declarations
    Button btnpack1;
    Button btnpack2;
    Button btnpack3;

    String amount = "";
    String token = "";
    //On destroy to release the paypal service
    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Go to the paypal service screen
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
        //Button instantiations
        btnpack1 = findViewById(R.id.pack1);
        btnpack2 = findViewById(R.id.pack2);
        btnpack3 = findViewById(R.id.pack3);
        //Package button details
        btnpack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processPayment("10", "40");
            }
        });
        btnpack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processPayment("50", "250");
            }
        });
        btnpack3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processPayment("100", "600");
            }
        });
    }
    //Process the paypal payment
    private void processPayment(String amt, String tkn) {
        amount = amt;
        token = tkn;
        //Register the payment
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(String.valueOf(amount)), "USD",
                "Pay for parking", PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                //Get a confirmation for the transaction
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirmation != null){
                    try {
                        //Call the payment details function
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        startActivity(new Intent(this, PaymentDetails.class)
                                .putExtra("PaymentDetails", paymentDetails)
                                .putExtra("PaymentAmount", amount)
                                .putExtra("TokenAmount", token)
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            //If back button pressed then generate a toast
            else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
            }
        }
        //If any error occurs
        else if(resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
            Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
        }
    }
}
