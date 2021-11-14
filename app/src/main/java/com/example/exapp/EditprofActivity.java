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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditprofActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editfullname, editusername, editphone, editlocation;
    private Button confbutton;
    private ImageView backprof;
    private String userid;
    private CircleImageView profpic;
    public Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprof);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        editfullname = (EditText) findViewById(R.id.editFullname);
        editusername = (EditText) findViewById(R.id.editUsername);
        editlocation = (EditText) findViewById(R.id.editLocation);
        editphone = (EditText) findViewById(R.id.editPhone);

        confbutton = (Button) findViewById(R.id.buttonConf);
        confbutton.setOnClickListener(this);

        backprof = (ImageView) findViewById(R.id.profBack);
        backprof.setOnClickListener(this);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();



        profpic = (CircleImageView) findViewById(R.id.profilePic);
        profpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePic();
            }
        });

    }

    private void choosePic() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            profpic.setImageURI(imageUri);
            uploadPic();
        }
    }

    private void uploadPic() {
        final ProgressDialog prog = new ProgressDialog(this, R.style.MyDialogTheme2);
        prog.setTitle("Updating Image...");
        prog.show();

        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference imageUp = storageReference.child("images/" + userid);

        imageUp.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        prog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content),"Profile Pic Updated", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        prog.dismiss();
                        Toast.makeText(EditprofActivity.this,"Profile Pic Update Failed", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progrespercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        prog.setMessage("Progress: " + (int) progrespercent + "%");
                    }
                });
    }

    private void updateUser() {

        Bundle bundle = new Bundle();
        bundle.putString("update","Update Button");
        firebaseAnalytics.logEvent("user_update",bundle);

        String fullName = editfullname.getText().toString().trim();
        String userName = editusername.getText().toString().trim();
        String location = editlocation.getText().toString().trim();
        String phoneNumber = editphone.getText().toString().trim();

        if (fullName.isEmpty()){
            editfullname.setError("Please Enter Updated Full Name");
            editfullname.requestFocus();
            return;
        }
        if (userName.isEmpty()){
            editusername.setError("Please Enter Updated Username");
            editusername.requestFocus();
            return;
        }
        if (location.isEmpty()){
            editlocation.setError("Please Enter Updated Location");
            editlocation.requestFocus();
            return;
        }
        if (phoneNumber.isEmpty()){
            editphone.setError("Please Enter Update Phone Number");
            editphone.requestFocus();
            return;
        }

        User usrupdt = new User(fullName, userName, phoneNumber, location);

        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(usrupdt).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(EditprofActivity.this, "User Update Complete!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(EditprofActivity.this, "User Update Failed! Try Again!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.buttonConf:
                updateUser();
                startActivity(new Intent(EditprofActivity.this, ProfileActivity.class));
                break;
            case R.id.profBack:
                startActivity(new Intent(EditprofActivity.this, ProfileActivity.class));
                break;
        }
    }
}