package com.example.visitormanagemet;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.*;

public class ResProfileFragment extends Fragment {
    TextView tvName, tvFlat, tvPhone, tvEmail, tvWing, tvSociety;
    Button btnEdit;
    String residentName, userKey; // unique key like "kurkute@gmail,com"

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_res_profile, container, false);

        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);
        tvPhone = view.findViewById(R.id.profile_phone);
        tvFlat = view.findViewById(R.id.profile_flat);
        tvWing = view.findViewById(R.id.profile_wing);
        tvSociety = view.findViewById(R.id.profile_society);
        btnEdit = view.findViewById(R.id.btn_edit_profile);

        if (getArguments() != null) {
            residentName = getArguments().getString("RESIDENT_NAME");
        }

        // Accessing "res" node specifically
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child("res");

        ref.orderByChild("name").equalTo(residentName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        userKey = ds.getKey(); // Retrieve the actual email-based key

                        tvName.setText("Name: " + ds.child("name").getValue(String.class));
                        tvEmail.setText("Email: " + ds.child("email").getValue(String.class));
                        tvPhone.setText("Mobile: " + ds.child("mobile").getValue(String.class));
                        tvFlat.setText("Flat No: " + ds.child("flatNo").getValue(String.class));
                        tvWing.setText("Wing: " + ds.child("wing").getValue(String.class));
                        tvSociety.setText("Society: " + ds.child("society").getValue(String.class));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnEdit.setOnClickListener(v -> {
            if (userKey != null) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                intent.putExtra("USER_KEY", userKey);
                startActivity(intent);
            }
        });

        return view;
    }
}