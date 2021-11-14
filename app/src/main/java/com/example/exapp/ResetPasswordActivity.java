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
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailText;
    private Button resetButton;
    private ProgressBar progressbar;
    private ImageView bannerres;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        emailText = (EditText) findViewById(R.id.emailforgot);
        resetButton = (Button) findViewById(R.id.buttonreset);
        progressbar = (ProgressBar) findViewById(R.id.progressResBar);

        bannerres = (ImageView) findViewById(R.id.bannerRes);
        bannerres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResetPasswordActivity.this, MainActivity.class));
            }
        });

        auth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email = emailText.getText().toString().trim();
        if (email.isEmpty()){
            emailText.setError("Enter Email of The Account You Want To Reset");
            emailText.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailText.setError("Please Enter Valid Email");
            emailText.requestFocus();
            return;
        }
        progressbar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ResetPasswordActivity.this,"Please Check Your Email To Reset Password!", Toast.LENGTH_LONG).show();
                    progressbar.setVisibility(View.GONE);
                }
                else{
                    Toast.makeText(ResetPasswordActivity.this,"Email Not Found, Try Again!", Toast.LENGTH_LONG).show();
                    progressbar.setVisibility(View.GONE);
                }
            }
        });
    }
}