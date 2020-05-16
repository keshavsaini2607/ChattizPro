package com.keshav.chattizpro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

//import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class AccountSettings extends AppCompatActivity {

    private Button mStatusBtn, dpbtn;
    private ImageView mProfileImg;
    private TextView mName;
    private TextView mStatus;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mStorageDatabase;
    private ImageView mSignout;
    private ProgressDialog mProgressDialog;
    private static final int GALLERY_PICK = 1;

    private Toolbar mToolbar;
    //Storage Reference
    private StorageReference mImageStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mProfileImg = findViewById(R.id.profile_image);
        mStatus = findViewById(R.id.acc_status);
        mName = findViewById(R.id.acc_name);
        mSignout = findViewById(R.id.signoutprofile);
        mProgressDialog = new ProgressDialog(this);
        dpbtn = findViewById(R.id.dpbtn);
        mStatusBtn = findViewById(R.id.status_acc);

        mAuth = FirebaseAuth.getInstance();

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mStorageDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        mStorageDatabase.keepSynced(true);

        mToolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //This wll store the image using the onActivityResult method
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mStorageDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);


                //if (!image.equals("default")) {
                //Here we are using picasso library form github to show the image using the url that we have stored into our database
                //this line will set the image into our app by getting it from firebase
                // }
                //mProfileImg.setImageURI(null);
               // Picasso.get().load(image).placeholder(R.drawable.dpvector).into(mProfileImg);
                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dpvector).into(mProfileImg, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.dpvector).into(mProfileImg);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(AccountSettings.this, StatusActivity.class);
                String status_value = mStatus.getText().toString();
                //putextra is sending the value of status to status activity
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
            }
        });


        mSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.setTitle("Signing Out");
                mProgressDialog.setMessage("Please wait while we sign you out");
                mProgressDialog.show();

                FirebaseAuth.getInstance().signOut();
                Toast.makeText(AccountSettings.this, "Logout Successful", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(AccountSettings.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        });

        dpbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY_PICK);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setFixAspectRatio(true)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(AccountSettings.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("please wait!");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_file = new File(resultUri.getPath());
                String currentUser = FirebaseAuth.getInstance().getUid();


                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200).setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);


                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                final StorageReference filePath = mImageStorage.child("profile_images").child(currentUser + ".jpg");

                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(currentUser + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    final Uri download_uri = task.getResult();

                                    thumb_filepath.putBytes(thumb_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            thumb_filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    Uri thumb_uri = task.getResult();
                                                    if (task.isSuccessful()) {
                                                        Map updateHash = new HashMap();
                                                        updateHash.put("image", download_uri.toString());
                                                        updateHash.put("thumb_image", thumb_uri.toString());
                                                        mStorageDatabase.updateChildren(updateHash).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    mProgressDialog.dismiss();
                                                                    Toast.makeText(AccountSettings.this, "Success Uploading", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(AccountSettings.this, "Error in uploading Thumbnail", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });


                                }
                            });


                        } else {
                            Toast.makeText(AccountSettings.this, "Error", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}





