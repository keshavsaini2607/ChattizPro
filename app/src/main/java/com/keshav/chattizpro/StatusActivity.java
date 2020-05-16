package com.keshav.chattizpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {


    private EditText mStatus;
    private Button mButton,mDefStatusBtn;
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mStatus = findViewById(R.id.status_input);
        mButton = findViewById(R.id.status_save_btn);
        mDefStatusBtn = findViewById(R.id.def_status_btn);

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid=mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);


        //This here is getting the value of status that we have sent from settings activity using getIntent
        String status_value=getIntent().getStringExtra("status_value");

        // mEditText.getText().setText("status_value");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Progress
                mProgressDialog=new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("Saving Changes");
                mProgressDialog.setMessage("PLease wait while we proceed");
                mProgressDialog.show();
                final String status=mStatus.getText().toString().trim();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgressDialog.dismiss();
                            Toast.makeText(StatusActivity.this,"Status Updated",Toast.LENGTH_LONG).show();

                        }
                        else{
                            mProgressDialog.hide();
                            Toast.makeText(StatusActivity.this,"Something went wrong",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        mDefStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("Saving Default Status");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                mStatusDatabase.child("status").setValue("Hey There i Am using ChattizPro!").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            mProgressDialog.dismiss();
                            Toast.makeText(StatusActivity.this,"Set Status Successfull",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            mProgressDialog.hide();
                            Toast.makeText(StatusActivity.this,"Something went wrong",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
