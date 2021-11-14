package com.example.exapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView bannerreg;
    private Button buttonreg;
    private EditText fullnamereg, usernamereg, emailreg, passwordreg, phonereg, locationreg;
    private ProgressBar progressBar;

    private FirebaseAnalytics firebaseAnalytics;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();

        bannerreg = (ImageView) findViewById(R.id.bannerreg);
        bannerreg.setOnClickListener(this);

        buttonreg = (Button) findViewById(R.id.buttonreg);
        buttonreg.setOnClickListener(this);

        fullnamereg = (EditText) findViewById(R.id.fullnamereg);
        usernamereg = (EditText) findViewById(R.id.usernamereg);
        emailreg = (EditText) findViewById(R.id.emailreg);
        passwordreg = (EditText) findViewById(R.id.passwordreg);
        phonereg = (EditText) findViewById(R.id.phonereg);
        locationreg = (EditText) findViewById(R.id.locationreg);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bannerreg:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.buttonreg:
                registerUser();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.METHOD, "User Register");
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }

    private void registerUser() {
        String fullName = fullnamereg.getText().toString().trim();
        String userName = usernamereg.getText().toString().trim();
        String email = emailreg.getText().toString().trim();
        String password = passwordreg.getText().toString().trim();
        String phoneNumber = phonereg.getText().toString().trim();
        String location = locationreg.getText().toString().trim();

        if (fullName.isEmpty()){
            fullnamereg.setError("Please Enter Full Name");
            fullnamereg.requestFocus();
            return;
        }
        if (phoneNumber.isEmpty()){
            phonereg.setError("Please Enter Phone Number");
            phonereg.requestFocus();
            return;
        }
        if (location.isEmpty()){
            locationreg.setError("Please Enter Your Location");
            locationreg.requestFocus();
            return;
        }
        if (userName.isEmpty()){
            usernamereg.setError("Please Enter Username");
            usernamereg.requestFocus();
            return;
        }
        if (email.isEmpty()){
            emailreg.setError("Please Enter Email");
            emailreg.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailreg.setError("Email Invalid! Use a Valid Email");
            emailreg.requestFocus();
            return;
        }
        if (password.isEmpty()){
            passwordreg.setError("Please Enter Password");
            passwordreg.requestFocus();
            return;
        }
        if (password.length() < 6){
            passwordreg.setError("Password Should be More Than 6 Characters Length!");
            passwordreg.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            User user = new User(fullName, userName, phoneNumber, location);


                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "User Registration Complete!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    else{
                                        Toast.makeText(RegisterActivity.this, "User Registration Failed! Try Again!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });

                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "Failed to Register! Try Again!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
        });
    }
}