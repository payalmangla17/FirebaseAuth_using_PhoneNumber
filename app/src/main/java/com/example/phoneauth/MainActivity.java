package com.example.phoneauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button signOut=findViewById(R.id.signout);
       mAuth=FirebaseAuth.getInstance();
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "Signing out", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();

                finish();
                Intent intent = new Intent(MainActivity.this, PhoneAuth.class);

                startActivity(intent);
            }
        });
        testPhoneAutoRetrieve();
    }
    public void testPhoneVerify() {
        // [START auth_test_phone_verify]
        String phoneNum = "1234567890";
        String testVerificationCode = "123456";

        // Whenever verification is triggered with the whitelisted number,
        // provided it is not set for auto-retrieval, onCodeSent will be triggered.
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNum, 30L /*timeout*/, TimeUnit.SECONDS,
                this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        // Save the verification id somewhere
                        // ...

                        // The corresponding whitelisted code above should be used to complete sign-in.
                        MainActivity.this.enableUserManuallyInputCode();
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        // Sign in with the credential
                        // ...
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        // ...
                    }

                });
        // [END auth_test_phone_verify]
    }

    public void testPhoneAutoRetrieve() {
        // [START auth_test_phone_auto]
        // The test phone number and code should be whitelisted in the console.
        String phoneNumber = "+16505554567";
        String smsCode = "123456";

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();

        // Configure faking the auto-retrieval with the whitelisted numbers.
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode);

        PhoneAuthProvider phoneAuthProvider = PhoneAuthProvider.getInstance();
        phoneAuthProvider.verifyPhoneNumber(
                phoneNumber,
                60L,
                TimeUnit.SECONDS,
                this, /* activity */
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Instant verification is applied and a credential is directly returned.
                        // ...
                    }

                    // [START_EXCLUDE]
                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                    }
                    // [END_EXCLUDE]
                });
        // [END auth_test_phone_auto]
    }
    private void enableUserManuallyInputCode() {
        // No-op
    }
}
