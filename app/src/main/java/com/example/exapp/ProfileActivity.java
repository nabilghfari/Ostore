package com.example.exapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private Button logoutbtn, editbutton;

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userid;
    private ImageView btnprofBack;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAnalytics firebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference().child("images/" + userid);

        try{
            final File localFile = File.createTempFile("imageprof", "jpg");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            ((CircleImageView)findViewById(R.id.profilePic)).setImageBitmap(bitmap);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Initialize
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        //Set Page
        bottomNavigationView.setSelectedItemId(R.id.profact);

        //ItemSelectListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.storeact:
                        startActivity(new Intent(getApplicationContext(),NavigationActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.cartact:
                        startActivity(new Intent(getApplicationContext(), CartActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profact:
                        return true;
                }
                return false;
            }
        });

        logoutbtn = (Button) findViewById(R.id.buttonlogout);
        logoutbtn.setOnClickListener(this);

        editbutton = (Button) findViewById(R.id.buttonEdit);
        editbutton.setOnClickListener(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userid = user.getUid();

        btnprofBack = (ImageView) findViewById(R.id.profBack);
        btnprofBack.setOnClickListener(this);

        final TextView usernameText = (TextView) findViewById(R.id.usernameValue);
        final TextView emailText = (TextView) findViewById(R.id.emailValue);
        final TextView fullnameText = (TextView) findViewById(R.id.fullnameValue);
        final TextView phoneText = (TextView) findViewById(R.id.phoneValue);
        final TextView locationText = (TextView) findViewById(R.id.locationValue);

        reference.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User userprofile = snapshot.getValue(User.class);
                if (userprofile != null){
                    String username = userprofile.userName;
                    String fullname = userprofile.fullName;
                    String phone = userprofile.phoneNumber;
                    String location = userprofile.location;


                    usernameText.setText(username);
                    emailText.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    fullnameText.setText(fullname);
                    phoneText.setText(phone);
                    locationText.setText(location);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this,"Something Wrong With The Data", Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonlogout:
                FirebaseAuth.getInstance().signOut();
                Bundle bundle = new Bundle();
                bundle.putString("logout","Logout Button");
                firebaseAnalytics.logEvent("user_logout",bundle);
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                break;
            case R.id.profBack:
                startActivity(new Intent(ProfileActivity.this, NavigationActivity.class));
                break;
            case R.id.buttonEdit:
                startActivity(new Intent(ProfileActivity.this, EditprofActivity.class));
                break;
        }
    }
}