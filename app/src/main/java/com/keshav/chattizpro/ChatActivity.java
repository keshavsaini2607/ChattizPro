package com.keshav.chattizpro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageRecieverId, messageRecieverName,messageRecieverImage,messageSenderId;

    private TextView userName,userLastSeen;
    private CircleImageView userImage;

    private Toolbar chatToolbar;

    private RecyclerView mRecyclerView;

    private ImageButton messageSendButton;
    private EditText messageEditText;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    //For message
    private  final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdapter mMessageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();



        RootRef = FirebaseDatabase.getInstance().getReference();

        messageRecieverId = getIntent().getExtras().get("user_id").toString();
        messageRecieverName = getIntent().getExtras().get("user_name").toString();
        messageRecieverImage = getIntent().getExtras().get("user_image").toString();


        InitializrControllers();

        userName.setText(messageRecieverName);
        Picasso.get().load(messageRecieverImage).placeholder(R.drawable.avatar).into(userImage);
        userLastSeen.setText("Last Seen");

        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    private void InitializrControllers() {

        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_toolbar,null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.profile_custom_dp);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_profile_last_seen);


        //This is for displaying user messages using the classes Messages and MessageAdapter in the main Java classes folder
        messageSendButton = findViewById(R.id.imageButton2);
        messageEditText = findViewById(R.id.input);

        mMessageAdapter = new MessageAdapter(messagesList);
        mRecyclerView = findViewById(R.id.private_message_list);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdapter);

    }

    private void displayLastSeen()
    {
        RootRef.child("Users").child(messageSenderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("userState").hasChild("state"))
                {
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online"))
                    {
                       userLastSeen.setText("online");
                    }
                    else if (state.equals("offline"))
                    {
                        userLastSeen.setText("Last Seen: " + time);
                    }
                }
                else {
                   userLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


        RootRef.child("Messages").child(messageSenderId).child(messageRecieverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        mMessageAdapter.notifyDataSetChanged();

                        mRecyclerView.scrollToPosition(messagesList.size() - 1);

                        displayLastSeen();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    //This method works to send the messages into the database when the send button on the chat activity is pressed

    private void sendMessage()
    {
        String messageText = messageEditText.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Please Input Some Message", Toast.LENGTH_SHORT).show();
        }else
        {
            String messageSenderRef = "Messages/" + messageSenderId + "/" + messageRecieverId;
            String messageRecieverRef = "Messages/" + messageRecieverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderId).child(messageRecieverId).push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId , messageTextBody);
            messageBodyDetails.put(messageRecieverRef + "/" + messagePushId , messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    }else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    messageEditText.setText("");
                }
            });



        }
    }
}
