package com.keshav.chattizpro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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


///**
// * A simple {@link Fragment} subclass.
// * Use the {@link FriendsFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class FriendsFragment extends Fragment {

    private View FriendsView;
    private RecyclerView myFriendsList;

    private String userImage = "default";

    //Database Ref
    private DatabaseReference FriendsRef,usersRef;

    private FirebaseAuth mAuth;
    private String current_user_id;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
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
        FriendsView = inflater.inflate(R.layout.fragment_friends, container, false);
        myFriendsList = FriendsView.findViewById(R.id.friends_list);
        myFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user_id);
        FriendsRef.keepSynced(true);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);


        return FriendsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(FriendsRef,Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends,friendsViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, friendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final friendsViewHolder holder, int position, @NonNull Friends model)
            {
                final String userIDs = getRef(position).getKey();

                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.child("userState").hasChild("state"))
                            {
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online"))
                                {
                                    holder.mOnlineCircle.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline"))
                                {
                                    holder.mOnlineCircle.setVisibility(View.INVISIBLE);
                                }
                            }
                            else {
                                holder.mOnlineCircle.setVisibility(View.INVISIBLE);
                            }

                            if (dataSnapshot.hasChild("image"))
                            {
                                userImage = dataSnapshot.child("image").getValue().toString();
                                final String profile_name = dataSnapshot.child("name").getValue().toString();
                                String profile_status = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profile_name);
                                holder.userStatus.setText(profile_status);

//                            if (dataSnapshot.hasChild("online"))
//                            {
//                                String user_online = (String) dataSnapshot.child("online").getValue();
//                                holder.setUserOnline(user_online);
//                            }

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        CharSequence options [] = new CharSequence[]{"Open Profile"};

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Select Options");
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                //Click events for each option
                                                if (i == 0 )
                                                {
                                                    Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                                    profileIntent.putExtra("visit_user_id",userIDs);
                                                    profileIntent.putExtra("user_name",profile_name);
                                                    profileIntent.putExtra("image",userImage);
                                                    startActivity(profileIntent);
                                                }
                                                if (i == 1)
                                                {
                                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                                    chatIntent.putExtra("visit_user_id",userIDs);
                                                    startActivity(chatIntent);
                                                }
                                            }
                                        });

                                        builder.show();

                                    }
                                });



                                Picasso.get().load(userImage).placeholder(R.drawable.dpvector).into(holder.profileImage);

                            }else
                            {
                                String profile_name = dataSnapshot.child("name").getValue().toString();
                                String profile_status = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profile_name);
                                holder.userStatus.setText(profile_status);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public friendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout,parent,false);
                friendsViewHolder viewHolder = new friendsViewHolder(view);
                return viewHolder;
            }
        };

        myFriendsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class friendsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;
        ImageView mOnlineCircle;

        public friendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_single_name);
            userStatus = itemView.findViewById(R.id.all_user_status);
            profileImage = itemView.findViewById(R.id.all_circle_dp);
            mOnlineCircle = itemView.findViewById(R.id.online_circle);

        }



//        public void setUserOnline(String online_status)
//        {
//            ImageView online_image = mView.findViewById(R.id.online_circle);
//
//            if (online_status.equals("true"))
//            {
//                online_image.setVisibility(View.VISIBLE);
//            }else
//            {
//                online_image.setVisibility(View.INVISIBLE);
//            }
//        }
    }
}
