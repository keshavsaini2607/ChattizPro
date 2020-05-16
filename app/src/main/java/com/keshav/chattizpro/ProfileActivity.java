package com.keshav.chattizpro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private String reciever_user_id;
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mRequestSendBtn,mDeclineBtn;

    private Toolbar mToolbar;

    //Database Refereces
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //By this line we have recieved the user id of the account that has been clicked on the all users page
        // this user id is being sent by the intent that we have been used in the UsersActivity class
        reciever_user_id = getIntent().getExtras().get("visit_user_id").toString();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(reciever_user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileName = findViewById(R.id.profile_disp_name);
        mProfileImage = findViewById(R.id.profile_image);
//        mProfileStatus = findViewById(R.id.profile_status);
//        mProfileFriendsCount = findViewById(R.id.profile_friends_count);
        mRequestSendBtn = findViewById(R.id.friend_request_btn);
        mDeclineBtn = findViewById(R.id.delete_request_btn);

        mCurrent_state = "Not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please Wait");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);


        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String user_status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
//                mProfileStatus.setText(user_status);

                Picasso.get().load(image).placeholder(R.drawable.dpvector).into(mProfileImage);


                if (mCurrent_state.equals("Friends"))
                {
                    mCurrent_state = "Not_friends";
                    mRequestSendBtn.setText("Send Friends Request");

                    mDeclineBtn.setVisibility(View.INVISIBLE);
                    mDeclineBtn.setEnabled(false);
                }

                //--------------------------Friends List / Request Feature -------------------------
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(reciever_user_id)) {
                            String req_type = dataSnapshot.child(reciever_user_id).child("request_type").getValue().toString();
                            if (req_type.equals("recieved")) {
                                mCurrent_state = "req_recieved";
                                mRequestSendBtn.setText("Accept Friend Request");
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mRequestSendBtn.setText("Cancel Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                            mProgressDialog.dismiss();

                        }else
                        {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(reciever_user_id))
                                    {
                                        mCurrent_state = "friends";
                                        mRequestSendBtn.setText("Unfriend this person");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }else if (dataSnapshot.hasChild(mCurrentUser.getUid()))
                                    {
                                        mCurrent_state = "Not_friends";
                                        mRequestSendBtn.setText("Send Friend Request");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    mProgressDialog.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRequestSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestSendBtn.setEnabled(false);

                // - ----------------------- Not Friends State When two users are not friends ----------------------------
                if (mCurrent_state.equals(mCurrent_state)) {

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(reciever_user_id).push();
                    String newNotificatoinId = newNotificationRef.getKey();

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("From",mCurrentUser.getUid());
                    notificationData.put("Type","request");

                    Map requestMap = new HashMap<>();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + reciever_user_id + "/request_type" , "sent");
                    requestMap.put("Friend_req/" + reciever_user_id + "/" + mCurrentUser.getUid() + "/request_type" , "recieved");
                    requestMap.put("notifications/" + reciever_user_id + "/" + newNotificatoinId , notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        if (databaseError != null)
                        {
                            Toast.makeText(ProfileActivity.this, "There was an error sending request", Toast.LENGTH_SHORT).show();
                        }

                            mRequestSendBtn.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mRequestSendBtn.setText("Cancel Request");

                        }
                    });

                }
                // - ----------------------- Cancel Friend Request ----------------------------
                if (mCurrent_state.equals("req_sent")) {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(reciever_user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(reciever_user_id).child(mCurrentUser.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mRequestSendBtn.setEnabled(true);
                                                    mCurrent_state = "Not_friends";
                                                    mRequestSendBtn.setText("Send Friend Request");

                                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                                    mDeclineBtn.setEnabled(false);
                                                }
                                            });

                                }
                            });
                }

                //---------------------- Program to accept request when it is recieved------------------------
                if (mCurrent_state.equals("req_recieved")) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + reciever_user_id + "/date" ,currentDate);
                    friendsMap.put("Friends/" + reciever_user_id +"/" + mCurrentUser.getUid() + "/date" , currentDate);

                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" +reciever_user_id , null);
                    friendsMap.put("Friend_req/" + reciever_user_id + "/" + mCurrentUser.getUid() , null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null)
                            {
                                mRequestSendBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mRequestSendBtn.setText("Unfriend This Person");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }else{
                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

                //-------------------------- REMOVE FRIEND FEATURE------------------------

                if (mCurrent_state.equals("friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" +reciever_user_id , null);
                    unfriendMap.put("Friends/" + reciever_user_id  + "/" + mCurrentUser.getUid() , null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null)
                            {
                                mCurrent_state = "Not_friends";
                                mRequestSendBtn.setText("Send Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }else{
                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                            mRequestSendBtn.setEnabled(true);
                        }
                    });
                }

            }
        });

        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendRequestDatabase.child(reciever_user_id).child(mCurrentUser.getUid()).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(reciever_user_id).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mCurrent_state = "Not_friends";
                                        mRequestSendBtn.setText("Send Friend Request");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                });
                            }
                        });
            }
        });

    }
}
