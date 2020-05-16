package com.keshav.chattizpro;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.animation.Animation;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DatabaseReference mUserRef;

    private TabLayout mTabLayout;
    private String currentUserId;

    private DatabaseReference mRootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        mRootRef = FirebaseDatabase.getInstance().getReference();



        mToolbar=findViewById(R.id.main_tollbar);
        mToolbar.setTitle("ChattizPro");
        mToolbar.setTitleTextColor(0xFFFFFFFF);
        mToolbar.inflateMenu(R.menu.game_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId())
                {

                    case R.id.acc_settings:
                        Intent settingsIntent=new Intent(MainActivity.this,AccountSettings.class);
                        startActivity(settingsIntent);
                        return true;


                    case R.id.about_menu:
                        Intent aboutIntent=new Intent(MainActivity.this,AboutActivity.class);
                         startActivity(aboutIntent);
                         return true;

                    case R.id.all_users_menu:
                        Intent usersIntent = new Intent(MainActivity.this,UsersActivity.class);
                        startActivity(usersIntent);
                        return true;

                }

                return true;
            }
        });

        mViewPager= findViewById(R.id.viewpager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mTabLayout = findViewById(R.id.main_tab_layout);

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            updateUi();
        }
        else {

            updateUserStatus("online");
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {

            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {

            updateUserStatus("offline");
        }
    }

    private void updateUi() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    private void updateUserStatus(String state)
    {
        String currentUser = mAuth.getCurrentUser().getUid();
        String saveCurrentTime , saveCurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String , Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        mRootRef.child("Users").child(currentUser).child("userState")
                .updateChildren(onlineStateMap);
    }

}




