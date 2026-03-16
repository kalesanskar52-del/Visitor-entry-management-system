package com.example.visitormanagemet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class VisitorAdapter extends RecyclerView.Adapter<VisitorAdapter.MyViewHolder> {
    Context context;
    List<VisitorRequest> list;
    String userRole;

    public VisitorAdapter(Context context, List<VisitorRequest> list, String userRole) {
        this.context = context;
        this.list = list;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_res_visitor_request, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        VisitorRequest request = list.get(position);
        holder.name.setText(request.getVisitorName());
        holder.status.setText("Status: " + request.getStatus());

        if ("History".equals(userRole)) {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.status.setVisibility(View.VISIBLE);

            if ("Approved".equals(request.getStatus())) {
                holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            } else {
                holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            }
        } else {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.status.setVisibility(View.GONE);

            holder.btnAccept.setOnClickListener(v -> updateStatus(request.getKey(), "Approved"));
            holder.btnReject.setOnClickListener(v -> updateStatus(request.getKey(), "Rejected"));
        }
    }

    private void updateStatus(String key, String newStatus) {
        if (key == null) return;
        FirebaseDatabase.getInstance().getReference("Visits").child(key).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Status: " + newStatus, Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, status;
        Button btnAccept, btnReject;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.visitorNameTxt);
            status = itemView.findViewById(R.id.statusTxt);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}