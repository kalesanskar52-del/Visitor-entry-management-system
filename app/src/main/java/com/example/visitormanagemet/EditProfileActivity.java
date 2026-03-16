package com.example.visitormanagemet;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etMobile, etFlat, etWing, etSociety;
    private MaterialButton btnSave;
    private ImageButton btnBack;
    private DatabaseReference mDatabase;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize all fields
        etName = findViewById(R.id.etEditName);
        etMobile = findViewById(R.id.etEditMobile);
        etFlat = findViewById(R.id.etEditFlat);
        etWing = findViewById(R.id.etEditWing);
        etSociety = findViewById(R.id.etEditSociety);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnBack = findViewById(R.id.btnBack);

        // Receive the unique email key (e.g., "user@gmail,com")
        userKey = getIntent().getStringExtra("USER_KEY");

        if (userKey != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference("Users").child("res").child(userKey);
            loadCurrentData();
        } else {
            Toast.makeText(this, "User key missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveAllChanges());
    }

    private void loadCurrentData() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etName.setText(snapshot.child("name").getValue(String.class));
                    etMobile.setText(snapshot.child("mobile").getValue(String.class));
                    etFlat.setText(snapshot.child("flatNo").getValue(String.class));
                    etWing.setText(snapshot.child("wing").getValue(String.class));
                    etSociety.setText(snapshot.child("society").getValue(String.class));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveAllChanges() {
        // Create a map to update all fields except password
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etName.getText().toString().trim());
        updates.put("mobile", etMobile.getText().toString().trim());
        updates.put("flatNo", etFlat.getText().toString().trim());
        updates.put("wing", etWing.getText().toString().trim());
        updates.put("society", etSociety.getText().toString().trim());

        // Basic validation
        if (updates.get("name").toString().isEmpty() || updates.get("mobile").toString().isEmpty()) {
            Toast.makeText(this, "Name and Mobile are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform the multi-field update
        mDatabase.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(EditProfileActivity.this, "Database Updated Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(EditProfileActivity.this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}