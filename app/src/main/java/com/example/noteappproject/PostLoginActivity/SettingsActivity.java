package com.example.noteappproject.PostLoginActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noteappproject.Models.Settings;
import com.example.noteappproject.R;
import com.example.noteappproject.ReLoginActivity.ChangePasswordActivity;
import com.example.noteappproject.ReLoginActivity.MainActivity;
import com.example.noteappproject.utilities.StringUlti;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseUser user;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseDatabase rootNode;
    private String userID;

    private Button changePassBtn, logoutBtn;

    private String fontSizeItem, fontStyleItem, timeDeleteItem;
    private String fontSizeDB, fontStyleDB, timeDeleteDB;
    TextInputLayout layoutFontSize, layoutFontStyle, layoutAddTimeDelete;
    AutoCompleteTextView selectFontSize, selectFontStyle, selectTimeDelete;

    // Small : 10dp, Medium : 15dp, Big : 20dp, Very Big : 25dp
    String[] itemFontSize = {"Small", "Medium", "Big", "Very Big"};

    String[] itemFontStyle = {"Normal", "Bold", "Italic", "Underline"};

    String[] itemTimeDelete = {"1", "2", "3", "4", "5", "6", "7"};

    ArrayAdapter<String> arrayFontSizeAdapter, arrayFontStyleAdapter, arrayTimeDeleteAdapter;

    @Override
    protected void onResume() {
        super.onResume();

        arrayFontSizeAdapter = new ArrayAdapter<>(this, R.layout.list_item_font_size, itemFontSize);
        selectFontSize.setAdapter(arrayFontSizeAdapter);

        arrayFontStyleAdapter = new ArrayAdapter<>(this, R.layout.list_item_font_size, itemFontStyle);
        selectFontStyle.setAdapter(arrayFontStyleAdapter);

        this.reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Settings settings = snapshot.getValue(Settings.class);

                if (settings != null) {
                    fontSizeDB = settings.getFontSize();
                    fontStyleDB = settings.getFontStyle();
                    timeDeleteDB = settings.getTimeDelete();
                }
                selectFontSize.setText(fontSizeDB);
                selectFontStyle.setText(fontStyleDB);
                selectTimeDelete.setText(timeDeleteDB);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.mAuth = FirebaseAuth.getInstance();

        changePassBtn = findViewById(R.id.changePassBtn);
        changePassBtn.setOnClickListener(this);

        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);

        layoutFontSize = findViewById(R.id.layoutFontSize);
        layoutFontStyle = findViewById(R.id.layoutFontStyle);
        layoutAddTimeDelete = findViewById(R.id.layoutAddTimeDelete);

        selectFontSize = findViewById(R.id.selectFontSize);
        selectFontStyle = findViewById(R.id.selectFontStyle);
        selectTimeDelete = findViewById(R.id.selectTimeDelete);


        final String userEmail = StringUlti.getSubEmailName(Objects.requireNonNull(Objects.requireNonNull(this.mAuth.getCurrentUser()).getEmail()));
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("Users").child(userEmail).child("Settings");

        this.reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Settings settings = snapshot.getValue(Settings.class);

                if (settings != null) {
                    fontSizeDB = settings.getFontSize();
                    fontStyleDB = settings.getFontStyle();
                    timeDeleteDB = settings.getTimeDelete();
                }
                selectFontSize.setText(fontSizeDB);
                selectFontStyle.setText(fontStyleDB);
                selectTimeDelete.setText(timeDeleteDB);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        arrayFontSizeAdapter = new ArrayAdapter<>(this, R.layout.list_item_font_size, itemFontSize);
        selectFontSize.setAdapter(arrayFontSizeAdapter);

        selectFontSize.setOnItemClickListener((parent, view, position, id) -> fontSizeItem = parent.getItemAtPosition(position).toString());

        arrayFontStyleAdapter = new ArrayAdapter<>(this, R.layout.list_item_font_size, itemFontStyle);
        selectFontStyle.setAdapter(arrayFontStyleAdapter);

        selectFontStyle.setOnItemClickListener((parent, view, position, id) -> fontStyleItem = parent.getItemAtPosition(position).toString());

        arrayTimeDeleteAdapter = new ArrayAdapter<>(this, R.layout.list_item_font_size, itemTimeDelete);
        selectTimeDelete.setAdapter(arrayTimeDeleteAdapter);

        selectTimeDelete.setOnItemClickListener((parent, view, position, id) -> timeDeleteItem = parent.getItemAtPosition(position).toString());

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.changePassBtn :
                startActivity(new Intent(this, ChangePasswordActivity.class));
                break;
            case R.id.logoutBtn :
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveProfileBtn :
                saveSettingsData();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSettingsData() {
        fontSizeItem = selectFontSize.getText().toString();
        fontStyleItem = selectFontStyle.getText().toString();
        timeDeleteItem = selectTimeDelete.getText().toString();

        Settings settings = new Settings(fontSizeItem, fontStyleItem, timeDeleteItem);
        reference.setValue(settings);
        Toast.makeText(this, "Settings were applied", Toast.LENGTH_SHORT).show();
    }

}