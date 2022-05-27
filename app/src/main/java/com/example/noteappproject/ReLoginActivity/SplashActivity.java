package com.example.noteappproject.ReLoginActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noteappproject.PostLoginActivity.NoteActivity;
import com.example.noteappproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(this::nextActivityScreen, 2000);
    }

    private void nextActivityScreen() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Chưa đăng nhập
        if (user == null) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SplashActivity.this, NoteActivity.class);
            startActivity(intent);
        }
    }
}