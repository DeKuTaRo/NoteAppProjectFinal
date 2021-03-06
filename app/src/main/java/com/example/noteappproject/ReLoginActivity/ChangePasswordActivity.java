package com.example.noteappproject.ReLoginActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noteappproject.databinding.ActivityChangePasswordBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseUser user;

    private ActivityChangePasswordBinding binding;

    @Override
    protected void onStart() {
        super.onStart();

        this.user = FirebaseAuth.getInstance().getCurrentUser();
        if (this.user == null) {
            // Show Error Message That User need to login first before change password
            // Use alert dialoag...

            // Create the object of
            // AlertDialog Builder class
            AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
            // Set Alert Title
            builder.setTitle("Change Password Alert !");
            // Set the message show for the Alert time
            builder.setMessage("You need to login first before you can change your password ?");
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
        this.binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        this.binding.changePassBtn.setOnClickListener(this);
    }

    @SuppressLint("ShowToast")
    @Override
    public void onClick(View view) {
        String email = this.user.getEmail();

        if ( email == null ){
            Toast.makeText(ChangePasswordActivity.this, "You need to login before you can change password", Toast.LENGTH_SHORT).show();
            return;
        }

        final String oldPassword = this.binding.editTextOldPassword.getText().toString();
        final String newPassEnter = this.binding.newPass.getText().toString();
        final String confirmPassEnter = this.binding.confirmPass.getText().toString();

        if (TextUtils.isEmpty(oldPassword)) {
            this.binding.editTextOldPassword.setError("Please enter your old password");
            this.binding.editTextOldPassword.requestFocus();
            return;
        }

        if (oldPassword.length() < 6) {
            this.binding.editTextOldPassword.setError("The old password must be at least 6 characters");
            this.binding.editTextOldPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassEnter)) {
            this.binding.newPass.setError("Please enter your new password");
            this.binding.newPass.requestFocus();
            return;
        }

        if (newPassEnter.length() < 6) {
            this.binding.newPass.setError("The password must be at least 6 characters");
            this.binding.newPass.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassEnter)) {
            this.binding.confirmPass.setError("Please confirm your new password");
            this.binding.confirmPass.requestFocus();
            return;
        }

        if (!newPassEnter.equals(confirmPassEnter)) {
            this.binding.confirmPass.setError("The password must be the same");
            this.binding.confirmPass.requestFocus();
            return;
        }

        this.binding.progressBar.setVisibility(View.VISIBLE);

        // Re Authen ????? check pass c?? c?? ????ng k m???i cho ?????i
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, oldPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if ( task.isSuccessful() ){
                        // Authen th??nh c??ng => ?????i pass
                        user.updatePassword(newPassEnter)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // ?????i
                                        Toast.makeText(ChangePasswordActivity.this, "Change password successfully", Toast.LENGTH_SHORT).show();
                                        FirebaseAuth.getInstance().signOut(); // ????ng xu???t
                                        startActivity(new Intent(ChangePasswordActivity.this, MainActivity.class));
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, "Failed to change password, some errors has occurred", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Failed to change password, please check your old password", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(ChangePasswordActivity.this, "Failed to change password, please check your old password", Toast.LENGTH_SHORT).show());
    }
}

