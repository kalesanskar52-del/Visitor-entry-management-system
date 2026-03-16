package com.example.visitormanagemet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Views
        ImageButton btnEditProfile = view.findViewById(R.id.btnEditProfile);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        MaterialButton btnHelp = view.findViewById(R.id.btnHelp);
        MaterialButton btnPrivacy = view.findViewById(R.id.btnPrivacy);

        // Inside your ProfileFragment.java, update the Edit Button click listener:
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            // Assuming you stored the loginID when the user logged in
            // For now, we pass a test ID; later you can use SharedPreferences to get the real one
            intent.putExtra("USER_ID", "REC001");
            startActivity(intent);
        });
        // Logout with Confirmation
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        btnHelp.setOnClickListener(v ->
                Toast.makeText(getContext(), "Contacting Admin...", Toast.LENGTH_SHORT).show());

        btnPrivacy.setOnClickListener(v ->
                Toast.makeText(getContext(), "Opening Privacy Policy...", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to exit the application?")
                .setPositiveButton("Sign Out", (dialog, which) -> performLogout())
                .setNegativeButton("Stay Logged In", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }
}