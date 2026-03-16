
package com.example.visitormanagemet;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etLoginID, etOTP, etNewPass, etConfirmPass;
    private Button btnSendOTP, btnReset;
    private String generatedOTP = "", userEmailKey = "";
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Fixed IDs to match XML
        etLoginID = findViewById(R.id.etForgotLoginID);
        etOTP = findViewById(R.id.etForgotOTP);
        etNewPass = findViewById(R.id.etNewPassword);
        etConfirmPass = findViewById(R.id.etConfirmNewPassword);
        btnSendOTP = findViewById(R.id.btnSendForgotOTP);
        btnReset = findViewById(R.id.btnResetPassword);

        btnSendOTP.setOnClickListener(v -> fetchEmailAndSendOTP());
        btnReset.setOnClickListener(v -> performReset());
        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
    }

    private void fetchEmailAndSendOTP() {
        String loginID = etLoginID.getText().toString().trim();
        if (loginID.isEmpty()) {
            Toast.makeText(this, "Please enter Login ID", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.orderByChild("loginID").equalTo(loginID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String email = ds.child("email").getValue(String.class);
                        userEmailKey = ds.getKey();
                        generatedOTP = String.valueOf(new Random().nextInt(899999) + 100000);
                        new SendEmailTask(email, "Reset OTP: " + generatedOTP, "Password Reset").execute();
                        Toast.makeText(ForgotPasswordActivity.this, "OTP sent to: " + email, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Invalid Login ID", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void performReset() {
        String otpInput = etOTP.getText().toString().trim();
        String pass = etNewPass.getText().toString().trim();
        String confirm = etConfirmPass.getText().toString().trim();

        if (otpInput.isEmpty() || !otpInput.equals(generatedOTP)) {
            Toast.makeText(this, "Wrong OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.isEmpty() || !pass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.child(userEmailKey).child("password").setValue(pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        String e, m, s;
        SendEmailTask(String e, String m, String s) { this.e = e; this.m = m; this.s = s; }
        @Override protected Boolean doInBackground(Void... v) {
            final String user = "visitormanagement27@gmail.com", pass = "xipm ubre erwa mbig";
            Properties p = new Properties();
            p.put("mail.smtp.host", "smtp.gmail.com"); p.put("mail.smtp.socketFactory.port", "465");
            p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            p.put("mail.smtp.auth", "true"); p.put("mail.smtp.port", "465");
            Session sn = Session.getInstance(p, new Authenticator() { protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(user, pass); } });
            try { MimeMessage mm = new MimeMessage(sn); mm.setFrom(new InternetAddress(user)); mm.addRecipient(Message.RecipientType.TO, new InternetAddress(e)); mm.setSubject(s); mm.setText(m); Transport.send(mm); return true; } catch (Exception ex) { return false; }
        }
    }
}