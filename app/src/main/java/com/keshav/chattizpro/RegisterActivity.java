package com.keshav.chattizpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mUname;
    private EditText mEmail;
    private EditText mPassword;
    private Button mRegBtn;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mProgressDialog =new ProgressDialog(this);


        mUname = findViewById(R.id.reg_uname);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_pass);
        mRegBtn = findViewById(R.id.reg_btn);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String UserName = mUname.getText().toString().trim();
                String Email = mEmail.getText().toString().trim();
                String Password = mPassword.getText().toString().trim();

                register_user(UserName,Email,Password);
            }
        });
    }

    private void register_user(final String userName, String email, String password) {

        mProgressDialog.setTitle("Signing Up");
        mProgressDialog.setMessage("Please wait while we create your account");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                    String uid=current_user.getUid();
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    HashMap<String,String> userMap=new HashMap<>();
                    userMap.put("name",userName);
                    userMap.put("status","Hey There i am using chattizPro");
                    userMap.put("image","default");
                    userMap.put("thumb_image","defalut");
                    userMap.put("device_token",device_token);

                    mDatabaseReference.setValue(userMap);

                    mProgressDialog.dismiss();
                    Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }else{
                    mProgressDialog.hide();
                    Toast.makeText(RegisterActivity.this,"There has been some error while registering",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
