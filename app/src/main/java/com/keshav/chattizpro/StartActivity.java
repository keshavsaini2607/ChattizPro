package com.keshav.chattizpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    private Button mLoginBtn;
    private Button mRegBtn;
    private Animation top,bottom;
    private ImageView mImageView,mSocialIcons,mIconMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Animations
        top= AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottom=AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        mLoginBtn = findViewById(R.id.Login_btn_start);
        mRegBtn = findViewById(R.id.signup_btn_start);
        mImageView = findViewById(R.id.starthead);
        mSocialIcons = findViewById(R.id.socialicons);
        mIconMain = findViewById(R.id.logomain);

        mIconMain.setAnimation(bottom);
        mImageView.setAnimation(bottom);
        mLoginBtn.setAnimation(bottom);
        mRegBtn.setAnimation(bottom);
        mSocialIcons.setAnimation(bottom);




        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent LoginIntent = new Intent(StartActivity.this,LoginActivity.class);
                startActivity(LoginIntent);
            }
        });

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RegIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(RegIntent);
            }
        });
    }


}
