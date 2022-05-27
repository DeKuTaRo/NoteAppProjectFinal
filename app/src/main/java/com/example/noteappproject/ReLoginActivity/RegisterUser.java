package com.example.noteappproject.ReLoginActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noteappproject.Models.User;
import com.example.noteappproject.R;
import com.example.noteappproject.databinding.ActivityRegisterUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancytoastlib.FancyToast;

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
                        User user = new User(fullNameValue, emailValue);

                        // Tạo bảng Users/Email/Account
                        FirebaseDatabase.getInstance().getReference("Users").child(getSubEmailName(emailValue)).child("Account")
                                .setValue(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        FancyToast.makeText(RegisterUser.this,
                                                "User account has been register successfully !",
                                                FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false);
                                        progressBar.setVisibility(View.VISIBLE);
                                        finish();
                                    } else {
                                        FancyToast.makeText(RegisterUser.this,
                                                "Failed to register ! Try again !",
                                                FancyToast.LENGTH_LONG, FancyToast.ERROR, false);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    } else {
                        FancyToast.makeText(RegisterUser.this,
                                "Failed to register ! Try again !",
                                FancyToast.LENGTH_LONG, FancyToast.ERROR, false);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public final static String getSubEmailName(String email){
        return email.substring(0, email.indexOf("@"));
    }
}