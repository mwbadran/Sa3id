package com.example.sa3id.adminActivities;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.models.FeedbackMsg;
import com.example.sa3id.R;
import com.example.sa3id.models.User;
import com.example.sa3id.userActivities.CustomAlertDialog;
import com.example.sa3id.userActivities.MainActivity;
import com.example.sa3id.userActivities.SignIn;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;



public class FeedbackRespondActivity extends BaseActivity {
    private TabLayout tabLayout;
    private RecyclerView recyclerViewFeedback;
    private CardView cardFeedbackDetails, cardResponse;
    private TextView tvFeedbackSubject, tvFeedbackMessage, tvFeedbackInfo;
    private TextInputEditText etAdminResponse;
    private MaterialButton btnUpdateStatus, btnSendResponse;
    private ImageButton btnCloseDetails;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseFirestore mFirestore;
    private FirebaseRecyclerAdapter<FeedbackMsg, FeedbackViewHolder> adapter;

    private String currentFeedbackId;
    private String currentStatus = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase components first
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK).getReference();
        mFirestore = FirebaseFirestore.getInstance();

        checkAdminAccess();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        recyclerViewFeedback = findViewById(R.id.recyclerViewFeedback);
        cardFeedbackDetails = findViewById(R.id.cardFeedbackDetails);
        cardResponse = findViewById(R.id.cardResponse);
        tvFeedbackSubject = findViewById(R.id.tvFeedbackSubject);
        tvFeedbackMessage = findViewById(R.id.tvFeedbackMessage);
        tvFeedbackInfo = findViewById(R.id.tvFeedbackInfo);
        etAdminResponse = findViewById(R.id.etAdminResponse);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        btnSendResponse = findViewById(R.id.btnSendResponse);
        btnCloseDetails = findViewById(R.id.btnCloseDetails);

        // Setup RecyclerView
        recyclerViewFeedback.setLayoutManager(new LinearLayoutManager(this));

        // Setup click listeners
        btnCloseDetails.setOnClickListener(v -> closeDetailsView());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentStatus = "pending";
                        loadFeedbackData("pending");
                        break;
                    case 1:
                        currentStatus = "in-progress";
                        loadFeedbackData("in-progress");
                        break;
                    case 2:
                        currentStatus = "resolved";
                        loadFeedbackData("resolved");
                        break;
                }
                // Close details view when changing tabs
                closeDetailsView();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        btnUpdateStatus.setOnClickListener(v -> showStatusUpdateDialog());
        btnSendResponse.setOnClickListener(v -> sendResponse());
    }

    private void closeDetailsView() {
        cardFeedbackDetails.setVisibility(View.GONE);
        cardResponse.setVisibility(View.GONE);
        currentFeedbackId = null;
    }

    private void checkAdminAccess() {
        firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            CustomAlertDialog dialog = new CustomAlertDialog(this);
            dialog.show("يرجى تسجيل الدخول أولاً", R.drawable.baseline_error_24);
            startActivity(new Intent(this, SignIn.class));
            finish();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = new User(
                                documentSnapshot.getString("username"),
                                documentSnapshot.getString("email"),
                                documentSnapshot.getString("profilePic")
                        );

                        if (Boolean.TRUE.equals(documentSnapshot.getBoolean("admin"))) {
                            CustomAlertDialog dialog = new CustomAlertDialog(this);
                            dialog.show("مرحبًا " + user.getUsername() + "، تم تسجيل الدخول كمسؤول", R.drawable.baseline_check_circle_24);

                            // Continue with admin functionality
                            initViews();
                            loadFeedbackData("pending");
                        } else {
                            // Not an admin
                            CustomAlertDialog dialog = new CustomAlertDialog(this);
                            dialog.show("غير مصرح لك بالوصول إلى هذه الصفحة", R.drawable.baseline_error_24);
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }
                    } else {
                        // User document doesn't exist
                        CustomAlertDialog dialog = new CustomAlertDialog(this);
                        dialog.show("لم يتم العثور على الملف الشخصي للمستخدم", R.drawable.baseline_error_24);
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure (network issues, etc.)
                    CustomAlertDialog dialog = new CustomAlertDialog(this);
                    dialog.show("فشل في التحقق من حالة المسؤول: " + e.getMessage(), R.drawable.baseline_error_24);
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    private void loadFeedbackData(String status) {
        Query query = mDatabase.child("feedback").orderByChild("status").equalTo(status);

        FirebaseRecyclerOptions<FeedbackMsg> options =
                new FirebaseRecyclerOptions.Builder<FeedbackMsg>()
                        .setQuery(query, FeedbackMsg.class)
                        .build();

        if (adapter != null) {
            adapter.stopListening();
        }

        adapter = new FirebaseRecyclerAdapter<FeedbackMsg, FeedbackViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position, @NonNull FeedbackMsg model) {
                holder.tvSubject.setText(model.getSubject());
                holder.tvName.setText(model.getName());
                holder.tvDate.setText(model.getTimestamp());

                holder.itemView.setOnClickListener(v -> {
                    currentFeedbackId = model.getId();
                    showFeedbackDetails(model);
                });

                // Add long click listener for quick status update
                holder.itemView.setOnLongClickListener(v -> {
                    currentFeedbackId = model.getId();
                    showStatusUpdateDialog();
                    return true;
                });
            }

            @NonNull
            @Override
            public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_feedback, parent, false);
                return new FeedbackViewHolder(view);
            }
        };

        recyclerViewFeedback.setAdapter(adapter);
        adapter.startListening();
    }

    private void showFeedbackDetails(FeedbackMsg model) {
        tvFeedbackSubject.setText(model.getSubject());
        tvFeedbackMessage.setText(model.getMessage());
        tvFeedbackInfo.setText(String.format("المرسل: %s \nالبريد: %s \nالتاريخ: %s",
                model.getName(), model.getEmail(), model.getTimestamp()));

        cardFeedbackDetails.setVisibility(View.VISIBLE);
        cardResponse.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(model.getResponse())) {
            etAdminResponse.setText(model.getResponse());
        } else {
            etAdminResponse.setText("");
        }
    }

    private void showStatusUpdateDialog() {
        if (currentFeedbackId == null) {
            CustomAlertDialog dialog = new CustomAlertDialog(this);
            dialog.show("الرجاء تحديد استفسار أولاً", R.drawable.baseline_info_24);
            return;
        }

        String[] statuses = {"قيد الانتظار", "قيد المعالجة", "تم الرد"};
        String[] statusValues = {"pending", "in-progress", "resolved"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("تغيير حالة الاستفسار");
        builder.setSingleChoiceItems(statuses, -1, (dialog, which) -> {
            updateFeedbackStatus(statusValues[which]);
            dialog.dismiss();
        });
        builder.setNegativeButton("إلغاء", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateFeedbackStatus(String newStatus) {
        if (currentFeedbackId == null) return;

        mDatabase.child("feedback").child(currentFeedbackId).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    CustomAlertDialog dialog = new CustomAlertDialog(this);
                    dialog.show("تم تحديث الحالة بنجاح", R.drawable.baseline_check_circle_24);
                    loadFeedbackData(currentStatus);
                })
                .addOnFailureListener(e -> {
                    CustomAlertDialog dialog = new CustomAlertDialog(this);
                    dialog.show("فشل تحديث الحالة", R.drawable.baseline_error_24);
                });
    }

    private void sendResponse() {
        if (currentFeedbackId == null) {
            CustomAlertDialog dialog = new CustomAlertDialog(this);
            dialog.show("الرجاء تحديد استفسار أولاً", R.drawable.baseline_info_24);
            return;
        }

        String response = etAdminResponse.getText().toString().trim();
        if (TextUtils.isEmpty(response)) {
            etAdminResponse.setError("الرجاء كتابة الرد");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("response", response);
        updates.put("status", "resolved");
        updates.put("respondedBy", mAuth.getCurrentUser().getUid());
        updates.put("responseTimestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        mDatabase.child("feedback").child(currentFeedbackId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    CustomAlertDialog dialog = new CustomAlertDialog(this);
                    dialog.show("تم إرسال الرد بنجاح", R.drawable.baseline_check_circle_24);
                    loadFeedbackData(currentStatus);
                    closeDetailsView();
                })
                .addOnFailureListener(e -> {
                    CustomAlertDialog dialog = new CustomAlertDialog(this);
                    dialog.show("فشل إرسال الرد", R.drawable.baseline_error_24);
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    // ViewHolder for feedback items
    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvName, tvDate;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvItemSubject);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDate = itemView.findViewById(R.id.tvItemDate);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_feedback_respond;
    }
}