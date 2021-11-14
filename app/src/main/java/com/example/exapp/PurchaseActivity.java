package com.example.exapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class PurchaseActivity extends AppCompatActivity {

    private Button backbutton;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .removeValue();

        backbutton = (Button) findViewById(R.id.buttonDone);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PurchaseActivity.this,NavigationActivity.class));
            }
        });
    }
}