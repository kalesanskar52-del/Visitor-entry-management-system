package com.example.visitormanagemet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.*;

public class ResSOSFragment extends Fragment {
    private DatabaseReference mDatabase;
    private String residentName;
    private String targetMobile = "100"; // Fallback number

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_res_sos, container, false);
        Button btnSos = view.findViewById(R.id.btn_res_sos_call);

        if (getArguments() != null) {
            residentName = getArguments().getString("RESIDENT_NAME");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("Visits");
        getLatestReceptionistContact();

        btnSos.setOnClickListener(v -> makeCall(targetMobile));
        return view;
    }

    private void getLatestReceptionistContact() {
        if (residentName == null) return;
        // Query the last request handled for this resident
        mDatabase.orderByChild("residentName").equalTo(residentName).limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            VisitorRequest req = ds.getValue(VisitorRequest.class);
                            if (req != null && req.getReceptionistMobile() != null) {
                                targetMobile = req.getReceptionistMobile();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void makeCall(String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error placing call", Toast.LENGTH_SHORT).show();
        }
    }
}