package com.keshav.chattizpro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;

    //Firebase Stuff
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    public MessageAdapter(List<Messages> userMessageList)
    {
        this.userMessageList = userMessageList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText , recieverMessageText;
        public CircleImageView recieverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            recieverMessageText = itemView.findViewById(R.id.reciever_message_text);
            recieverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout,parent,false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image"))
                {
                    String recieverProfileImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(recieverProfileImage).placeholder(R.drawable.avatar).into(holder.recieverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text"))
        {
            holder.recieverMessageText.setVisibility(View.INVISIBLE);
            holder.recieverProfileImage.setVisibility(View.INVISIBLE);

            if (fromUserId.equals(messageSenderId))
            {
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setText(messages.getMessage());
                holder.senderMessageText.setTextColor(0xFFFFFFFF);
            }
            else {
                holder.senderMessageText.setVisibility(View.INVISIBLE);

                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.recieverMessageText.setVisibility(View.VISIBLE);

                holder.recieverMessageText.setBackgroundResource(R.drawable.reciever_message_layout);
                holder.recieverMessageText.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


}
