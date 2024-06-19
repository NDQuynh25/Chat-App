package com.example.chatappfirebase.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatappfirebase.R;

public class IntroActivity_1 extends AppCompatActivity {

    Animation topAnim, rotateAnim;
    ImageView icon_loading, icon_logo;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);


        setContentView(R.layout.activity_intro1);

        //Animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);

        icon_loading = findViewById(R.id.icon_loading);
        icon_logo = findViewById(R.id.icon_logo);

        icon_loading.setAnimation(rotateAnim);
        icon_logo.setAnimation(topAnim);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity( new Intent(IntroActivity_1.this, IntroActivity_2.class));
                finish();
            }
        }, 2500);

    }
}