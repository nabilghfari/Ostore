package com.example.exapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {



    private TextView textregister, textforgotpass;
    private EditText emailtext, passwordtext;
    private Button loginbutton;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        textregister = (TextView) findViewById(R.id.textregister);
        textregister.setOnClickListener(this);

        textforgotpass = (TextView) findViewById(R.id.textforgotpass);
        textforgotpass.setOnClickListener(this);

        loginbutton = (Button) findViewById(R.id.buttonlogin);
        loginbutton.setOnClickListener(this);

        emailtext = (EditText) findViewById(R.id.email);
        passwordtext = (EditText) findViewById(R.id.password);

        progressBar = (ProgressBar) findViewById(R.id.progressBarlogin);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.textregister:
                startActivity(new Intent(this, RegisterActivity.class));
                break;

            case R.id.buttonlogin:
                userLogin();
                break;

            case R.id.textforgotpass:
                startActivity(new Intent(this, ResetPasswordActivity.class));
                break;
        }

    }

    private void userLogin() {
        String emaillog = emailtext.getText().toString().trim();
        String passlog = passwordtext.getText().toString().trim();

        if (emaillog.isEmpty()){
            emailtext.setError("Email Required!");
            emailtext.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emaillog).matches()){
            emailtext.setError("Enter Valid Email!");
            emailtext.requestFocus();
            return;
        }
        if (passlog.isEmpty()) {
            passwordtext.setError("Password Required!");
            passwordtext.requestFocus();
            return;
        }
        if (passlog.length() < 6){
            passwordtext.setError("Password is more than 6 characters");
            passwordtext.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(emaillog, passlog).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user.isEmailVerified()){
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.METHOD, "User Login");
                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                        startActivity(new Intent(MainActivity.this, NavigationActivity.class));
                    }
                    else{
                        user.sendEmailVerification();
                        Toast.makeText(MainActivity.this, "Verification Send, Check Your Email", Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
                else{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Failed To Login! Make Sure Email and Password Correct!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}