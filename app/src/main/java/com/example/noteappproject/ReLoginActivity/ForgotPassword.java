package com.example.noteappproject.ReLoginActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noteappproject.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        this.mAuth = FirebaseAuth.getInstance();
        this.binding.resetPasswordBtn.setOnClickListener(view -> resetPassword());
    }

    private void resetPassword() {
        String email = this.binding.emailReset.getText().toString().trim();

        if (email.isEmpty()) {
            this.binding.emailReset.setError("Email is required");
            this.binding.emailReset.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.binding.emailReset.setError("Please provide valid email");
            this.binding.emailReset.requestFocus();
            return;
        }

        this.binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPassword.this, "Check success", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPassword.this, "Check failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}