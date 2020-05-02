package com.example.phoneauth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class SignUp extends AppCompatActivity  implements View.OnClickListener {
        private static final String TAG = "SIGNUPActivity";
    Button signup,resendOtp;
    TextView loginpage,resend_otp;
    EditText phoneno,otp_text;
    FirebaseDatabase mDatabase;
    DatabaseReference ref;
    String s;
    private FirebaseAuth mAuth;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        mDatabase=FirebaseDatabase.getInstance();
        phoneno=findViewById(R.id.Phone1);
        loginpage=findViewById(R.id.signin);
        loginpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(SignUp.this,PhoneAuth.class));
            }
        });
        resendOtp=findViewById(R.id.Resend1);
        signup=findViewById(R.id.signUp);
        signup.setOnClickListener(this);
        resendOtp.setOnClickListener(this);
        mAuth=FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                // Log.e("uid new :",""+firebaseAuth.getUid());

            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    phoneno.setError("Invalid Credentials.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {

                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                otpDialog();


            }
        };
    }
    private void saveDataInFirebase(String uid) {
        ref=mDatabase.getReference("Users");
        s = phoneno.getText().toString().trim();

       user_data_model user = new user_data_model(s,uid);


        Log.e("uid : ", "" + uid);
       // String mob = phoneno.getText().toString();

        ref.child(uid).setValue(user);
        //ref.child(uid).setValue(UID);

    }
    public  void readData(String phoneNum){
        // final String[] phoneNum = new String[1];
        final int[] f =new int[1];
        //String uid=phoneNum.getUid();
        ref=mDatabase.getReference("Users");
        ref.orderByChild("mobile_no").equalTo(phoneNum).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null){
                    //f[0] =1;
                    startPhoneNumberVerification(phoneno.getText().toString());

                }
                else     Toast.makeText(SignUp.this, "User already exist.\nPlease register with different phone number.", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //  if(f[0]==0) return true;
        //return false;
    }


    private void startPhoneNumberVerification(String phoneNumber) {
        s="+91"+phoneNumber;
        if(s.length()!=13){
            Toast.makeText(SignUp.this,"Invalid Phone Number!\n Enter valid 10 digit Phone number." ,Toast.LENGTH_LONG).show();
            return;
        }


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                s,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
        //otpDialog();
        mVerificationInProgress = true;


    }


    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);

        // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                           saveDataInFirebase(user.getUid());

                            startActivity(new Intent(SignUp.this,MainActivity.class));
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                Toast.makeText(SignUp.this,"Invalid credentials",Toast.LENGTH_SHORT).show();

                            }

                        }
                    }
                });
    }
    // [END sign_in_with_phone]

    private void signOut() {
        mAuth.signOut();
        // updateUI(STATE_INITIALIZED);
    }




    private boolean validatePhoneNumber() {
        String phoneNumber = phoneno.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)||phoneNumber.length()!=10) {
            // phone.setError("Invalid phone number.");
            Toast.makeText(SignUp.this,"Invalid Number",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    private void otpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);

        View view = getLayoutInflater().inflate(R.layout.fragment_otp, null);
        resend_otp = view.findViewById(R.id.tv_otp_resend);
        resend_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode(s, mResendToken);
            }
        });

        otp_text = view.findViewById(R.id.et_otp_dig_1);
        builder.setCancelable(false);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String code=otp_text.getText().toString();
                if (code.equals("")||code.length()!=6) {
                    Toast.makeText(SignUp.this, "Cannot leave empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.signUp:
                if (!validatePhoneNumber()) {
                    return;
                }
              //  startPhoneNumberVerification(phoneno.getText().toString());
                readData(phoneno.getText().toString());


                break;
            case R.id.Resend1:
            /*    if(s.isEmpty()){
                    Toast.makeText(this,"Enter valid number",Toast.LENGTH_SHORT).show();
                    return;
                }*/
                resendVerificationCode(s, mResendToken);
                break;

        }
    }
}
