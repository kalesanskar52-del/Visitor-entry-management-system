package com.example.visitormanagemet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private VisitorAdapter adapter;
    private List<VisitorRequest> historyList;
    private DatabaseReference mDatabase;
    private String currentResidentName;
    private TextView tvNoHistory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_res_history, container, false);

        // 1. Receive Resident Name from the Login/Main Activity bundle
        if (getArguments() != null) {
            currentResidentName = getArguments().getString("RESIDENT_NAME");
        }

        // 2. Initialize Views
        recyclerView = view.findViewById(R.id.historyRecyclerView);
        tvNoHistory = view.findViewById(R.id.tvNoHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyList = new ArrayList<>();

        // Use "History" mode so the adapter hides the Accept/Reject buttons
        adapter = new VisitorAdapter(getContext(), historyList, "History");
        recyclerView.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("Visits");

        fetchHistoryData();

        return view;
    }

    private void fetchHistoryData() {
        if (currentResidentName == null) return;

        // Query: Filter by this specific resident
        Query historyQuery = mDatabase.orderByChild("residentName").equalTo(currentResidentName);

        historyQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    VisitorRequest request = ds.getValue(VisitorRequest.class);

                    // Only add to history if status is NOT "Pending"
                    if (request != null && !"Pending".equals(request.getStatus())) {
                        request.setKey(ds.getKey()); // Store key for consistency
                        historyList.add(request);
                    }
                }

                // Show newest history at the top
                Collections.reverse(historyList);
                adapter.notifyDataSetChanged();

                // Toggle empty state message
                tvNoHistory.setVisibility(historyList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}