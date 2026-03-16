package com.example.visitormanagemet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.mail.*;
import javax.mail.internet.*;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etMobile, etEmail, etOTP, etAadhar, etSociety, etWing, etFlatNo, etPass, etCPass;
    private Button btnSendOTP, btnRegister;
    private LinearLayout layoutReception, layoutResident;
    private MaterialButtonToggleGroup roleToggleGroup;
    private Spinner spinnerGate, spinnerShift;
    private String selectedRole = "Reception", generatedOTP = "";
    private DatabaseReference mDatabase;
    private ImageView imgProfile;

    private static final int CAMERA_REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mDatabase = FirebaseDatabase.getInstance().getReference("Users");
        initViews();
        setupSpinners();

        // 3. Camera Click Logic (Take Photo par camera open hoga)
        findViewById(R.id.btnCapturePhoto).setOnClickListener(v -> {
            Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(iCamera, CAMERA_REQ_CODE);
        });

        roleToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnRoleReception) {
                    selectedRole = "Reception";
                    layoutReception.setVisibility(View.VISIBLE);
                    layoutResident.setVisibility(View.GONE);
                } else {
                    selectedRole = "Resident";
                    layoutReception.setVisibility(View.GONE);
                    layoutResident.setVisibility(View.VISIBLE);
                }
            }
        });

        btnSendOTP.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) { etEmail.setError("Email is mandatory"); return; }
            generatedOTP = String.valueOf(new Random().nextInt(899999) + 100000);
            new SendEmailTask(email, "Your OTP: " + generatedOTP, "OTP Verification").execute();
            Toast.makeText(this, "OTP Sent!", Toast.LENGTH_SHORT).show();
        });

        btnRegister.setOnClickListener(v -> performFinalValidation());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CAMERA_REQ_CODE) {
            Bitmap img = (Bitmap) (data.getExtras().get("data"));
            imgProfile.setImageBitmap(img);
        }
    }

    private void performFinalValidation() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String otp = etOTP.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        String cpass = etCPass.getText().toString().trim();

        // Basic Empty Check
        if (name.isEmpty() || mobile.isEmpty() || email.isEmpty() || otp.isEmpty() || pass.isEmpty() || cpass.isEmpty()) {
            Toast.makeText(this, "All fields are compulsory!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Mobile Number Check (Compulsory 10 Digit)
        if (mobile.length() != 10) {
            etMobile.setError("Mobile number must be exactly 10 digits");
            return;
        }

        // 1. Password Strictly Check (1 Capital, 1 Special, 1 Number)
        if (!isValidPassword(pass)) {
            etPass.setError("Password must have 1 Capital, 1 Special character and 1 Number");
            Toast.makeText(this, "Password is too weak!", Toast.LENGTH_LONG).show();
            return;
        }

        if (generatedOTP.isEmpty() || !otp.equals(generatedOTP)) {
            Toast.makeText(this, "Invalid OTP!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(cpass)) {
            etCPass.setError("Passwords do not match!");
            return;
        }

        String safeEmail = email.replace(".", ",");
        String folder = selectedRole.equals("Reception") ? "rec" : "res";

        mDatabase.child(folder).child(safeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(SignupActivity.this, "Already registered as " + selectedRole, Toast.LENGTH_SHORT).show();
                } else {
                    if (selectedRole.equals("Resident")) checkFlatDuplicate();
                    else saveToFirebase();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Password Validation Logic
    private boolean isValidPassword(String password) {
        Pattern caps = Pattern.compile("[A-Z]");
        Pattern nums = Pattern.compile("[0-9]");
        Pattern spec = Pattern.compile("[@#$%^&+=!_]");
        return caps.matcher(password).find() &&
                nums.matcher(password).find() &&
                spec.matcher(password).find() &&
                password.length() >= 6;
    }

    private void checkFlatDuplicate() {
        String soc = etSociety.getText().toString().trim();
        String win = etWing.getText().toString().trim();
        String flt = etFlatNo.getText().toString().trim();

        mDatabase.child("res").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean duplicate = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (soc.equalsIgnoreCase(ds.child("society").getValue(String.class)) &&
                            win.equalsIgnoreCase(ds.child("wing").getValue(String.class)) &&
                            flt.equals(ds.child("flatNo").getValue(String.class))) {
                        duplicate = true; break;
                    }
                }
                if (duplicate) Toast.makeText(SignupActivity.this, "Flat already registered!", Toast.LENGTH_LONG).show();
                else saveToFirebase();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveToFirebase() {
        String email = etEmail.getText().toString().trim();
        String folder = selectedRole.equals("Reception") ? "rec" : "res";
        String prefix = selectedRole.equals("Reception") ? "REC" : "RES";
        String uniqueID = prefix + (new Random().nextInt(89999) + 10000);

        Map<String, Object> user = new HashMap<>();
        user.put("name", etName.getText().toString().trim());
        user.put("mobile", etMobile.getText().toString().trim());
        user.put("email", email);
        user.put("role", selectedRole);
        user.put("loginID", uniqueID);
        user.put("password", etPass.getText().toString().trim());

        if (selectedRole.equals("Reception")) {
            user.put("aadhar", etAadhar.getText().toString().trim());
            user.put("gate", spinnerGate.getSelectedItem().toString());
            user.put("shift", spinnerShift.getSelectedItem().toString());
        } else {
            user.put("society", etSociety.getText().toString().trim());
            user.put("wing", etWing.getText().toString().trim());
            user.put("flatNo", etFlatNo.getText().toString().trim());
        }

        mDatabase.child(folder).child(email.replace(".", ",")).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                new SendEmailTask(email, "Your Unique Login ID: " + uniqueID, "Registration Successful").execute();
                Toast.makeText(this, "Success! ID: " + uniqueID, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void initViews() {
        imgProfile = findViewById(R.id.imgProfile);
        etName = findViewById(R.id.etFullName); etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail); etOTP = findViewById(R.id.etOTP);
        etAadhar = findViewById(R.id.etAadhar); etSociety = findViewById(R.id.etSociety);
        etWing = findViewById(R.id.etWing); etFlatNo = findViewById(R.id.etFlatNo);
        etPass = findViewById(R.id.etPassword); etCPass = findViewById(R.id.etConfirmPassword);
        btnSendOTP = findViewById(R.id.btnSendOTP); btnRegister = findViewById(R.id.btnRegisterAccount);
        roleToggleGroup = findViewById(R.id.roleToggleGroup);
        layoutReception = findViewById(R.id.layoutReception); layoutResident = findViewById(R.id.layoutResident);
        spinnerGate = findViewById(R.id.spinnerGate); spinnerShift = findViewById(R.id.spinnerShift);
    }

    private void setupSpinners() {
        spinnerGate.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Main Gate", "Exit Gate"}));
        spinnerShift.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Day (8AM-8PM)", "Night (8PM-8AM)"}));
    }

    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private String email, message, subject;
        SendEmailTask(String email, String message, String subject) { this.email = email; this.message = message; this.subject = subject; }
        @Override protected Boolean doInBackground(Void... voids) {
            final String user = "visitormanagement27@gmail.com", pass = "xipm ubre erwa mbig";
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com"); props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true"); props.put("mail.smtp.port", "465");
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(user, pass); }
            });
            try {
                MimeMessage mm = new MimeMessage(session);
                mm.setFrom(new InternetAddress(user)); mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                mm.setSubject(subject); mm.setText(message); Transport.send(mm);
                return true;
            } catch (Exception e) { return false; }
        }
    }
}