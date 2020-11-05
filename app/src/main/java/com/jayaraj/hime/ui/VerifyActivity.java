package com.jayaraj.hime.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.jayaraj.hime.R;

import java.util.concurrent.TimeUnit;

public class VerifyActivity extends AppCompatActivity {

  TextView resendOtp;
  EditText enterOtp;
  Button verifyOtp;
  String mVerificationId;
  private FirebaseAuth mAuth;
  private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
  String number;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_verify);
    resendOtp = findViewById(R.id.resendOtp);
    enterOtp = findViewById(R.id.verifyCode);
    verifyOtp = findViewById(R.id.verifyButton);

    mAuth = FirebaseAuth.getInstance();
    Intent getDetail = getIntent();
    number = getDetail.getStringExtra("Number");
    sendVerificationCode(number);

    verifyOtp.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(mVerificationId, enterOtp.getText().toString());
            signInWithPhoneAuthCredential(credential);
          }
        });
    resendOtp.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(
                    "+91 " + number, 0, TimeUnit.SECONDS, VerifyActivity.this, mCallbacks);
          }
        });
  }

  private void sendVerificationCode(String mobile) {
    setupVerificationCallbacks();
    PhoneAuthProvider.getInstance()
        .verifyPhoneNumber("+91" + mobile, 0, TimeUnit.SECONDS, VerifyActivity.this, mCallbacks);
  }

  private void setupVerificationCallbacks() {
    mCallbacks =
        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
          @Override
          public void onVerificationCompleted(PhoneAuthCredential credential) {
            Toast.makeText(getApplicationContext(), "Verification Complete", Toast.LENGTH_SHORT)
                .show();
            signInWithPhoneAuthCredential(credential);
          }

          @Override
          public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
          }

          @Override
          public void onCodeSent(
              String verificationId, PhoneAuthProvider.ForceResendingToken token) {
            Toast.makeText(
                    getApplicationContext(), "Otp has sent to +91" + number, Toast.LENGTH_SHORT)
                .show();
            mVerificationId = verificationId;
          }
        };
  }

  private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
    mAuth
        .signInWithCredential(credential)
        .addOnCompleteListener(
            this,
            new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  // Sign in success, update UI with the signed-in user's information
                  Toast.makeText(VerifyActivity.this, "Registered successfully", Toast.LENGTH_SHORT)
                      .show();
                  Intent intent = new Intent(VerifyActivity.this, AddProfileActivity.class);
                  startActivity(intent);
                  finish();
                } else {
                  // Sign in failed, display a message and update the UI
                  Toast.makeText(VerifyActivity.this, "Registration Failed", Toast.LENGTH_SHORT)
                      .show();
                  if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                  }
                }
              }
            });
  }
}
