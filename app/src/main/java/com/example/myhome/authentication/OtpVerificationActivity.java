package com.example.myhome.authentication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.example.myhome.R;
import com.example.myhome.maincontroller.BottomNavBarActivity;
import com.example.myhome.reportissue.helpers.EmailSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.Random;

public class OtpVerificationActivity extends AppCompatActivity {
    private PinView pinFromUser;
    private String emailid,pwd;
    private TextView mob_No;
    private Button submit_btn,resend_btn;
    private Random rOtp;
    private char[] otp;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        if(getSupportActionBar()!=null) {
            getSupportActionBar().hide();
        }

        mp = MediaPlayer.create(OtpVerificationActivity.this, R.raw.buttonsound);

        rOtp = new Random();
        final String numbers = "0123456789";
        otp = new char[6];

        for (int i = 0; i < 6; i++)
        {
            otp[i] = numbers.charAt(rOtp.nextInt(numbers.length()));
        }

        emailid = getIntent().getStringExtra("emailfromlogin");
        pwd = getIntent().getStringExtra("pwdfromlogin");

        sendEmailOTPMessage();

        pinFromUser = findViewById(R.id.OTPPinView);
        mob_No = findViewById(R.id.mob_no);
        resend_btn = findViewById(R.id.resend_otp);
        submit_btn = findViewById(R.id.submit_otp);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.start();

                if (!pinFromUser.getText().toString().isEmpty()) {
                    if (pinFromUser.getText().toString().equals(String.valueOf(otp))) {
                        DynamicToast.makeSuccess(OtpVerificationActivity.this, "Verification Completed!!", 10).show();
                        signUser(emailid,pwd);

                    } else {
                        DynamicToast.makeError(OtpVerificationActivity.this, "Verification Not Completed! Try Again", 10).show();
                        startActivity(new Intent(OtpVerificationActivity.this, LoginActivity.class));
                        finish();
                    }
                }
                else{
                    pinFromUser.setError("OTP can't be empty!");
                }
            }
        });

        resend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { mp.start();
                sendEmailOTPMessage();
            }
        });
    }

    private void sendEmailOTPMessage() {
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EmailSender sn = new EmailSender();
                    EmailSender.sendEmail(emailid, "2 Factor OTP Verification for MyHome App","OTP for My Home 2 factor authentication is " + String.valueOf(otp));
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailid});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "\"2 Factor OTP Verification for MyHome App\"");
                    intent.putExtra(Intent.EXTRA_TEXT,"OTP for My Home 2 factor authentication is " + String.valueOf(otp));
//                    startActivity(intent);
//                    if (intent.resolveActivity(getPackageManager()) != null) {
//
//                    }

//                    MailSender sender = new MailSender("widambedeograss5@gmail.com", "0744748698");
//                    System.out.println(String.valueOf(otp));
//                    sender.sendMail("2 Factor OTP Verification for MyHome App",
//                            "OTP for My Home 2 factor authentication is " + String.valueOf(otp),
//                            "widambedeograss5@gmail.com",
//                            emailid);


                    OtpVerificationActivity.this.runOnUiThread(new Runnable()
                    {
                        @SuppressLint("SetTextI18n")
                        public void run()
                        {
                            mob_No.setText("Enter one time password sent on "+ emailid);
                            DynamicToast.makeSuccess(OtpVerificationActivity.this, "OTP was Send Successfully", 10).show();
                        }
                                    });

                } catch (Exception e) {
                    OtpVerificationActivity.this.runOnUiThread(new Runnable()
                    {
                        @SuppressLint("SetTextI18n")
                        public void run()
                        {
                            mob_No.setText("ERROR OCCURED! TRY AGAIN!");
                            mob_No.setTextColor(Color.RED);
                            DynamicToast.makeError(OtpVerificationActivity.this, "ERROR OCCURED!", 10).show();
                        }
                    });
                }
            }
        });
        sender.start();
    }
    public void signUser(String email, String password){

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    assert user != null;
                    if (!user.isEmailVerified()) {
                        DynamicToast.makeSuccess(OtpVerificationActivity.this, "Successfully logged in", 10).show();
                        startActivity(new Intent(OtpVerificationActivity.this, BottomNavBarActivity.class));
                        finish();
                    }
                    else {
                        FirebaseAuth.getInstance().signOut();
                        DynamicToast.makeWarning(OtpVerificationActivity.this, "Please Verify Your Email", 10).show();
                        startActivity(new Intent(OtpVerificationActivity.this, LoginActivity.class));
                        finish();
                    }
                }
                else{
                    DynamicToast.makeError(OtpVerificationActivity.this, "Invalid Email or Password", 10).show();
                    startActivity(new Intent(OtpVerificationActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });
    }
}
