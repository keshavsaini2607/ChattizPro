package com.keshav.chattizpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.userToolbar);
        mToolbar.setTitle("All Users");
        mToolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList = findViewById(R.id.user_view);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
    }

    //Since we need to retrieve the data in real time so we need to put the retrieving code into the onStart method

    @Override
    protected void onStart() {
        super.onStart();

        /*
        Previously only the FirebaseRecyclerAdapter method is used but in the current version of android sdk
        it is depericated and instead of this we have to create FirebaseRecyclerOptions method first and then
        create a reference of FirebaseRecyclerAdapter to work with that method to show our required data form firebase
        server into our recycler view



         */

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mDatabaseReference, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users , usersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, usersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull usersViewHolder holder, final int position, @NonNull Users users) {

                holder.setName(users.getName());
                holder.setStatus(users.getStatus());
                holder.setImage(users.getThumb_image());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profile_intent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profile_intent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profile_intent);
                    }
                });

            }

            @NonNull
            @Override
            public usersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);
                usersViewHolder viewHolder = new usersViewHolder(view);
                return viewHolder;


            }
        };
        /*
        IN the new version of firebase ui as of now what i am using we have to include a method adapter.startListening();
        to make our firebaseRecyclerAdapter method start to get information from the database
         */
        firebaseRecyclerAdapter.startListening();
        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class usersViewHolder extends  RecyclerView.ViewHolder{

        //mView is going to be used for setting onclick listener for recycler view item
        public View mView;

        public usersViewHolder(@NonNull View itemView) {
            super(itemView);


            mView = itemView;
        }

        public void setName(String name) {
            TextView userName = mView.findViewById(R.id.user_single_name);
            userName.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatus = mView.findViewById(R.id.all_user_status);
            userStatus.setText(status);
        }

        public void setImage(String thumb_image) {
            CircleImageView userImageView = mView.findViewById(R.id.all_circle_dp);

           Picasso.get().load(thumb_image).placeholder(R.drawable.dpvector).into(userImageView);
        }
    }


}
