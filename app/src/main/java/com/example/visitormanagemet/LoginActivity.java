package com.example.visitormanagemet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etLoginID, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgetPassword;
    private ProgressBar progressBar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        etLoginID = findViewById(R.id.etLoginID);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgetPassword = findViewById(R.id.tvForgetPassword);

        // FIX: XML ki sahi ID 'loginProgressBar' use ki hai
        progressBar = findViewById(R.id.loginProgressBar);

        if (tvForgetPassword != null) {
            tvForgetPassword.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            });
        }

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> performLogin());
        }

        setupStyling();
    }

    private void performLogin() {
        String loginID = etLoginID.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (loginID.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // 1. Pehle 'rec' (Reception) folder mein check karo
        mDatabase.child("rec").orderByChild("loginID").equalTo(loginID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    handleUserSnapshot(snapshot, password, "Reception");
                } else {
                    // 2. Agar rec mein nahi mila, toh 'res' (Resident) mein check karo
                    checkInResident(loginID, password);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { showError(error.getMessage()); }
        });
    }

    private void checkInResident(String loginID, String password) {
        mDatabase.child("res").orderByChild("loginID").equalTo(loginID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    handleUserSnapshot(snapshot, password, "Resident");
                } else {
                    showError("User ID not found!");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { showError(error.getMessage()); }
        });
    }

    private void handleUserSnapshot(DataSnapshot snapshot, String password, String role) {
        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);

        for (DataSnapshot ds : snapshot.getChildren()) {
            String dbPass = ds.child("password").getValue(String.class);
            String name = ds.child("name").getValue(String.class);

            if (dbPass != null && dbPass.equals(password)) {
                Toast.makeText(LoginActivity.this, "Welcome " + name, Toast.LENGTH_SHORT).show();

                Intent intent;
                if ("Reception".equals(role)) {
                    intent = new Intent(LoginActivity.this, ReceptionDashboardActivity.class);
                } else {
                    // FIXED: ResidentDashboardActivity ka use kiya gaya hai
                    intent = new Intent(LoginActivity.this, ResidentDashboardActivity.class);
                    intent.putExtra("RESIDENT_NAME", name);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupStyling() {
        if (tvRegister == null) return;
        String fullText = "Don't have an account? Create one";
        SpannableString ss = new SpannableString(fullText);
        int start = fullText.indexOf("Create one");
        if (start != -1) {
            ss.setSpan(new ForegroundColorSpan(Color.BLACK), start, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvRegister.setText(ss);
    }
}