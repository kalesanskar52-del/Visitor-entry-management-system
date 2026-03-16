package com.example.visitormanagemet;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ResidentDashboardActivity extends AppCompatActivity {

    private String residentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_dashboard);

        residentName = getIntent().getStringExtra("RESIDENT_NAME");

        BottomNavigationView bottomNav = findViewById(R.id.resident_bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_requests) selectedFragment = new ResRequestFragment();
            else if (id == R.id.nav_history) selectedFragment = new ResHistoryFragment();
            else if (id == R.id.nav_sos) selectedFragment = new ResSOSFragment(); // Updated name
            else if (id == R.id.nav_profile) selectedFragment = new ResProfileFragment(); // Updated name

            if (selectedFragment != null) {
                Bundle bundle = new Bundle();
                bundle.putString("RESIDENT_NAME", residentName);
                selectedFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.resident_fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Default Page: Requests
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_requests);
        }
    }
}