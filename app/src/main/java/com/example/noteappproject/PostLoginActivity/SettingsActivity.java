package com.example.noteappproject.PostLoginActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noteappproject.AdvancedFunction.TrashBinActivity;
import com.example.noteappproject.R;
import com.example.noteappproject.ReLoginActivity.ChangePasswordActivity;
import com.example.noteappproject.ReLoginActivity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseUser user;
    private DatabaseReference reference;

    private String userID;

    private TextView profileTextView, changeFontTextView, timeSettingTextView, volumeTextView;
    private Button changePassBtn, logoutBtn;

    private String oldPasswordUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileTextView = findViewById(R.id.profileTextView);
        profileTextView.setOnClickListener(this);

        changeFontTextView = findViewById(R.id.changeFontTextView);
        changeFontTextView.setOnClickListener(this);

        timeSettingTextView = findViewById(R.id.timeSettingTextView);
        timeSettingTextView.setOnClickListener(this);

        volumeTextView = findViewById(R.id.volumeTextView);
        volumeTextView.setOnClickListener(this);

        changePassBtn = findViewById(R.id.changePassBtn);
        changePassBtn.setOnClickListener(this);

        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profileTextView :
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.trashBinList :
                // chuyển vào screen thùng rác chứa những note bị xóa nhưng chưa xóa hẳn
                startActivity(new Intent(this, TrashBinActivity.class));
                break;
            case R.id.changeFontTextView :
                Toast.makeText(this, "change Font", Toast.LENGTH_SHORT).show();
                break;
            case R.id.timeSettingTextView :
                Toast.makeText(this, "time Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.volumeTextView :
                Toast.makeText(this, "volume Setting", Toast.LENGTH_SHORT).show();
                break;
            case R.id.changePassBtn :
                startActivity(new Intent(this, ChangePasswordActivity.class));
                break;
            case R.id.logoutBtn :
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                break;

        }
    }

}