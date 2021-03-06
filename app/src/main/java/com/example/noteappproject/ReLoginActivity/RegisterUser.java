package com.example.noteappproject.ReLoginActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noteappproject.Models.User;
import com.example.noteappproject.PostLoginActivity.NoteActivity;
import com.example.noteappproject.R;
import com.example.noteappproject.databinding.ActivityRegisterUserBinding;
import com.example.noteappproject.utilities.StringUlti;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener{
    private ActivityRegisterUserBinding binding;

    private EditText fullName_input, email_input, password_input, password_input_rewrite;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();

            // Create the object of
            // AlertDialog Builder class
            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterUser.this);
            // Set Alert Title
            builder.setTitle("Register Alert !");
            // Set the message show for the Alert time
            builder.setMessage("You need to logout first ?");
            // Set Cancelable false
            // for when the user clicks on the outside
            // the Dialog Box then it will remain show
            builder.setCancelable(false);

            // Set the positive button with yes name
            // OnClickListener method is use of
            // DialogInterface interface.
            builder.setPositiveButton( "Yes", (dialog, which) -> {
                // When the user click yes button
                // then app will close
                finish();
            });

            // Create the Alert dialog
            AlertDialog alertDialog = builder.create();

            // Show the Alert Dialog box
            alertDialog.show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityRegisterUserBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        this.mAuth = FirebaseAuth.getInstance();

        BindingView();
        SetOnClickEvent();
    }

    private void BindingView() {
        this.fullName_input = this.binding.fullNameInput;
        this.email_input = this.binding.emailInput;
        this.password_input = this.binding.passwordInput;
        this.password_input_rewrite= this.binding.passwordInputRewrite;
        this.progressBar = this.binding.progressBar;
    }

    private void SetOnClickEvent() {
        this.binding.banner.setOnClickListener(this);
        this.binding.registerUser.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.banner:
                finish();
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
            email_input.setError("Please provide valid email");
            email_input.requestFocus();
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
                        User user = new User(fullNameValue, emailValue);

                        // T???o b???ng Users/Email/Account
                        FirebaseDatabase.getInstance().getReference("Users").child(StringUlti.getSubEmailName(emailValue)).child("Account")
                                .setValue(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        ShowToast("User account has been register successfully !", RegisterUser.this);
                                        progressBar.setVisibility(View.VISIBLE);

                                        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailValue, passwordValue)
                                                .addOnCompleteListener(task2 -> {
                                                    if (task2.isSuccessful()) {
                                                        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();

                                                        if ( user1 != null ){
                                                            if (user1.isEmailVerified()) {
                                                                finish();
                                                            } else {
                                                                user1.sendEmailVerification()
                                                                        .addOnCompleteListener(task3 -> {
                                                                            if (task3.isSuccessful()) {
                                                                                Toast.makeText(RegisterUser.this, "Please check your mail to active account !", Toast.LENGTH_LONG).show();
                                                                                finish();
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    }
                                                });
                                    } else {
                                        ShowToast("Failed to register ! Try again !", RegisterUser.this);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    } else {
                        ShowToast("Failed to register ! Email already exists ! Try again !", RegisterUser.this);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public void ShowToast(String message, Context context){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}