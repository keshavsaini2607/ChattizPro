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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private Button mLoginBtn;

    private FirebaseAuth mAuth;

    private DatabaseReference mUserDatabase;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mProgressDialog =new ProgressDialog(this);

        mEmail = findViewById(R.id.gmail_login);
        mPassword = findViewById(R.id.pass_login);
        mLoginBtn = findViewById(R.id.login_btn);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = mEmail.getText().toString().trim();
                String Password = mPassword.getText().toString().trim();

                if (Password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
                }

                loginUser(Email,Password);
            }
        });
    }

    private void loginUser(String email, String password) {
        mProgressDialog.setTitle("Logging In");
        mProgressDialog.setMessage("Please wait while we check your details");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    mProgressDialog.dismiss();

                    final String currentUid = mAuth.getCurrentUser().getUid();

                    String device_token = FirebaseInstanceId.getInstance().getToken();
                    mUserDatabase.child(currentUid).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent LoginIntent=new Intent(LoginActivity.this,MainActivity.class);
                            LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(LoginIntent);
                            finish();
                        }
                    });
                }else{
                    mProgressDialog.hide();
                    Toast.makeText(LoginActivity.this,"There has been some error while logging you in",Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
