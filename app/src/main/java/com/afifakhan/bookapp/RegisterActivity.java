package com.afifakhan.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.afifakhan.bookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //view binding
    private ActivityRegisterBinding binding;
    //firebase authentication
    private FirebaseAuth firebaseAuth;
    //progress dialog
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click begin register
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();

            }
        });
    }

            private String name="",email="",password="";
            private void validateData() {
                /*before creating account lets do some data validation */

                //get data
                name = binding.nameEt.getText().toString().trim();
                email = binding.emailEt.getText().toString().trim();
                password = binding.passwordEt.getText().toString().trim();
                String cPassword = binding.cPasswordEt.getText().toString().trim();

                //validate data
                if (TextUtils.isEmpty(name)){
                    Toast.makeText(RegisterActivity.this, "Enter your name...!", Toast.LENGTH_SHORT).show();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(RegisterActivity.this, "Invalid email pattern", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this, "Enter your password...!", Toast.LENGTH_SHORT).show();

                } else if (TextUtils.isEmpty(cPassword)) {
                    Toast.makeText(RegisterActivity.this, "Enter Confirm password...!", Toast.LENGTH_SHORT).show();

                } else if (!password.equals(cPassword)) {
                    Toast.makeText(RegisterActivity.this, "Password doesn't match...!", Toast.LENGTH_SHORT).show();
                }
                else{
                    createUserAccount();
                }

            }
            private void createUserAccount() {
                //show progress
                progressDialog.setMessage("Creating account...");
                progressDialog.show();

                //create user in firebase auth
                firebaseAuth.createUserWithEmailAndPassword(email,password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                //account creation success, now add in firebase realtime database
                                updateUserInfo();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                //account creation failed
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving User Info...");

        //timestamp
        long timestamp = System.currentTimeMillis();

        //get current user uid, since user is registered so we can get now
        String uid=firebaseAuth.getUid();

        //set add data in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", "");//
        hashMap.put("userType", "user");//possible values are user, Admin: will make admin manually in firebase realtime database by changing this value
        hashMap.put("timestamp", timestamp);

        //set data to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data added to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account Created...", Toast.LENGTH_SHORT).show();
                        //since user account i created so start dashboard of user
                        startActivity(new Intent(RegisterActivity.this,DashboardUserActivity.class));
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //data failed  adding to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }
}