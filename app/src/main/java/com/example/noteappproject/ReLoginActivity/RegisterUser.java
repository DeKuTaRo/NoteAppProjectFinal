package com.example.noteappproject.ReLoginActivity;

import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noteappproject.Models.User;
import com.example.noteappproject.R;
import com.example.noteappproject.databinding.ActivityRegisterUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener{

    private ActivityRegisterUserBinding binding;

    private EditText fullName_input, age_input, email_input, password_input, password_input_rewrite;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityRegisterUserBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        this.mAuth = FirebaseAuth.getInstance();

        this.binding.banner.setOnClickListener(this);
        this.binding.registerUser.setOnClickListener(this);

        this.fullName_input = this.binding.fullNameInput;
        this.email_input = this.binding.emailInput;
        this.password_input = this.binding.passwordInput;
        this.password_input_rewrite= this.binding.passwordInputRewrite;

        this.progressBar = this.binding.progressBar;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.banner:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.registerUser:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String fullNameValue = fullName_input.getText().toString().trim();
        String emailValue = email_input.getText().toString().trim();
        String passwordValue = password_input.getText().toString().trim();
        String passwordRewrite = password_input_rewrite.getText().toString().trim();

        if (fullNameValue.isEmpty()) {
            fullName_input.setError("Full name is required");
            fullName_input.requestFocus();
            return;
        }


        if (emailValue.isEmpty()) {
            email_input.setError("Email is required");
            email_input.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            password_input.setError("Please provide valid email");
            password_input.requestFocus();
            return;
        }

        if (passwordValue.isEmpty()) {
            password_input.setError("Password is required");
            password_input.requestFocus();
            return;
        }

        if (passwordValue.length() < 6) {
            password_input.setError("Password must at least 6 characters");
            password_input.requestFocus();
            return;
        }

        if (!passwordRewrite.equals(passwordValue)) {
            password_input_rewrite.setError("The password must be the same");
            password_input_rewrite.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(emailValue, passwordValue)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        User user = new User(fullNameValue, emailValue, passwordValue);

                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(requireNonNull(requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()))
                                .setValue(user)
                                .addOnCompleteListener(task1 -> {

                                    if (task1.isSuccessful()) {
                                        Toast.makeText(RegisterUser.this, "User has been register successfully", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.VISIBLE);
                                        startActivity(new Intent(RegisterUser.this, MainActivity.class));
                                    } else {
                                        Toast.makeText(RegisterUser.this, "Failed to register ! Try again !", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterUser.this, "Failed to register", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}