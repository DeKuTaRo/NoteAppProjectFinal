package com.example.noteappproject.ReLoginActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noteappproject.PostLoginActivity.NoteActivity;
import com.example.noteappproject.R;
import com.example.noteappproject.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    private boolean passwordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        Initialize();
        SetToggleButtonInPasswordInput();
    }

    private void Initialize() {
        this.mAuth = FirebaseAuth.getInstance();

        this.binding.loginBtn.setOnClickListener(this);
        this.binding.forgotPassword.setOnClickListener(this);
        this.binding.register.setOnClickListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void SetToggleButtonInPasswordInput() {
        this.binding.passwordEditText.setOnTouchListener((View view, @SuppressLint("ClickableViewAccessibility") MotionEvent motionEvent) -> {
            final int Right = 2;
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (motionEvent.getRawX() >= this.binding.passwordEditText.getRight() - this.binding.passwordEditText.getCompoundDrawables()[Right].getBounds().width()) {
                    int selection = this.binding.passwordEditText.getSelectionEnd();
                    if (passwordVisible) {
                        this.binding.passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
                        this.binding.passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        passwordVisible = false;
                    } else {
                        this.binding.passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0);
                        this.binding.passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordVisible = true;
                    }
                    this.binding.passwordEditText.setSelection(selection);
                    return true;
                }
            }
            return false;
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register:
                Intent intent_Register = new Intent(this, RegisterUser.class);
                startActivity(intent_Register);
                break;
            case R.id.loginBtn:
                userLogin();
                break;
            case R.id.forgotPassword :
                Intent intent_ForgotPassword = new Intent(this, ForgotPassword.class);
                startActivity(intent_ForgotPassword);
                break;
            default:
                // Toast Error Message
                break;
        }

    }

    private void userLogin() {

        String email = this.binding.emailEditText.getText().toString().trim();
        String password = this.binding.passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            this.binding.emailEditText.setError("Email is required");
            this.binding.emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.binding.emailEditText.setError("Please enter a valid email");
            this.binding.emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            this.binding.passwordEditText.setError("Password is required");
            this.binding.passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            this.binding.passwordEditText.setError("Password must be at least 6 characters");
            this.binding.passwordEditText.requestFocus();
            return;
        }

        this.binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        assert user != null;
                        if (user.isEmailVerified()) {
                            Toast.makeText(MainActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, NoteActivity.class));
                        } else {
                            user.sendEmailVerification();
                            Toast.makeText(MainActivity.this, "Check your email to verify your account", Toast.LENGTH_SHORT).show();
                            this.binding.progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to login ! Please check your credentials", Toast.LENGTH_SHORT).show();
                        this.binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}