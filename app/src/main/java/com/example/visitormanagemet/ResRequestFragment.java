package com.example.visitormanagemet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResRequestFragment extends Fragment {
    RecyclerView recyclerView;
    VisitorAdapter adapter;
    List<VisitorRequest> list;
    DatabaseReference db;
    String residentName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Updated to your correct fragment layout name if needed, assuming fragment_res_request
        View view = inflater.inflate(R.layout.fragment_res_request, container, false);

        if (getArguments() != null) {
            residentName = getArguments().getString("RESIDENT_NAME");
        }

        recyclerView = view.findViewById(R.id.visitorRequestsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList<>();

        // Passing "Resident" role to adapter
        adapter = new VisitorAdapter(getContext(), list, "Resident");
        recyclerView.setAdapter(adapter);

        // Your current node is "Visits"
        db = FirebaseDatabase.getInstance().getReference("Visits");
        fetchRequests();
        return view;
    }

    private void fetchRequests() {
        if (residentName == null) return;

        db.orderByChild("residentName").equalTo(residentName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    VisitorRequest req = ds.getValue(VisitorRequest.class);
                    if (req != null && "Pending".equals(req.getStatus())) {
                        req.setKey(ds.getKey()); // Store the Firebase push ID
                        list.add(req);
                    }
                }
                Collections.reverse(list); // Newest requests on top
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}