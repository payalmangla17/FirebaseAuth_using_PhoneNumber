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

public class PhoneAuth extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "PhoneAuthActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    FirebaseDatabase mFdatabase;
    DatabaseReference mdatabaseRef;
    private FirebaseAuth mAuth;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    Button signin,resendotp;
    EditText phone,otp_text;
    TextView createAcc,resend_otp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mBinding = ActivityPhoneAuthBinding.inflate(getLayoutInflater());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            finish();
            Intent i = new Intent(PhoneAuth.this, MainActivity.class);
            i.putExtra("choice", 3);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(i);

        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");

        }
        setContentView(R.layout.activity_phone_auth);

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        // Assign click listeners
         signin=findViewById(R.id.Submit);
        resendotp=findViewById(R.id.Resend);
         phone=findViewById(R.id.Phone);
        signin.setOnClickListener(this);
        resendotp.setOnClickListener(this);
        mFdatabase=FirebaseDatabase.getInstance();
        createAcc=findViewById(R.id.createacc);
        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(PhoneAuth.this,SignUp.class));
            }
        });
        mAuth = FirebaseAuth.getInstance();

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
                     phone.setError("Invalid Credentials.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {

                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded. Please try again after an hour.",
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
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(phone.getText().toString());
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }
    public  void readData(String phoneNum){

        mdatabaseRef=mFdatabase.getReference("Users");
        mdatabaseRef.orderByChild("mobile_no").equalTo(phoneNum).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    //f[0] =1;
                    startPhoneNumberVerification(phone.getText().toString());

                }
                else     Toast.makeText(PhoneAuth.this, "User does not exist.\nPlease register first.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
      //  if(f[0]==0) return true;
        //return false;
    }
String s;
    private void startPhoneNumberVerification(String phoneNumber) {
       s="+91"+phoneNumber;
      if(s.length()!=13){
          Toast.makeText(PhoneAuth.this,"Invalid Phone Number!\n Enter valid 10 digit Phone number." ,Toast.LENGTH_LONG).show();
          return;
      }

   /*  if(readData(phoneNumber)){
         Toast.makeText(this,"User does not exist.\nPlease register first.",Toast.LENGTH_SHORT).show();
         Log.i("valid","not valid");
         return ;
     }*/
        Log.i("valid"," valid");
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
                          //  boolean t=user.getUid().equals(phone);
                          //  if(t==false){
//                                Toast.makeText(PhoneAuth.this,"Number not registered!\n Please create Account",Toast.LENGTH_SHORT).show();

                         //   }

                            startActivity(new Intent(PhoneAuth.this,MainActivity.class));
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                Toast.makeText(PhoneAuth.this,"Invalid credentials",Toast.LENGTH_SHORT).show();

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
        String phoneNumber = phone.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)||phoneNumber.length()!=10) {
           // phone.setError("Invalid phone number.");
            Toast.makeText(PhoneAuth.this,"Invalid Number",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    private void otpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PhoneAuth.this);

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
                    Toast.makeText(PhoneAuth.this, "Cannot leave empty", Toast.LENGTH_SHORT).show();
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

            case R.id.Submit:
               if (!validatePhoneNumber()) {
                    return;
                }
               //Test number
               if(phone.getText().toString()=="1234567890"){
                   Log.i("test no","test no");
                   otpDialog();
                   return;
               }
                readData(phone.getText().toString());
                //    Toast.makeText(this, "User does not exist.\nPlease register first.", Toast.LENGTH_SHORT).show();

            //    startPhoneNumberVerification(phone.getText().toString());

             // otpDialog();
               /*
                if(TextUtils.isEmpty(code)){
                    Toast.makeText(PhoneAuth.this,"Invalid One Time Password(OTP).",Toast.LENGTH_LONG).show();
                }
                verifyPhoneNumberWithCode(mVerificationId, code);
            */
       /*    String code = otp_text.getText().toString();if (TextUtils.isEmpty(code)||code.length()!=10) {
                    Toast.makeText(PhoneAuth.this,"Invalid",Toast.LENGTH_SHORT).show();
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, code);*/
                break;
            case R.id.Resend:
                resendVerificationCode(s, mResendToken);
                break;

        }
    }
}


