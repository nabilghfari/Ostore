package com.example.exapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.analytics.FirebaseAnalytics;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAnalytics firebaseAnalytics;
    Animation zooming;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        zooming = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom);
        img = findViewById(R.id.imageAnim);
        img.startAnimation(zooming);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        },4000);
    }
}