package com.keshav.chattizpro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
 * Use the {@link RequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestsFragment extends Fragment {

    private View RequestView;
    private RecyclerView myRequestList;

    private FirebaseAuth mAuth;
    private String currentUser;

    private DatabaseReference mRequestRef, UsersRef;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestsFragment newInstance(String param1, String param2) {
        RequestsFragment fragment = new RequestsFragment();
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
        RequestView = inflater.inflate(R.layout.fragment_requests, container, false);

        myRequestList = RequestView.findViewById(R.id.requests_list);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        mRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        return RequestView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mRequestRef.child(currentUser), Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Users, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Users model) {

                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                // list_user_id serially gets all the user ids (ids of those users who sent us request or whom we sent a request)
                // from the friend_req node in the database
                final String list_user_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String type = dataSnapshot.getValue().toString();
                            if (type.equals("recieved")) {
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image")) {
                                            final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                            final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                            final String requestUserImage = dataSnapshot.child("image").getValue().toString();

                                            holder.senderName.setText(requestUserName);
                                            holder.senderStatus.setText(requestUserStatus);
                                            Picasso.get().load(requestUserImage).placeholder(R.drawable.avatar).into(holder.senderProfileImage);
                                        } else {
                                            final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                            final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                            holder.senderName.setText(requestUserName);
                                            holder.senderStatus.setText(requestUserStatus);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
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
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                RequestViewHolder viewHolder = new RequestViewHolder(view);
                return viewHolder;
            }
        };


        myRequestList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView senderProfileImage;
        private TextView senderName,senderStatus;
        private Button requestAcceptBtn, requestCancelBtn;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            senderProfileImage = itemView.findViewById(R.id.all_circle_dp);
            senderName = itemView.findViewById(R.id.user_single_name);
            senderStatus = itemView.findViewById(R.id.all_user_status);
            requestAcceptBtn = itemView.findViewById(R.id.request_accept_btn);
            requestCancelBtn = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
