package com.example.noteappproject.ReLoginActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.example.noteappproject.databinding.ActivityForgotPasswordBinding;
import com.example.noteappproject.databinding.ActivityVerifyOtpBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyOTP extends AppCompatActivity {

    private ActivityVerifyOtpBinding binding;
    private String registerUser_PhoneNumber;
    private String registerUser_Email;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityVerifyOtpBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);

        this.mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();

        if ( intent != null ){
//            this.registerUser_PhoneNumber = intent.getStringExtra(RegisterUser.INTENT_EXTRAS_KEY_PHONE_NUMBER);
//            this.registerUser_Email = intent.getStringExtra(RegisterUser.INTENT_EXTRAS_KEY_EMAIL);

            Log.e("Test", this.registerUser_Email + this.registerUser_PhoneNumber);
        }
    }

//    private void sendOTPCodeToPhoneNumber(String phoneNumber) {
//        PhoneAuthOptions options =
//                PhoneAuthOptions.newBuilder(mAuth)
//                        .setPhoneNumber(phoneNumber)       // Phone number to verify
//                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                        .setActivity(this)                 // Activity (for callback binding)
//                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
//                        .build();
//        PhoneAuthProvider.verifyPhoneNumber(options);
//    }

//    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//        @Override
//        public void onVerificationCompleted(PhoneAuthCredential credential) {
//            // This callback will be invoked in two situations:
//            // 1 - Instant verification. In some cases the phone number can be instantly
//            //     verified without needing to send or enter a verification code.
//            // 2 - Auto-retrieval. On some devices Google Play services can automatically
//            //     detect the incoming verification SMS and perform verification without
//            //     user action.
//            Log.d("TEST", "onVerificationCompleted:" + credential);
//
//            signInWithPhoneAuthCredential(credential);
//        }
//
//        @Override
//        public void onVerificationFailed(FirebaseException e) {
//            // This callback is invoked in an invalid request for verification is made,
//            // for instance if the the phone number format is not valid.
//            Log.e("TEST", "onVerificationFailed", e);
//
//            if (e instanceof FirebaseAuthInvalidCredentialsException) {
//                // Invalid request
//            } else if (e instanceof FirebaseTooManyRequestsException) {
//                // The SMS quota for the project has been exceeded
//            }
//
//            // Show a message and update the UI
//        }
//
//        @Override
//        public void onCodeSent(@NonNull String verificationId,
//                @NonNull PhoneAuthProvider.ForceResendingToken token) {
//            // The SMS verification code has been sent to the provided phone number, we
//            // now need to ask the user to enter the code and then construct a credential
//            // by combining the code with a verification ID.
//            Log.d("TEST", "onCodeSent:" + verificationId);
//
//            // Save verification ID and resending token so we can use them later
//            mVerificationId = verificationId;
//            mResendToken = token;
//        }
//    };
}