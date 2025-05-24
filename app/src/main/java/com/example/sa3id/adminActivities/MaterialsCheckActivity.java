package com.example.sa3id.adminActivities;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.example.sa3id.models.UploadRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MaterialsCheckActivity extends BaseActivity {

    private TextInputEditText etSearchMaterials;
    private CardView btnSearchMaterials;
    private MaterialButton btnApproveAll, btnRejectAll;
    private CardView cardNoMaterials;
    private RecyclerView recyclerViewMaterials;
    private DatabaseReference mDatabase;
    private List<UploadRequest> uploadRequests;
    private List<String> requestIds;
    private MaterialsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK).getReference("upload_requests");
        uploadRequests = new ArrayList<>();
        requestIds = new ArrayList<>();

        initViews();
        setupListeners();
        loadUploadRequests();
    }

    private void initViews() {
        etSearchMaterials = findViewById(R.id.etSearchMaterials);
        btnSearchMaterials = findViewById(R.id.btnSearchMaterials);
        btnApproveAll = findViewById(R.id.btnApproveAll);
        btnRejectAll = findViewById(R.id.btnRejectAll);
        cardNoMaterials = findViewById(R.id.cardNoMaterials);
        recyclerViewMaterials = findViewById(R.id.recyclerViewMaterials);
        recyclerViewMaterials.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaterialsAdapter(uploadRequests, requestIds);
        recyclerViewMaterials.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSearchMaterials.setOnClickListener(v -> searchMaterials());

        btnApproveAll.setOnClickListener(v -> {
            if (uploadRequests.isEmpty()) {
                Toast.makeText(this, "لا توجد مواد للموافقة عليها", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < requestIds.size(); i++) {
                approveRequest(requestIds.get(i));
            }

            Toast.makeText(this, "تمت الموافقة على جميع المواد", Toast.LENGTH_SHORT).show();
            loadUploadRequests(); // Refresh the list
        });

        btnRejectAll.setOnClickListener(v -> {
            // Implement reject all functionality
            if (uploadRequests.isEmpty()) {
                Toast.makeText(this, "لا توجد مواد لرفضها", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < requestIds.size(); i++) {
                rejectRequest(requestIds.get(i));
            }

            Toast.makeText(this, "تم رفض جميع المواد", Toast.LENGTH_SHORT).show();
            loadUploadRequests(); // Refresh the list
        });
    }

    private void searchMaterials() {
        String searchQuery = etSearchMaterials.getText().toString().trim();

        if (searchQuery.isEmpty()) {
            loadUploadRequests();
            return;
        }

        Query query = mDatabase.orderByChild("description")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uploadRequests.clear();
                requestIds.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UploadRequest request = snapshot.getValue(UploadRequest.class);
                    if (request != null) {
                        uploadRequests.add(request);
                        requestIds.add(snapshot.getKey());
                    }
                }

                adapter.notifyDataSetChanged();
                updateNoMaterialsCardVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MaterialsCheckActivity.this, "فشل البحث: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUploadRequests() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uploadRequests.clear();
                requestIds.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UploadRequest request = snapshot.getValue(UploadRequest.class);
                    if (request != null) {
                        uploadRequests.add(request);
                        requestIds.add(snapshot.getKey());
                    }
                }

                adapter.notifyDataSetChanged();
                updateNoMaterialsCardVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MaterialsCheckActivity.this, "فشل تحميل البيانات: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNoMaterialsCardVisibility() {
        if (uploadRequests.isEmpty()) {
            cardNoMaterials.setVisibility(View.VISIBLE);
            recyclerViewMaterials.setVisibility(View.GONE);
        } else {
            cardNoMaterials.setVisibility(View.GONE);
            recyclerViewMaterials.setVisibility(View.VISIBLE);
        }
    }

    private void approveRequest(String requestId) {
        DatabaseReference approvedRef = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK).getReference("approved_materials");
        DatabaseReference requestRef = mDatabase.child(requestId);

        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UploadRequest request = dataSnapshot.getValue(UploadRequest.class);
                if (request != null) {
                    approvedRef.child(requestId).setValue(request);
                    requestRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MaterialsCheckActivity.this, "فشل الموافقة: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectRequest(String requestId) {
        mDatabase.child(requestId).removeValue();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_materials_check;
    }

    private class MaterialsAdapter extends RecyclerView.Adapter<MaterialsAdapter.ViewHolder> {
        private List<UploadRequest> requests;
        private List<String> requestIds;

        public MaterialsAdapter(List<UploadRequest> requests, List<String> requestIds) {
            this.requests = requests;
            this.requestIds = requestIds;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_material_check, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UploadRequest request = requests.get(position);

            // Extract subject from description
            String fullDescription = request.getDescription();
            String subject = "غير محدد";
            String descriptionBody = fullDescription;

            if (fullDescription.contains("المادة:")) {
                int subjectStart = fullDescription.indexOf("المادة:") + "المادة:".length();
                int endIndex = fullDescription.indexOf("\n", subjectStart);
                if (endIndex == -1) endIndex = fullDescription.length();
                subject = fullDescription.substring(subjectStart, endIndex).trim();
                if (endIndex + 1 <= fullDescription.length()) {
                    descriptionBody = fullDescription.substring(endIndex).trim();
                }
            }

            // Set data to views
            holder.tvMaterialTitle.setText(subject);
            holder.tvMaterialDescription.setText(descriptionBody);
            holder.tvMaterialSubject.setText("المادة: " + subject);
            holder.tvMaterialSubmittedBy.setText("تم الإرسال بواسطة: " + request.getSenderEmail());
            holder.tvMaterialSubmissionDate.setText("تاريخ الإرسال: " + request.getTimestamp());

            // Handle file viewing
            holder.btnViewFile.setOnClickListener(v -> {
                List<String> materials = request.getMaterialsList();
                if (materials == null || materials.isEmpty()) {
                    Toast.makeText(MaterialsCheckActivity.this, "لا توجد ملفات متاحة", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Open first file (you can modify this to show a list)
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(materials.get(0)));
                startActivity(intent);
            });

            // Approval button
            holder.btnApprove.setOnClickListener(v -> {
                approveRequest(requestIds.get(position));
                Toast.makeText(MaterialsCheckActivity.this, "تمت الموافقة على المواد", Toast.LENGTH_SHORT).show();
            });

            // Rejection button
            holder.btnReject.setOnClickListener(v -> {
                rejectRequest(requestIds.get(position));
                Toast.makeText(MaterialsCheckActivity.this, "تم رفض المواد", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMaterialTitle, tvMaterialDescription, tvMaterialSubject,
                    tvMaterialSubmittedBy, tvMaterialSubmissionDate;
            MaterialButton btnViewFile, btnApprove, btnReject;

            ViewHolder(View itemView) {
                super(itemView);
                tvMaterialTitle = itemView.findViewById(R.id.tvMaterialTitle);
                tvMaterialDescription = itemView.findViewById(R.id.tvMaterialDescription);
                tvMaterialSubject = itemView.findViewById(R.id.tvMaterialSubject);
                tvMaterialSubmittedBy = itemView.findViewById(R.id.tvMaterialSubmittedBy);
                tvMaterialSubmissionDate = itemView.findViewById(R.id.tvMaterialSubmissionDate);
                btnViewFile = itemView.findViewById(R.id.btnViewFile);
                btnApprove = itemView.findViewById(R.id.btnApproveMaterial);
                btnReject = itemView.findViewById(R.id.btnRejectMaterial);
            }
        }
    }


}
