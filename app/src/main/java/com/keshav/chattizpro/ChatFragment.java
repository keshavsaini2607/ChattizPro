package com.keshav.chattizpro;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private View chatsView;
    private RecyclerView mChatList;

    private DatabaseReference chatRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chatsView =  inflater.inflate(R.layout.fragment_chat, container, false);

        mChatList = chatsView.findViewById(R.id.chat_list);
        mChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        chatRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return chatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(chatRef,Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends,chatViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, chatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatViewHolder holder, int position, @NonNull Friends model) {

                final String userIds = getRef(position).getKey();
                final String[] retImage = {"default"};
                UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                       if (dataSnapshot.exists())
                       {
                           if (dataSnapshot.hasChild("image"))
                           {
                               retImage[0] = dataSnapshot.child("image").getValue().toString();
                               Picasso.get().load(retImage[0]).placeholder(R.drawable.avatar).into(holder.profileImage);
                           }

                           final String retName = dataSnapshot.child("name").getValue().toString();
                           final String retStatus = dataSnapshot.child("status").getValue().toString();

                           holder.userName.setText(retName);


                           if (dataSnapshot.child("userState").hasChild("state"))
                           {
                               String date = dataSnapshot.child("userState").child("date").getValue().toString();
                               String state = dataSnapshot.child("userState").child("state").getValue().toString();
                               String time = dataSnapshot.child("userState").child("time").getValue().toString();

                               if (state.equals("online"))
                               {
                                   holder.userStatus.setText("online");
                               }
                               else if (state.equals("offline"))
                               {
                                   holder.userStatus.setText("Last Seen: " + time);
                               }
                           }
                           else {
                               holder.userStatus.setText("offline");
                           }



                           holder.itemView.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                   chatIntent.putExtra("user_id",userIds);
                                   chatIntent.putExtra("user_name",retName);
                                   chatIntent.putExtra("user_image", retImage[0]);
                                   startActivity(chatIntent);
                               }
                           });
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout , parent,false);
                return new chatViewHolder(view);
            }
        };

        mChatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class chatViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView userName,userStatus;

        public chatViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.all_circle_dp);
            userName = itemView.findViewById(R.id.user_single_name);
            userStatus = itemView.findViewById(R.id.all_user_status);
        }
    }
}
